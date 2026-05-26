const db = require("../db/knex");
const { sendNotificationToUsers } = require("../services/notify");

/**
 * @desc    Lấy danh sách tất cả người dùng (Có filter và search)
 * @route   GET /admin/users
 */
async function getAllUsers(req, res, next) {
  try {
    const { role, is_verified, search } = req.query;

    let query = db("users").select(
      "user_id", "email", "full_name", "role", "is_verified", "created_at"
    );

    if (role) {
      query = query.where("role", role);
    }

    if (is_verified) {
      query = query.where("is_verified", is_verified);
    }

    if (search) {
      query = query.where(function() {
        this.where("full_name", "like", `%${search}%`)
            .orWhere("email", "like", `%${search}%`);
      });
    }

    const users = await query.orderBy("created_at", "desc");
    res.json({ success: true, users });
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

    res.json({ success: true, user });
  } catch (error) {
    next(error);
  }
}

module.exports = { getAllUsers, verifyUserIdentity, getUserDetail };
