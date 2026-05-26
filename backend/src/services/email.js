const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
  host: process.env.MAIL_HOST,
  port: parseInt(process.env.MAIL_PORT || "587"),
  secure: process.env.MAIL_SECURE === "true",
  auth: {
    user: process.env.MAIL_USER,
    pass: process.env.MAIL_PASS,
  },
});

async function sendEmailOtp(to, otp) {
  const mailOptions = {
    from: `"Delivery Pro" <${process.env.MAIL_FROM}>`,
    to: to,
    subject: "Mã xác thực OTP của bạn",
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>Xác thực tài khoản</h2>
        <p>Chào bạn,</p>
        <p>Mã OTP để đăng nhập/đăng ký vào hệ thống Delivery Pro của bạn là:</p>
        <h1 style="color: #4CAF50; font-size: 32px; letter-spacing: 5px;">${otp}</h1>
        <p>Mã này sẽ hết hạn sau ${process.env.OTP_TTL_MINUTES || 5} phút.</p>
        <p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
        <hr />
        <p style="font-size: 12px; color: #888;">Đây là email tự động, vui lòng không phản hồi.</p>
      </div>
    `,
  };

  try {
    await transporter.sendMail(mailOptions);
    return true;
  } catch (error) {
    console.error("Error sending email:", error);
    throw new Error("Không thể gửi email OTP");
  }
}

module.exports = { sendEmailOtp };
