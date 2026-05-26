const db = require("../db/knex");
const { emitToDriver } = require("../socket");

async function persistNotifications(users, payload) {
  if (!Array.isArray(users) || users.length === 0) {
    return 0;
  }

  const rows = users
    .filter((user) => user?.user_id)
    .map((user) => ({
      user_id: user.user_id,
      title: payload.title,
      body: payload.body,
      type: payload.type ?? payload.data?.type ?? null,
      data_json: payload.data ? JSON.stringify(payload.data) : null,
      is_read: 0
    }));

  if (rows.length === 0) {
    return 0;
  }

  await db("notifications").insert(rows);
  return rows.length;
}

async function sendNotificationToUsers(users, payload) {
  let stored = 0;
  try {
    stored = await persistNotifications(users, payload);

    // Phát tín hiệu Realtime qua Socket.io vì không dùng Firebase
    if (stored > 0) {
      users.forEach((user) => {
        if (user.user_id) {
          emitToDriver(user.user_id, "new_db_notification", {
            title: payload.title,
            body: payload.body,
            data: payload.data
          });
        }
      });
    }
  } catch (error) {
    console.warn("Notification save failed:", error.message || error);
  }

  return { success: stored > 0, stored, sent: 0 };
}

module.exports = { sendNotificationToUsers };
