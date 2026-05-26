const path = require("path");
const db = require("../db/knex");
const { uploadRoot } = require("../middleware/upload");
const { sendNotificationToUsers } = require("../services/notify");

const NEAR_PICKUP_KM = 3;

function isValidCoordinate(value) {
  return Number.isFinite(value) && Math.abs(value) <= 180;
}

function toRadians(value) {
  return (value * Math.PI) / 180;
}

function calculateDistanceKm(lat1, lng1, lat2, lng2) {
  const earthRadiusKm = 6371;
  const dLat = toRadians(lat2 - lat1);
  const dLng = toRadians(lng2 - lng1);
  const startLat = toRadians(lat1);
  const endLat = toRadians(lat2);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(startLat) * Math.cos(endLat) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadiusKm * c;
}

function normalizeDistanceKm(distanceKm) {
  return Number.isFinite(distanceKm) ? Number(distanceKm.toFixed(2)) : null;
}

async function notifyNearbyOrders(userId, currentLat, currentLng) {
  if (!isValidCoordinate(currentLat) || !isValidCoordinate(currentLng)) {
    return;
  }

  const driverUser = await db("users").select("user_id", "fcm_token").where({ user_id: userId }).first();
  if (!driverUser) {
    return;
  }

  const pendingOrders = await db("orders")
    .select("order_id", "pickup_lat", "pickup_lng")
    .where({ status: "pending" })
    .whereNull("driver_id");

  if (!pendingOrders.length) {
    return;
  }

  const existingRows = await db("notifications")
    .select("data_json")
    .where({ user_id: userId, type: "near_pickup" });

  const notifiedOrderIds = new Set(
    existingRows
      .map((row) => {
        try {
          return JSON.parse(row.data_json || "{}").order_id;
        } catch {
          return null;
        }
      })
      .filter((value) => value)
  );

  for (const order of pendingOrders) {
    if (!isValidCoordinate(order.pickup_lat) || !isValidCoordinate(order.pickup_lng)) {
      continue;
    }

    const distanceKm = normalizeDistanceKm(
      calculateDistanceKm(currentLat, currentLng, order.pickup_lat, order.pickup_lng)
    );

    if (distanceKm === null || distanceKm > NEAR_PICKUP_KM) {
      continue;
    }

    const orderIdString = String(order.order_id);
    if (notifiedOrderIds.has(orderIdString)) {
      continue;
    }

    await sendNotificationToUsers(
      [{ user_id: userId, fcm_token: driverUser.fcm_token }],
      {
        title: "Đơn hàng gần điểm lấy",
        body: `Bạn cách điểm lấy đơn #${order.order_id} khoảng ${distanceKm} km.`,
        type: "near_pickup",
        data: {
          type: "near_pickup",
          order_id: orderIdString,
          distance_km: String(distanceKm)
        }
      }
    );
  }
}

function toPublicUrl(filePath) {
  const relativePath = path.relative(uploadRoot, filePath).split(path.sep).join("/");
  return `/uploads/${relativePath}`;
}

async function getDriverProfile(req, res, next) {
  try {
    const userId = req.user.user_id;
    const profile = await db("users")
      .leftJoin("drivers", "users.user_id", "drivers.driver_id")
      .select(
        "users.user_id",
        "users.phone",
        "users.full_name",
        "users.email",
        "users.avatar_url",
        "users.cccd_number",
        "users.id_card_front_url",
        "users.id_card_back_url",
        "users.portrait_url",
        "users.is_verified",
        "users.identity_reject_reason",
        "users.role",
        "drivers.vehicle_type",
        "drivers.license_plate",
        "drivers.is_online",
        "drivers.wallet_balance",
        "drivers.rating_avg"
      )
      .where("users.user_id", userId)
      .first();

    if (!profile) {
      return res.status(404).json({ success: false, message: "Driver not found" });
    }

    return res.json({ success: true, profile });
  } catch (error) {
    return next(error);
  }
}

