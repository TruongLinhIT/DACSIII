const db = require("../db/knex");

async function getNotifications(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { status = "all", limit = "50", offset = "0" } = req.query;
    const safeLimit = Math.min(Number.parseInt(limit, 10) || 50, 100);
    const safeOffset = Math.max(Number.parseInt(offset, 10) || 0, 0);

    let query = db("notifications").where({ user_id: userId });
    if (status === "unread") {
      query = query.where({ is_read: 0 });
    }

    const notifications = await query
      .orderBy("created_at", "desc")
      .limit(safeLimit)
      .offset(safeOffset);

    return res.json({ success: true, notifications });
  } catch (error) {
    return next(error);
  }
}

async function markNotificationRead(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { id } = req.params;

    const updated = await db("notifications")
      .where({ notification_id: id, user_id: userId })
      .update({ is_read: 1 });

    if (!updated) {
      return res.status(404).json({ success: false, message: "Không tìm thấy thông báo." });
    }

    return res.json({ success: true, message: "Đã đánh dấu đã đọc." });
  } catch (error) {
    return next(error);
  }
}

async function markAllRead(req, res, next) {
  try {
    const userId = req.user.user_id;

    await db("notifications")
      .where({ user_id: userId, is_read: 0 })
      .update({ is_read: 1 });

    return res.json({ success: true, message: "Đã đánh dấu tất cả đã đọc." });
  } catch (error) {
    return next(error);
  }
}

module.exports = { getNotifications, markNotificationRead, markAllRead };

