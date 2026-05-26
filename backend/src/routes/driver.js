const express = require("express");
const { authenticate, authorize } = require("../middleware/auth");
const { upload } = require("../middleware/upload");
const { validateBody } = require("../middleware/validate");
const { updateDriverProfileSchema, driverLocationSchema } = require("../validators/driver");
const {
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
} = require("../controllers/driver");

const router = express.Router();

router.use(authenticate);
router.use(authorize("driver"));

router.get("/profile", getDriverProfile);
router.put("/profile", validateBody(updateDriverProfileSchema), updateDriverProfile);
router.post("/location", validateBody(driverLocationSchema), updateDriverLocation);
router.get("/wallet", getDriverWallet);

router.get("/orders/available", listAvailableOrders);
router.get("/orders/active", listActiveOrders);
router.get("/orders/history", listOrderHistory);
router.post("/orders/:id/accept", acceptOrder);
router.post("/orders/:id/arrive-pickup", arrivePickup);
router.post("/orders/:id/pickup", upload.single("order_photo_pickup"), pickupOrder);
router.post("/orders/:id/arrive-delivery", arriveDelivery);
router.post("/orders/:id/complete", upload.single("order_photo_delivery"), completeOrder);

router.get("/earnings", getDriverEarnings);

module.exports = router;
