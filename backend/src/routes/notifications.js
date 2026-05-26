const express = require("express");
const { authenticate } = require("../middleware/auth");
const {
  getNotifications,
  markNotificationRead,
  markAllRead
} = require("../controllers/notifications");

const router = express.Router();

router.get("/", authenticate, getNotifications);
router.put("/read-all", authenticate, markAllRead);
router.put("/:id/read", authenticate, markNotificationRead);

module.exports = router;

