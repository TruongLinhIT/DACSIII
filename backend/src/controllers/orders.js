const db = require("../db/knex");
const path = require("path");
const { uploadRoot } = require("../middleware/upload");
const { sendNotificationToUsers } = require("../services/notify");
const { emitToDriver, getNearbyDrivers } = require("../socket");

/**
 * @desc    Tạo đơn hàng mới
 */
async function createOrder(req, res, next) {
  try {
    const customerId = req.user.user_id;
    const {
      package_type,
      weight_kg,
      order_description,
      pickup_address,
      delivery_address,
      pickup_lat,
      pickup_lng,
      delivery_lat,
      delivery_lng,
      photo_before_booking,
      sender_name,
      sender_phone,
      recipient_name,
      recipient_phone,
      pickup_note,
      delivery_note,
      package_size,
      cod_amount,
      payment_method
    } = req.body;

    const pricingKeys = ["commission_rate", "base_fare", "price_per_km", "price_per_kg"];
    const settings = await db("system_settings").whereIn("setting_key", pricingKeys);
    const settingsMap = settings.reduce((acc, row) => {
      acc[row.setting_key] = row.setting_value;
      return acc;
    }, {});

    const commissionRate = parseFloat(settingsMap.commission_rate ?? 0.2);
    const baseFare = parseFloat(settingsMap.base_fare ?? 10000);
    const pricePerKm = parseFloat(settingsMap.price_per_km ?? 5000);
    const pricePerKg = parseFloat(settingsMap.price_per_kg ?? 2000);

    const distance_km = calculateDistanceKm(pickup_lat, pickup_lng, delivery_lat, delivery_lng);
    const total_price = calculateTotalPrice(distance_km, weight_kg, baseFare, pricePerKm, pricePerKg);
    const app_commission = total_price * commissionRate;
    const driver_earning = total_price - app_commission;

    const [order_id] = await db("orders").insert({
      customer_id: customerId,
      package_type,
      weight_kg,
      order_description,
      pickup_address,
      delivery_address,
      pickup_lat,
      pickup_lng,
      delivery_lat,
      delivery_lng,
      distance_km,
      total_price,
      app_commission,
      driver_earning,
      photo_before_booking,
      sender_name,
      sender_phone,
      recipient_name,
      recipient_phone,
      pickup_note,
      delivery_note,
      package_size,
      cod_amount,
      payment_method,
      status: "pending"
    });

    // Notify drivers
    const distanceSql = db.raw(
      "(6371 * acos(cos(radians(?)) * cos(radians(drivers.current_lat)) * cos(radians(drivers.current_lng) - radians(?)) + sin(radians(?)) * sin(radians(drivers.current_lat))))",
      [pickup_lat, pickup_lng, pickup_lat]
    );
    const distanceQuery = distanceSql.toSQL();

    const nearbyDrivers = await db("drivers")
      .join("users", "drivers.driver_id", "users.user_id")
      .select("drivers.driver_id", "users.fcm_token")
      .whereNotNull("drivers.current_lat")
      .whereNotNull("drivers.current_lng")
      .where("drivers.is_online", 1)
      .whereRaw(`${distanceQuery.sql} <= ?`, [...distanceQuery.bindings, 3]);

    const notifyDrivers = nearbyDrivers.map((driver) => ({
      user_id: driver.driver_id,
      fcm_token: driver.fcm_token
    }));

    if (notifyDrivers.length > 0) {
      await sendNotificationToUsers(notifyDrivers, {
        title: "Có đơn hàng mới gần bạn",
        body: `Đơn #${order_id} gần vị trí của bạn.`,
        data: {
          type: "new_order",
          order_id: String(order_id)
        }
      });
    }

    const customer = await db("users").select("full_name", "phone").where({ user_id: customerId }).first();

    const orderPayload = {
      order_id,
      package_type,
      weight_kg,
      pickup_address,
      delivery_address,
      pickup_lat,
      pickup_lng,
      delivery_lat,
      delivery_lng,
      distance_km,
      total_price,
      driver_earning,
      status: "pending",
      created_at: new Date().toISOString(),
      customer_name: customer?.full_name ?? null,
      customer_phone: customer?.phone ?? null
    };

    const socketDrivers = getNearbyDrivers(Number(pickup_lat), Number(pickup_lng), 3);
    socketDrivers.forEach((driver) => {
      emitToDriver(driver.driver_id, "new_order_nearby", {
        order: orderPayload,
        distance_from_driver: driver.distance_km
      });
    });

    res.status(201).json({
      success: true,
      message: "Đơn hàng đã được tạo thành công.",
      order_id
    });
  } catch (error) {
    next(error);
  }
}