async function updateDriverProfile(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { license_plate, vehicle_type } = req.body;

    const existing = await db("drivers").where({ driver_id: userId }).first();

    if (existing) {
      await db("drivers")
        .where({ driver_id: userId })
        .update({
          license_plate,
          vehicle_type: vehicle_type ?? existing.vehicle_type
        });
    } else {
      await db("drivers").insert({
        driver_id: userId,
        license_plate,
        vehicle_type: vehicle_type ?? "Motorbike",
        is_online: 0,
        wallet_balance: 0,
        rating_avg: 5
      });
    }

    return res.json({ success: true, message: "Driver profile updated" });
  } catch (error) {
    return next(error);
  }
}

async function updateDriverLocation(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { current_lat, current_lng, is_online } = req.body;

    const existing = await db("drivers").where({ driver_id: userId }).first();
    const payload = {
      current_lat,
      current_lng,
      is_online: is_online ?? 1
    };

    if (existing) {
      await db("drivers").where({ driver_id: userId }).update(payload);
    } else {
      await db("drivers").insert({
        driver_id: userId,
        license_plate: "",
        vehicle_type: "Motorbike",
        wallet_balance: 0,
        rating_avg: 5,
        ...payload
      });
    }

    const isOnline = payload.is_online ?? existing?.is_online ?? 1;
    if (isOnline !== 0) {
      await notifyNearbyOrders(userId, current_lat, current_lng);
    }

    return res.json({ success: true, message: "Đã cập nhật vị trí tài xế." });
  } catch (error) {
    return next(error);
  }
}

async function getDriverWallet(req, res, next) {
  try {
    const userId = req.user.user_id;
    const wallet = await db("drivers")
      .select("wallet_balance", "rating_avg")
      .where({ driver_id: userId })
      .first();

    if (!wallet) {
      return res.status(404).json({ success: false, message: "Driver wallet not found" });
    }

    return res.json({ success: true, wallet });
  } catch (error) {
    return next(error);
  }
}

async function listAvailableOrders(req, res, next) {
  try {
    const orders = await db("orders")
      .leftJoin("users", "orders.customer_id", "users.user_id")
      .select(
        "orders.order_id",
        "orders.package_type",
        "orders.weight_kg",
        "orders.pickup_address",
        "orders.delivery_address",
        "orders.pickup_lat",
        "orders.pickup_lng",
        "orders.delivery_lat",
        "orders.delivery_lng",
        "orders.distance_km",
        "orders.total_price",
        "orders.driver_earning",
        "orders.status",
        "orders.created_at",
        "users.full_name as customer_name",
        "users.phone as customer_phone"
      )
      .where({ "orders.status": "pending" })
      .whereNull("orders.driver_id")
      .orderBy("orders.created_at", "desc");

    const driver = await db("drivers")
      .select("current_lat", "current_lng")
      .where({ driver_id: req.user.user_id })
      .first();

    const driverLat = driver?.current_lat;
    const driverLng = driver?.current_lng;
    const shouldComputeDistance = isValidCoordinate(driverLat) && isValidCoordinate(driverLng);

    const ordersWithDistance = shouldComputeDistance
      ? orders.map((order) => {
          if (!isValidCoordinate(order.pickup_lat) || !isValidCoordinate(order.pickup_lng)) {
            return { ...order, distance_to_pickup_km: null, is_near_pickup: false };
          }

          const distanceToPickup = normalizeDistanceKm(
            calculateDistanceKm(driverLat, driverLng, order.pickup_lat, order.pickup_lng)
          );
          return {
            ...order,
            distance_to_pickup_km: distanceToPickup,
            is_near_pickup: distanceToPickup !== null && distanceToPickup <= NEAR_PICKUP_KM
          };
        })
      : orders;

    return res.json({ success: true, orders: ordersWithDistance });
  } catch (error) {
    return next(error);
  }
}

