const db = require("../db/knex");
const { sendToTokens, isFirebaseReady } = require("./firebase");

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
  let sent = 0;

  try {
    stored = await persistNotifications(users, payload);

    if (isFirebaseReady()) {
      const tokens = [];
      const userIdsToFetch = [];

      for (const user of users) {
        if (user.fcm_token) {
          tokens.push(user.fcm_token);
        } else if (user.user_id) {
          userIdsToFetch.push(user.user_id);
        }
      }

      if (userIdsToFetch.length > 0) {
        const dbUsers = await db("users")
          .select("user_id", "fcm_token")
          .whereIn("user_id", userIdsToFetch);
        for (const u of dbUsers) {
          if (u.fcm_token) {
            tokens.push(u.fcm_token);
          }
        }
      }

      const uniqueTokens = [...new Set(tokens)];
      if (uniqueTokens.length > 0) {
        const response = await sendToTokens(
          uniqueTokens,
          { title: payload.title, body: payload.body },
          payload.data
        );
        sent = response.successCount || 0;
      }
    } else {
      console.warn("FCM not ready. Push notification skipped.");
    }
  } catch (error) {
    console.warn("Notification sending or saving failed:", error.message || error);
  }

  return { success: stored > 0 || sent > 0, stored, sent };
}

module.exports = { sendNotificationToUsers };
