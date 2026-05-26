require("dotenv").config();

const required = [
  "DB_HOST",
  "DB_USER",
  "DB_NAME",
  "JWT_SECRET",
  "OTP_SECRET",
  "TWILIO_ACCOUNT_SID",
  "TWILIO_AUTH_TOKEN",
  "TWILIO_FROM_NUMBER"
];

const missing = required.filter((key) => !process.env[key]);

if (missing.length > 0) {
  console.error("Missing env vars:", missing.join(", "));
  process.exit(1);
}

if (!process.env.UPLOAD_DIR) {
  console.log("UPLOAD_DIR not set, using default ./uploads");
}

console.log("Smoke test passed");