async function listActiveOrders(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const orders = await db("orders")
      .leftJoin("users", "orders.customer_id", "users.user_id")
      .select(
        "orders.order_id",
        "orders.package_type",
        "orders.weight_kg",
        "orders.pickup_address",
        "orders.delivery_address",
        "orders.distance_km",
        "orders.total_price",
        "orders.driver_earning",
        "orders.status",
        "orders.created_at",
        "orders.accepted_at",
        "users.full_name as customer_name",
        "users.phone as customer_phone"
      )
      .where({ "orders.driver_id": driverId })
      .whereIn("orders.status", ["accepted", "picking_up", "delivering", "arrived_delivery"])
      .orderBy("orders.created_at", "desc");

    return res.json({ success: true, orders });
  } catch (error) {
    return next(error);
  }
}

async function listOrderHistory(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const orders = await db("orders")
      .leftJoin("users", "orders.customer_id", "users.user_id")
      .select(
        "orders.order_id",
        "orders.package_type",
        "orders.weight_kg",
        "orders.pickup_address",
        "orders.delivery_address",
        "orders.distance_km",
        "orders.total_price",
        "orders.driver_earning",
        "orders.status",
        "orders.completed_at",
        "users.full_name as customer_name",
        "users.phone as customer_phone"
      )
      .where({ "orders.driver_id": driverId })
      .whereIn("orders.status", ["completed", "cancelled"])
      .orderBy("orders.completed_at", "desc");

    return res.json({ success: true, orders });
  } catch (error) {
    return next(error);
  }
}

async function acceptOrder(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const { id } = req.params;
    const updated = await db("orders")
      .where({ order_id: id, status: "pending" })
      .whereNull("driver_id")
      .update({
        driver_id: driverId,
        status: "accepted",
        accepted_at: db.fn.now()
      });

    if (!updated) {
      return res.status(409).json({ success: false, message: "Order not available" });
    }

    const order = await db("orders")
      .select("order_id", "customer_id")
      .where({ order_id: id })
      .first();
    const customer = await db("users")
      .select("fcm_token")
      .where({ user_id: order?.customer_id })
      .first();

    if (order?.customer_id) {
      await sendNotificationToUsers(
        [{ user_id: order.customer_id, fcm_token: customer?.fcm_token }],
        {
          title: "Đơn hàng đã có tài xế",
          body: `Đơn #${id} đã được tài xế nhận.`,
          data: {
            type: "order_accepted",
            order_id: String(id)
          }
        }
      );
    }

    return res.json({ success: true, message: "Order accepted" });
  } catch (error) {
    return next(error);
  }
}

async function arrivePickup(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const { id } = req.params;
    const updated = await db("orders")
      .where({ order_id: id, driver_id: driverId, status: "accepted" })
      .update({ status: "picking_up" });

    if (!updated) {
      return res.status(409).json({ success: false, message: "Invalid order state" });
    }

    return res.json({ success: true, message: "Arrived at pickup" });
  } catch (error) {
    return next(error);
  }
}

async function pickupOrder(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const { id } = req.params;

    if (!req.file) {
      return res.status(400).json({ success: false, message: "Missing pickup photo" });
    }

    const photoUrl = toPublicUrl(req.file.path);
    const updated = await db("orders")
      .where({ order_id: id, driver_id: driverId })
      .whereIn("status", ["accepted", "picking_up"])
      .update({
        status: "delivering",
        photo_at_pickup: photoUrl
      });

    if (!updated) {
      return res.status(409).json({ success: false, message: "Invalid order state" });
    }

    return res.json({ success: true, message: "Pickup confirmed", photo_url: photoUrl });
  } catch (error) {
    return next(error);
  }
}

async function arriveDelivery(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const { id } = req.params;

    const updated = await db("orders")
      .where({ order_id: id, driver_id: driverId, status: "delivering" })
      .update({ status: "arrived_delivery" });

    if (!updated) {
      return res.status(409).json({ success: false, message: "Invalid order state" });
    }

    return res.json({ success: true, message: "Arrived at delivery" });
  } catch (error) {
    return next(error);
  }
}

