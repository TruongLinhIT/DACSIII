const express = require("express");
const { authenticate, authorize } = require("../middleware/auth");
const { upload } = require("../middleware/upload");
const { validateBody } = require("../middleware/validate");
const { createOrderSchema } = require("../validators/orders");
const {
  createOrder,
  getCustomerOrders,
  getOrderDetails,
  uploadOrderPhotoBefore,
  listAvailableOrders
} = require("../controllers/orders");

const router = express.Router();

router.use(authenticate); // Tất cả các route đơn hàng đều cần đăng nhập

router.post("/", validateBody(createOrderSchema), createOrder);
router.post("/photo-before", upload.single("order_photo_before"), uploadOrderPhotoBefore);
router.get("/available", authorize("driver"), listAvailableOrders);
router.get("/", getCustomerOrders);
router.get("/:id", getOrderDetails);

module.exports = router;
