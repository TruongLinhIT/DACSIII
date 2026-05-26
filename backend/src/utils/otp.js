const crypto = require("crypto");

function createOtp() {
  // Tạo mã 6 số ngẫu nhiên
  const otp = String(Math.floor(100000 + Math.random() * 900000));
  const otpHash = hashOtp(otp);
  const ttlMinutes = Number(process.env.OTP_TTL_MINUTES || 5);

  // Dùng đối tượng Date để tương thích tốt nhất với cột DATETIME/TIMESTAMP trong MySQL
  const expiresAt = new Date(Date.now() + ttlMinutes * 60 * 1000);

  return { otp, otpHash, expiresAt };
}

function hashOtp(otp) {
  // Lấy secret từ env, nếu không có thì dùng mặc định (nhưng nên có trong .env)
  const secret = process.env.OTP_SECRET || "default_otp_secret_key";
  // Trim để loại bỏ khoảng trắng thừa nếu có
  return crypto.createHmac("sha256", secret).update(String(otp).trim()).digest("hex");
}

function isOtpExpired(expiry) {
  if (!expiry) return true;
  const expiryTime = new Date(expiry).getTime();
  return expiryTime < Date.now();
}

module.exports = { createOtp, hashOtp, isOtpExpired };
