const { Server } = require("socket.io");
const db = require("./db/knex");

let io = null;
const driverLocations = new Map();

const DRIVER_STALE_MS = Number(process.env.DRIVER_STALE_MS ?? 60000);
const DRIVER_PERSIST_MS = Number(process.env.DRIVER_PERSIST_MS ?? 5000);
const DRIVER_MIN_MOVE_KM = Number(process.env.DRIVER_MIN_MOVE_KM ?? 0.05);

function isValidLatitude(value) {
  return Number.isFinite(value) && Math.abs(value) <= 90;
}

function isValidLongitude(value) {
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

function updateDriverLocation(driverId, lat, lng, socketId) {
  if (!driverId || !isValidLatitude(lat) || !isValidLongitude(lng)) {
    return null;
  }

  const id = String(driverId);
  const existing = driverLocations.get(id) || {};
  const next = {
    socketId: socketId ?? existing.socketId ?? null,
    lat,
    lng,
    updatedAt: Date.now(),
    persistedLat: existing.persistedLat ?? null,
    persistedLng: existing.persistedLng ?? null,
    lastPersistedAt: existing.lastPersistedAt ?? 0
  };

  driverLocations.set(id, next);
  return next;
}

function shouldPersistLocation(info, now) {
  if (!info) {
    return false;
  }

  if (!info.lastPersistedAt || !Number.isFinite(info.persistedLat) || !Number.isFinite(info.persistedLng)) {
    return true;
  }

  if (now - info.lastPersistedAt >= DRIVER_PERSIST_MS) {
    return true;
  }

  const movedKm = calculateDistanceKm(info.persistedLat, info.persistedLng, info.lat, info.lng);
  return Number.isFinite(movedKm) && movedKm >= DRIVER_MIN_MOVE_KM;
}

async function persistDriverLocationIfOnline(driverId, info) {
  if (!driverId || !info) {
    return false;
  }

  try {
    const updated = await db("drivers")
      .where({ driver_id: driverId, is_online: 1 })
      .update({ current_lat: info.lat, current_lng: info.lng });

    if (!updated) {
      return false;
    }

    const current = driverLocations.get(String(driverId));
    if (current) {
      current.persistedLat = info.lat;
      current.persistedLng = info.lng;
      current.lastPersistedAt = Date.now();
      driverLocations.set(String(driverId), current);
    }

    return true;
  } catch (error) {
    console.warn("Failed to persist driver location:", error.message || error);
    return false;
  }
}

async function persistDriverLocation(driverId, info, isOnline = 1) {
  const now = Date.now();
  if (!shouldPersistLocation(info, now)) {
    return;
  }

  try {
    const payload = {
      current_lat: info.lat,
      current_lng: info.lng,
      is_online: isOnline
    };

    const existing = await db("drivers").where({ driver_id: driverId }).first();
    if (existing) {
      await db("drivers").where({ driver_id: driverId }).update(payload);
    } else {
      await db("drivers").insert({
        driver_id: driverId,
        license_plate: "",
        vehicle_type: "Motorbike",
        wallet_balance: 0,
        rating_avg: 5,
        ...payload
      });
    }

    const current = driverLocations.get(String(driverId));
    if (current) {
      current.persistedLat = info.lat;
      current.persistedLng = info.lng;
      current.lastPersistedAt = now;
      driverLocations.set(String(driverId), current);
    }
  } catch (error) {
    console.warn("Failed to persist driver location:", error.message || error);
  }
}

async function setDriverOffline(driverId) {
  if (!driverId) {
    return;
  }

  try {
    await db("drivers").where({ driver_id: driverId }).update({ is_online: 0 });
  } catch (error) {
    console.warn("Failed to set driver offline:", error.message || error);
  }
}

function removeDriverLocation(driverId) {
  if (driverId) {
    driverLocations.delete(String(driverId));
  }
}

function removeDriverBySocket(socketId) {
  const removed = [];
  for (const [driverId, info] of driverLocations.entries()) {
    if (info.socketId === socketId) {
      driverLocations.delete(driverId);
      removed.push(driverId);
    }
  }
  return removed;
}

function initSocket(httpServer) {
  io = new Server(httpServer, {
    cors: {
      origin: "*"
    }
  });

  io.on("connection", (socket) => {
    socket.on("driver:online", async (payload = {}) => {
      const { driver_id, lat, lng } = payload;
      const info = updateDriverLocation(driver_id, Number(lat), Number(lng), socket.id);
      if (driver_id) {
        socket.join(`driver:${driver_id}`);
      }
      await persistDriverLocation(driver_id, info, 1);
    });

    socket.on("update_location", async (payload = {}) => {
      const { driver_id, lat, lng, is_online } = payload;
      const info = updateDriverLocation(driver_id, Number(lat), Number(lng), socket.id);
      if (driver_id) {
        socket.join(`driver:${driver_id}`);
      }
      if (is_online === 1) {
        await persistDriverLocation(driver_id, info, 1);
      } else {
        await persistDriverLocationIfOnline(driver_id, info);
      }
    });

    socket.on("driver:location", async (payload = {}) => {
      const { driver_id, lat, lng, is_online } = payload;
      const isOnline = is_online === 0 ? 0 : 1;

      if (isOnline === 0 && driver_id) {
        removeDriverLocation(driver_id);
        socket.leave(`driver:${driver_id}`);
        await setDriverOffline(driver_id);
        return;
      }

      const info = updateDriverLocation(driver_id, Number(lat), Number(lng), socket.id);
      if (driver_id) {
        socket.join(`driver:${driver_id}`);
      }
      await persistDriverLocation(driver_id, info, isOnline);
    });

    socket.on("driver:offline", async (payload = {}) => {
      const { driver_id } = payload;
      if (driver_id) {
        removeDriverLocation(driver_id);
        socket.leave(`driver:${driver_id}`);
        await setDriverOffline(driver_id);
      }
    });

    socket.on("disconnect", async () => {
      const offlineDriverIds = removeDriverBySocket(socket.id);
      await Promise.all(offlineDriverIds.map((driverId) => setDriverOffline(driverId)));
    });
  });

  return io;
}

function getNearbyDrivers(pickupLat, pickupLng, radiusKm) {
  if (!isValidLatitude(pickupLat) || !isValidLongitude(pickupLng)) {
    return [];
  }

  const nearby = [];
  const now = Date.now();
  for (const [driverId, info] of driverLocations.entries()) {
    if (!isValidLatitude(info.lat) || !isValidLongitude(info.lng)) {
      continue;
    }

    if (now - info.updatedAt > DRIVER_STALE_MS) {
      driverLocations.delete(driverId);
      continue;
    }

    const distanceKm = normalizeDistanceKm(
      calculateDistanceKm(pickupLat, pickupLng, info.lat, info.lng)
    );

    if (distanceKm !== null && distanceKm <= radiusKm) {
      nearby.push({ driver_id: Number(driverId), distance_km: distanceKm });
    }
  }

  return nearby;
}

function trackDriverLocation(driverId, lat, lng) {
  return updateDriverLocation(driverId, lat, lng, null);
}

function emitToDriver(driverId, event, payload) {
  if (!io || !driverId) {
    return;
  }

  io.to(`driver:${driverId}`).emit(event, payload);
}

module.exports = {
  initSocket,
  getNearbyDrivers,
  emitToDriver,
  trackDriverLocation,
  removeDriverLocation
};
