const express = require("express");
const { authenticate, authorize } = require("../middleware/auth");
const { validateBody } = require("../middleware/validate");
const { createReportSchema } = require("../validators/reports");
const {
  createReport,
  getAllReports,
  getReportDetail,
  resolveReport
} = require("../controllers/reports");

const router = express.Router();

// Tất cả các route đều yêu cầu đăng nhập
router.use(authenticate);

// Khách hàng gửi báo cáo
router.post("/", validateBody(createReportSchema), createReport);

// Các route của admin yêu cầu quyền admin
router.get("/", authorize("admin"), getAllReports);
router.get("/:id", authorize("admin"), getReportDetail);
router.put("/:id/resolve", authorize("admin"), resolveReport);

module.exports = router;