async function getCustomerOrders(req, res, next) {
  try {
    const customerId = req.user.user_id;
    const orders = await db("orders")
      .where({ customer_id: customerId })
      .orderBy("created_at", "desc");
    res.json({ success: true, orders });
  } catch (error) {
    next(error);
  }
}

async function getOrderDetails(req, res, next) {
  try {
    const { id } = req.params;
    const order = await db("orders").where({ order_id: id }).first();
    if (!order) return res.status(404).json({ success: false, message: "Không tìm thấy đơn hàng." });

    let driver = null;
    if (order.driver_id) {
      const driverUser = await db("users").select("user_id", "full_name", "phone", "avatar_url").where({ user_id: order.driver_id }).first();
      const driverProfile = await db("drivers").select("vehicle_type", "license_plate", "rating_avg").where({ driver_id: order.driver_id }).first();
      if (driverUser) {
        driver = { ...driverUser, ...driverProfile };
      }
    }
    res.json({ success: true, order, driver });
  } catch (error) {
    next(error);
  }
}

async function listAvailableOrders(req, res, next) {
  try {
    const lat = Number(req.query.lat);
    const lng = Number(req.query.lng);
    const hasLocation = Number.isFinite(lat) && Number.isFinite(lng);

    let query = db("orders")
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
      );

    if (hasLocation) {
      const distanceSql = db.raw(
        "(6371 * acos(cos(radians(?)) * cos(radians(orders.pickup_lat)) * cos(radians(orders.pickup_lng) - radians(?)) + sin(radians(?)) * sin(radians(orders.pickup_lat))))",
        [lat, lng, lat]
      );
      query = query.select({ distance_from_driver: distanceSql });
    } else {
      query = query.select(db.raw("NULL as distance_from_driver"));
    }

    const orders = await query
      .where({ "orders.status": "pending" })
      .whereNull("orders.driver_id")
      .orderBy("orders.created_at", "desc");

    res.json({ success: true, orders });
  } catch (error) {
    next(error);
  }
}

function calculateDistanceKm(lat1, lng1, lat2, lng2) {
  const earthRadiusKm = 6371;
  const toRadians = (v) => (v * Math.PI) / 180;
  const dLat = toRadians(lat2 - lat1);
  const dLng = toRadians(lng2 - lng1);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.sin(dLng / 2) ** 2;
  return Math.round(earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) * 100) / 100;
}

function calculateTotalPrice(dist, weight, base, pKm, pKg) {
  return Math.round((base + dist * pKm + weight * pKg) * 100) / 100;
}

async function uploadOrderPhotoBefore(req, res, next) {
  try {
    if (!req.file) return res.status(400).json({ success: false, message: "Missing order photo" });
    const relativePath = path.relative(uploadRoot, req.file.path).split(path.sep).join("/");
    return res.json({ success: true, message: "Order photo uploaded", photo_url: `/uploads/${relativePath}` });
  } catch (error) {
    next(error);
  }
}

module.exports = { createOrder, getCustomerOrders, getOrderDetails, uploadOrderPhotoBefore, listAvailableOrders };
