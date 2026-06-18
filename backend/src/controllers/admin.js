const db = require("../db/knex");
const { sendNotificationToUsers } = require("../services/notify");

/**
 * @desc    Lấy danh sách tất cả người dùng (Có filter và search)
 * @route   GET /admin/users
 */
async function getAllUsers(req, res, next) {
  try {
    const { role, is_verified, is_locked, search } = req.query;

    let query = db("users").select(
      "user_id", "email", "full_name", "role", "is_verified", "is_locked", "created_at"
    );

    if (role) {
      query = query.where("role", role);
    }

    if (is_verified) {
      query = query.where("is_verified", is_verified);
    }

    if (is_locked !== undefined && is_locked !== null && is_locked !== "") {
      query = query.where("is_locked", Number(is_locked));
    }

    if (search) {
      query = query.where(function() {
        this.where("full_name", "like", `%${search}%`)
            .orWhere("email", "like", `%${search}%`);
      });
    }

    const users = await query.orderBy("created_at", "desc");
    const formattedUsers = users.map(u => ({
      ...u,
      is_locked: u.is_locked === 1
    }));
    res.json({ success: true, users: formattedUsers });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Duyệt hồ sơ định danh (eKYC)
 * @route   PUT /admin/users/:id/verify
 */
async function verifyUserIdentity(req, res, next) {
  try {
    const { id } = req.params;
    const { status, reason } = req.body; // status: 'verified' hoặc 'rejected'

    if (!["verified", "rejected"].includes(status)) {
      return res.status(400).json({ success: false, message: "Trạng thái không hợp lệ." });
    }

    if (status === "rejected" && (!reason || reason.trim().length < 3)) {
      return res.status(400).json({ success: false, message: "Vui lòng nhập lý do từ chối." });
    }

    const updatePayload = {
      is_verified: status,
      identity_reject_reason: status === "rejected" ? reason.trim() : null
    };
    await db("users").where({ user_id: id }).update(updatePayload);

    const user = await db("users")
      .select("fcm_token")
      .where({ user_id: id })
      .first();

    if (user) {
      const body =
        status === "verified"
          ? "Hồ sơ định danh đã được duyệt."
          : `Hồ sơ định danh bị từ chối: ${reason.trim()}`;
      await sendNotificationToUsers(
        [{ user_id: Number(id), fcm_token: user.fcm_token }],
        {
          title: "Kết quả định danh",
          body,
          data: {
            type: "identity_status",
            status,
            reason: status === "rejected" ? reason.trim() : ""
          }
        }
      );
    }

    res.json({ success: true, message: `Hồ sơ đã được cập nhật thành: ${status}` });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Lấy chi tiết hồ sơ người dùng để duyệt
 * @route   GET /admin/users/:id
 */
async function getUserDetail(req, res, next) {
  try {
    const { id } = req.params;
    const user = await db("users").where({ user_id: id }).first();

    if (!user) {
      return res.status(404).json({ success: false, message: "Không tìm thấy người dùng." });
    }

    // Không trả về password
    delete user.password;
    user.is_locked = user.is_locked === 1;

    res.json({ success: true, user });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Khóa tài khoản người dùng (customer/driver)
 * @route   PUT /admin/users/:id/lock
 */
async function lockUser(req, res, next) {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const user = await db("users").where({ user_id: id }).first();
    if (!user) {
      return res.status(404).json({ success: false, message: "Không tìm thấy người dùng." });
    }

    if (user.role === "admin") {
      return res.status(400).json({ success: false, message: "Không thể khóa tài khoản admin." });
    }

    if (user.is_locked === 1) {
      return res.status(400).json({ success: false, message: "Tài khoản đã bị khóa trước đó." });
    }

    await db("users").where({ user_id: id }).update({ is_locked: 1 });

    // Gửi thông báo cho user bị khóa
    await sendNotificationToUsers(
      [{ user_id: Number(id), fcm_token: user.fcm_token }],
      {
        title: "Tài khoản bị khóa",
        body: `Tài khoản của bạn đã bị khóa. Lý do: ${reason.trim()}`,
        data: {
          type: "account_locked",
          reason: reason.trim()
        }
      }
    );

    res.json({ success: true, message: "Đã khóa tài khoản thành công." });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Mở khóa tài khoản người dùng
 * @route   PUT /admin/users/:id/unlock
 */
async function unlockUser(req, res, next) {
  try {
    const { id } = req.params;

    const user = await db("users").where({ user_id: id }).first();
    if (!user) {
      return res.status(404).json({ success: false, message: "Không tìm thấy người dùng." });
    }

    if (user.is_locked === 0 || !user.is_locked) {
      return res.status(400).json({ success: false, message: "Tài khoản chưa bị khóa." });
    }

    await db("users").where({ user_id: id }).update({ is_locked: 0 });

    // Gửi thông báo cho user được mở khóa
    await sendNotificationToUsers(
      [{ user_id: Number(id), fcm_token: user.fcm_token }],
      {
        title: "Tài khoản đã được mở khóa",
        body: "Tài khoản của bạn đã được mở khóa. Bạn có thể đăng nhập lại.",
        data: {
          type: "account_unlocked"
        }
      }
    );

    res.json({ success: true, message: "Đã mở khóa tài khoản thành công." });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Hủy duyệt eKYC (reset về unverified, xóa ảnh)
 * @route   PUT /admin/users/:id/revoke-ekyc
 */
async function revokeEkyc(req, res, next) {
  try {
    const { id } = req.params;
    const { reason } = req.body;

    const user = await db("users").where({ user_id: id }).first();
    if (!user) {
      return res.status(404).json({ success: false, message: "Không tìm thấy người dùng." });
    }

    if (user.is_verified !== "verified") {
      return res.status(400).json({ success: false, message: "Chỉ có thể hủy duyệt hồ sơ đã được xác minh." });
    }

    await db("users").where({ user_id: id }).update({
      is_verified: "unverified",
      id_card_front_url: null,
      id_card_back_url: null,
      portrait_url: null,
      cccd_number: null,
      identity_reject_reason: `Hủy duyệt: ${reason.trim()}`
    });

    // Gửi thông báo
    await sendNotificationToUsers(
      [{ user_id: Number(id), fcm_token: user.fcm_token }],
      {
        title: "Hồ sơ eKYC bị hủy duyệt",
        body: `Hồ sơ định danh của bạn đã bị hủy duyệt. Lý do: ${reason.trim()}. Vui lòng gửi lại hồ sơ.`,
        data: {
          type: "ekyc_revoked",
          reason: reason.trim()
        }
      }
    );

    res.json({ success: true, message: "Đã hủy duyệt eKYC thành công." });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Thống kê tiền chiết khấu đã thu
 * @route   GET /admin/commission-summary
 */
async function getCommissionSummary(req, res, next) {
  try {
    const range = req.query.range || "day";
    const dateValue = req.query.date || null;

    if (![/^day$/, /^month$/, /^year$/].some((pattern) => pattern.test(range))) {
      return res.status(400).json({ success: false, message: "Invalid range" });
    }

    const { start, end } = buildRange(range, dateValue);

    const baseQuery = db("revenue_logs")
      .where({ log_type: "commission" })
      .whereBetween("created_at", [start, end]);

    const totals = await baseQuery
      .clone()
      .sum({ total_commission: "amount" })
      .count({ order_count: "order_id" })
      .first();

    let breakdown = [];
    if (range === "month") {
      breakdown = await baseQuery
        .clone()
        .select(db.raw("DATE(created_at) as bucket"))
        .sum({ total_commission: "amount" })
        .count({ order_count: "order_id" })
        .groupByRaw("DATE(created_at)")
        .orderBy("bucket", "asc");
    } else if (range === "year") {
      breakdown = await baseQuery
        .clone()
        .select(db.raw("DATE_FORMAT(created_at, '%Y-%m') as bucket"))
        .sum({ total_commission: "amount" })
        .count({ order_count: "order_id" })
        .groupByRaw("DATE_FORMAT(created_at, '%Y-%m')")
        .orderBy("bucket", "asc");
    }

    return res.json({
      success: true,
      range,
      start,
      end,
      totals: {
        total_commission: Number(totals?.total_commission || 0),
        order_count: Number(totals?.order_count || 0)
      },
      breakdown
    });
  } catch (error) {
    next(error);
  }
}

function buildRange(range, dateValue) {
  const now = new Date();
  if (range === "day") {
    const date = dateValue ? new Date(`${dateValue}T00:00:00`) : new Date(now);
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    const end = new Date(date);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  }

  if (range === "month") {
    const [year, month] = (dateValue || "").split("-").map(Number);
    const base = year && month ? new Date(year, month - 1, 1) : new Date(now.getFullYear(), now.getMonth(), 1);
    const start = new Date(base);
    const end = new Date(base.getFullYear(), base.getMonth() + 1, 0, 23, 59, 59, 999);
    return { start, end };
  }

  const year = dateValue ? Number(dateValue) : now.getFullYear();
  const start = new Date(year, 0, 1);
  const end = new Date(year, 11, 31, 23, 59, 59, 999);
  return { start, end };
}

module.exports = { getAllUsers, verifyUserIdentity, getUserDetail, lockUser, unlockUser, revokeEkyc, getCommissionSummary };