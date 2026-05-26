const { Server } = require("socket.io");

let io = null;
const driverLocations = new Map();

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
  if (!driverId || !Number.isFinite(lat) || !Number.isFinite(lng)) {
    return;
  }

  driverLocations.set(String(driverId), {
    socketId,
    lat,
    lng,
    updatedAt: Date.now()
  });
}

function removeDriverBySocket(socketId) {
  for (const [driverId, info] of driverLocations.entries()) {
    if (info.socketId === socketId) {
      driverLocations.delete(driverId);
    }
  }
}

function initSocket(httpServer) {
  io = new Server(httpServer, {
    cors: {
      origin: "*"
    }
  });

  io.on("connection", (socket) => {
    socket.on("driver:online", (payload = {}) => {
      const { driver_id, lat, lng } = payload;
      updateDriverLocation(driver_id, Number(lat), Number(lng), socket.id);
      if (driver_id) {
        socket.join(`driver:${driver_id}`);
      }
    });

    socket.on("driver:location", (payload = {}) => {
      const { driver_id, lat, lng } = payload;
      updateDriverLocation(driver_id, Number(lat), Number(lng), socket.id);
    });

    socket.on("driver:offline", (payload = {}) => {
      const { driver_id } = payload;
      if (driver_id) {
        driverLocations.delete(String(driver_id));
        socket.leave(`driver:${driver_id}`);
      }
    });

    socket.on("disconnect", () => {
      removeDriverBySocket(socket.id);
    });
  });

  return io;
}

function getNearbyDrivers(pickupLat, pickupLng, radiusKm) {
  if (!Number.isFinite(pickupLat) || !Number.isFinite(pickupLng)) {
    return [];
  }

  const nearby = [];
  for (const [driverId, info] of driverLocations.entries()) {
    if (!Number.isFinite(info.lat) || !Number.isFinite(info.lng)) {
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

function emitToDriver(driverId, event, payload) {
  if (!io || !driverId) {
    return;
  }

  io.to(`driver:${driverId}`).emit(event, payload);
}

module.exports = {
  initSocket,
  getNearbyDrivers,
  emitToDriver
};