async function completeOrder(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const { id } = req.params;

    if (!req.file) {
      return res.status(400).json({ success: false, message: "Missing delivery photo" });
    }

    const photoUrl = toPublicUrl(req.file.path);
    const trx = await db.transaction();

    try {
      const order = await trx("orders")
        .where({ order_id: id, driver_id: driverId })
        .whereIn("status", ["delivering", "arrived_delivery"])
        .first();

      if (!order) {
        await trx.rollback();
        return res.status(409).json({ success: false, message: "Invalid order state" });
      }

      const driver = await trx("drivers").where({ driver_id: driverId }).first();
      if (!driver) {
        await trx.rollback();
        return res.status(409).json({ success: false, message: "Driver profile missing" });
      }

      await trx("orders")
        .where({ order_id: id })
        .update({
          status: "completed",
          photo_at_delivery: photoUrl,
          completed_at: trx.fn.now()
        });

      await trx("drivers")
        .where({ driver_id: driverId })
        .update({
          wallet_balance: trx.raw("wallet_balance + ?", [order.driver_earning])
        });

      await trx("revenue_logs").insert([
        {
          order_id: id,
          amount: order.app_commission,
          log_type: "commission",
          note: "Commission from order"
        },
        {
          order_id: id,
          amount: order.driver_earning,
          log_type: "payout",
          note: "Driver earning"
        }
      ]);

      await trx.commit();
      return res.json({ success: true, message: "Order completed", photo_url: photoUrl });
    } catch (error) {
      await trx.rollback();
      return next(error);
    }
  } catch (error) {
    return next(error);
  }
}

function buildRange(range, dateValue) {
  const now = new Date();
  if (range === "day") {
    const date = dateValue ? new Date(`${dateValue}T00:00:00`) : new Date(now);
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    const end = new Date(date);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  }

  if (range === "month") {
    const [year, month] = (dateValue || "").split("-").map(Number);
    const base = year && month ? new Date(year, month - 1, 1) : new Date(now.getFullYear(), now.getMonth(), 1);
    const start = new Date(base);
    const end = new Date(base.getFullYear(), base.getMonth() + 1, 0, 23, 59, 59, 999);
    return { start, end };
  }

  const year = dateValue ? Number(dateValue) : now.getFullYear();
  const start = new Date(year, 0, 1);
  const end = new Date(year, 11, 31, 23, 59, 59, 999);
  return { start, end };
}

async function getDriverEarnings(req, res, next) {
  try {
    const driverId = req.user.user_id;
    const range = req.query.range || "day";
    const dateValue = req.query.date || null;

    if (![/^day$/, /^month$/, /^year$/].some((pattern) => pattern.test(range))) {
      return res.status(400).json({ success: false, message: "Invalid range" });
    }

    const { start, end } = buildRange(range, dateValue);

    const baseQuery = db("orders")
      .where({ driver_id: driverId, status: "completed" })
      .whereNotNull("completed_at")
      .whereBetween("completed_at", [start, end]);

    const totals = await baseQuery.clone().sum({ total_earning: "driver_earning" }).count({ order_count: "order_id" }).first();

    let breakdown = [];
    if (range === "month") {
      breakdown = await baseQuery
        .clone()
        .select(db.raw("DATE(completed_at) as bucket"))
        .sum({ total_earning: "driver_earning" })
        .count({ order_count: "order_id" })
        .groupByRaw("DATE(completed_at)")
        .orderBy("bucket", "asc");
    } else if (range === "year") {
      breakdown = await baseQuery
        .clone()
        .select(db.raw("DATE_FORMAT(completed_at, '%Y-%m') as bucket"))
        .sum({ total_earning: "driver_earning" })
        .count({ order_count: "order_id" })
        .groupByRaw("DATE_FORMAT(completed_at, '%Y-%m')")
        .orderBy("bucket", "asc");
    }

    return res.json({
      success: true,
      range,
      start,
      end,
      totals: {
        total_earning: Number(totals?.total_earning || 0),
        order_count: Number(totals?.order_count || 0)
      },
      breakdown
    });
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getDriverProfile,
  updateDriverProfile,
  updateDriverLocation,
  getDriverWallet,
  listAvailableOrders,
  listActiveOrders,
  listOrderHistory,
  acceptOrder,
  arrivePickup,
  pickupOrder,
  arriveDelivery,
  completeOrder,
  getDriverEarnings
};
