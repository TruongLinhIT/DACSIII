const path = require("path");
const db = require("../db/knex");
const crypto = require("crypto"); // Thêm crypto để băm mật khẩu
const { uploadRoot } = require("../middleware/upload");
const { createOtp, hashOtp, isOtpExpired } = require("../utils/otp");
const { sendEmailOtp } = require("../services/email");

function toPublicUrl(filePath) {
  const relativePath = path.relative(uploadRoot, filePath).split(path.sep).join("/");
  return `/uploads/${relativePath}`;
}

// Hàm băm mật khẩu đơn giản sử dụng SHA256 (nên dùng bcrypt nếu có thể)
function hashPassword(password) {
  const secret = process.env.PASSWORD_SECRET || "default_pwd_secret";
  return crypto.createHmac("sha256", secret).update(password).digest("hex");
}

async function getMe(req, res, next) {
  try {
    const userId = req.user.user_id;
    const user = await db("users")
      .select(
        "user_id",
        "phone",
        "full_name",
        "email",
        "role",
        "avatar_url",
        "cccd_number",
        "id_card_front_url",
        "id_card_back_url",
        "portrait_url",
        "is_verified",
        "identity_reject_reason",
        "created_at",
        "updated_at"
      )
      .where({ user_id: userId })
      .first();

    if (!user) {
      return res.status(404).json({ success: false, message: "User not found" });
    }

    return res.json({ success: true, user });
  } catch (error) {
    return next(error);
  }
}

async function updateProfile(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { full_name, email, cccd_number, avatar_url, password } = req.body;

    const payload = {};

    if (full_name !== undefined) {
      payload.full_name = full_name;
    }
    if (email !== undefined) {
      payload.email = email || null;
    }
    if (cccd_number !== undefined) {
      payload.cccd_number = cccd_number || null;
    }
    if (avatar_url !== undefined) {
      payload.avatar_url = avatar_url || null;
    }

    if (password) {
      const currentUser = await db("users")
        .select("password")
        .where({ user_id: userId })
        .first();
      const hasPassword = currentUser?.password && currentUser.password.trim().length > 0;

      if (hasPassword) {
        return res
          .status(400)
          .json({ success: false, message: "Vui lòng đổi mật khẩu bằng OTP." });
      }

      payload.password = hashPassword(password);
    }

    const updated = await db("users").where({ user_id: userId }).update(payload);
    if (!updated) {
      return res.status(404).json({ success: false, message: "User not found" });
    }

    return res.json({ success: true, message: "Profile updated" });
  } catch (error) {
    if (error && error.code === "ER_DUP_ENTRY") {
      return res.status(409).json({ success: false, message: "CCCD already exists" });
    }
    return next(error);
  }
}

async function uploadIdentity(req, res, next) {
  try {
    const userId = req.user.user_id;
    const front = req.files?.id_card_front?.[0];
    const back = req.files?.id_card_back?.[0];
    const portrait = req.files?.portrait?.[0];

    if (!front || !back || !portrait) {
      return res.status(400).json({ success: false, message: "Missing identity images" });
    }

    const update = {
      id_card_front_url: toPublicUrl(front.path),
      id_card_back_url: toPublicUrl(back.path),
      portrait_url: toPublicUrl(portrait.path),
      is_verified: "pending",
      identity_reject_reason: null
    };

    await db("users").where({ user_id: userId }).update(update);

    return res.json({
      success: true,
      message: "Gửi định danh thành công.",
      files: update
    });
  } catch (error) {
    return next(error);
  }
}

async function uploadAvatar(req, res, next) {
  try {
    const userId = req.user.user_id;
    const file = req.file;

    if (!file) {
      return res.status(400).json({ success: false, message: "Missing avatar file" });
    }

    const avatarUrl = toPublicUrl(file.path);
    await db("users").where({ user_id: userId }).update({ avatar_url: avatarUrl });

    return res.json({ success: true, message: "Avatar uploaded", avatar_url: avatarUrl });
  } catch (error) {
    return next(error);
  }
}

async function sendChangePasswordOtp(req, res, next) {
  try {
    const userId = req.user.user_id;
    const user = await db("users").select("email").where({ user_id: userId }).first();

    if (!user?.email) {
      return res.status(400).json({ success: false, message: "Vui lòng cập nhật email trước." });
    }

    const { otp, otpHash, expiresAt } = createOtp();
    await db("users")
      .where({ user_id: userId })
      .update({ otp_code: otpHash, otp_expiry: expiresAt });

    await sendEmailOtp(user.email, otp);

    return res.json({ success: true, message: "Mã OTP đã được gửi đến email của bạn." });
  } catch (error) {
    return next(error);
  }
}

async function changePasswordWithOtp(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { otp, newPassword } = req.body;
    const user = await db("users")
      .select("otp_code", "otp_expiry", "email")
      .where({ user_id: userId })
      .first();

    if (!user?.email) {
      return res.status(400).json({ success: false, message: "Vui lòng cập nhật email trước." });
    }

    if (!user.otp_code || !user.otp_expiry) {
      return res.status(400).json({ success: false, message: "Vui lòng yêu cầu OTP trước." });
    }

    if (isOtpExpired(user.otp_expiry)) {
      return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn." });
    }

    if (hashOtp(otp) !== user.otp_code) {
      return res.status(400).json({ success: false, message: "Mã OTP không chính xác." });
    }

    await db("users")
      .where({ user_id: userId })
      .update({
        password: hashPassword(newPassword),
        otp_code: null,
        otp_expiry: null
      });

    return res.json({ success: true, message: "Đổi mật khẩu thành công." });
  } catch (error) {
    return next(error);
  }
}

async function updateDeviceToken(req, res, next) {
  try {
    const userId = req.user.user_id;
    const { token } = req.body;

    await db("users").where({ user_id: userId }).update({ fcm_token: token });

    return res.json({ success: true, message: "Đã cập nhật token thiết bị." });
  } catch (error) {
    return next(error);
  }
}

module.exports = {
  getMe,
  updateProfile,
  uploadIdentity,
  uploadAvatar,
  sendChangePasswordOtp,
  changePasswordWithOtp,
  updateDeviceToken
};
