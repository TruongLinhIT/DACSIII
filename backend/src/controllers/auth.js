const db = require("../db/knex");
const crypto = require("crypto");
const { createOtp, hashOtp, isOtpExpired } = require("../utils/otp");
const { sendEmailOtp } = require("../services/email");
const { signJwt } = require("../utils/jwt");

function hashPassword(password) {
  const secret = process.env.PASSWORD_SECRET || "default_pwd_secret";
  return crypto.createHmac("sha256", secret).update(password).digest("hex");
}

/**
 * @desc    Đăng nhập bằng Email và Mật khẩu
 * @route   POST /auth/login
 */
async function login(req, res, next) {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: "Vui lòng nhập đầy đủ email và mật khẩu." });
  }
  const emailLower = email.toLowerCase().trim();

  try {
    const user = await db("users")
      .where({ email: emailLower })
      .orderBy('user_id', 'desc')
      .first();

    if (!user) {
      return res.status(401).json({ success: false, message: "Email hoặc mật khẩu không chính xác." });
    }

    if (!user.password) {
      return res.status(400).json({
        success: false,
        message: "Tài khoản này chưa thiết lập mật khẩu. Vui lòng đăng nhập qua OTP để thiết lập."
      });
    }

    const hashedInput = hashPassword(password);
    if (hashedInput !== user.password) {
      return res.status(401).json({ success: false, message: "Email hoặc mật khẩu không chính xác." });
    }

    const token = signJwt({
      user_id: user.user_id,
      email: user.email,
      role: user.role || "customer"
    });

    res.json({
      success: true,
      message: "Đăng nhập thành công.",
      token,
      user: {
        user_id: user.user_id,
        email: user.email,
        role: user.role || "customer"
      }
    });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Đặt lại mật khẩu bằng OTP
 * @route   POST /auth/reset-password
 */
async function resetPassword(req, res, next) {
  const { email, otp, newPassword } = req.body;
  const emailLower = email.toLowerCase().trim();

  try {
    const user = await db("users")
      .where({ email: emailLower })
      .orderBy('user_id', 'desc')
      .first();

    if (!user || !user.otp_code || !user.otp_expiry) {
      return res.status(400).json({ success: false, message: "Yêu cầu mã OTP trước khi đặt lại mật khẩu." });
    }

    if (isOtpExpired(user.otp_expiry)) {
      return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn." });
    }

    if (hashOtp(otp) !== user.otp_code) {
      return res.status(400).json({ success: false, message: "Mã OTP không chính xác." });
    }

    await db("users")
      .where({ user_id: user.user_id })
      .update({
        password: hashPassword(newPassword),
        otp_code: null,
        otp_expiry: null
      });

    res.json({
      success: true,
      message: "Mật khẩu đã được thay đổi thành công. Vui lòng đăng nhập lại."
    });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Gửi mã OTP qua Email
 * @route   POST /auth/send-otp
 */
async function sendOtp(req, res, next) {
  const { email } = req.body;
  if (!email) return res.status(400).json({ success: false, message: "Email là bắt buộc." });

  const emailLower = email.toLowerCase().trim();
  console.log(`[OTP] Request send-otp for: "${emailLower}"`);

  try {
    const { otp, otpHash, expiresAt } = createOtp();

    // Tìm user gần nhất có email này
    const existingUser = await db("users")
      .where({ email: emailLower })
      .orderBy('user_id', 'desc')
      .first();

    if (existingUser) {
      console.log(`[OTP] Updating existing user ID: ${existingUser.user_id}`);
      await db("users")
        .where({ user_id: existingUser.user_id })
        .update({
          otp_code: otpHash,
          otp_expiry: expiresAt
        });
    } else {
      console.log(`[OTP] Creating new temporary user for: ${emailLower}`);
      // Tạo phone ngẫu nhiên 10 số (tránh vượt quá varchar(15) và tránh trùng UNIQUE)
      const tempPhone = '0' + Math.floor(100000000 + Math.random() * 900000000).toString();
      await db("users").insert({
        email: emailLower,
        otp_code: otpHash,
        otp_expiry: expiresAt,
        phone: tempPhone,
        password: hashPassword('temp_pass_' + Date.now()),
        role: "customer",
        is_verified: "unverified"
      });
    }

    await sendEmailOtp(emailLower, otp);
    console.log(`[OTP] Successfully sent to ${emailLower}: ${otp}`);

    res.json({ success: true, message: "Mã OTP đã được gửi đến email của bạn." });
  } catch (error) {
    console.error("[OTP] Error in sendOtp:", error.message);
    res.status(500).json({ success: false, message: "Lỗi hệ thống: " + error.message });
  }
}

/**
 * @desc    Xác thực mã OTP
 * @route   POST /auth/verify-otp
 */
async function verifyOtp(req, res, next) {
  const { email, otp } = req.body;
  if (!email || !otp) {
    return res.status(400).json({ success: false, message: "Email và mã OTP là bắt buộc." });
  }

  const emailLower = email.toLowerCase().trim();
  console.log(`[OTP] Request verify-otp for: "${emailLower}", code: "${otp}"`);

  try {
    // Luôn lấy bản ghi mới nhất của email này để đảm bảo khớp với OTP vừa gửi
    const user = await db("users")
      .where({ email: emailLower })
      .orderBy('user_id', 'desc')
      .first();

    if (!user) {
      console.log(`[OTP] Verify failed: User not found for ${emailLower}`);
      return res.status(400).json({ success: false, message: "Yêu cầu mã OTP trước khi xác thực." });
    }

    console.log(`[OTP] Found UserID: ${user.user_id}, HasOTP: ${!!user.otp_code}, Expiry: ${user.otp_expiry}`);

    if (!user.otp_code || !user.otp_expiry) {
      return res.status(400).json({ success: false, message: "Yêu cầu mã OTP trước khi xác thực." });
    }

    if (isOtpExpired(user.otp_expiry)) {
      console.log(`[OTP] Verify failed: OTP expired for ${emailLower}`);
      return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn." });
    }

    const inputHash = hashOtp(otp);
    if (inputHash !== user.otp_code) {
      console.log(`[OTP] Verify failed: Incorrect code. InputHash: ${inputHash.substring(0,10)}... vs DB: ${user.otp_code.substring(0,10)}...`);
      return res.status(400).json({ success: false, message: "Mã OTP không chính xác." });
    }

    // Xác thực thành công: Xóa mã OTP
    await db("users")
      .where({ user_id: user.user_id })
      .update({ otp_code: null, otp_expiry: null });

    console.log(`[OTP] Verify success for UserID: ${user.user_id}`);

    const token = signJwt({
      user_id: user.user_id,
      email: user.email,
      role: user.role || "customer"
    });

    res.json({
      success: true,
      token,
      user: {
        user_id: user.user_id,
        email: user.email,
        role: user.role
      }
    });
  } catch (error) {
    console.error("[OTP] Error in verifyOtp:", error.message);
    next(error);
  }
}

async function firebaseLogin(req, res, next) {
    const { idToken } = req.body;
    try {
      const { verifyIdToken } = require("../services/firebase");
      const decoded = await verifyIdToken(idToken);
      const email = decoded.email.toLowerCase();

      const existing = await db("users").where({ email }).first();
      if (!existing) {
        await db("users").insert({
            email,
            role: "customer",
            phone: 'fb_' + Math.floor(Math.random() * 1000000000),
            password: hashPassword('firebase_login_default')
        });
      }

      const user = await db("users").where({ email }).orderBy('user_id', 'desc').first();
      const token = signJwt({ user_id: user.user_id, email: user.email, role: user.role });
      res.json({ success: true, token, user });
    } catch (error) {
      next(error);
    }
}

module.exports = { login, resetPassword, sendOtp, verifyOtp, firebaseLogin };
