const express = require("express");
const rateLimit = require("express-rate-limit");

const { validateBody } = require("../middleware/validate");
const {
  sendOtpSchema,
  verifyOtpSchema,
  loginSchema,
  resetPasswordSchema,
  firebaseLoginSchema
} = require("../validators/auth");
const {
  login,
  resetPassword,
  sendOtp,
  verifyOtp,
  firebaseLogin
} = require("../controllers/auth");

const router = express.Router();

const authLimiter = rateLimit({
  windowMs: 5 * 60 * 1000,
  max: 10,
  message: { success: false, message: "Too many requests" }
});

router.post("/login", authLimiter, validateBody(loginSchema), login);
router.post("/send-otp", authLimiter, validateBody(sendOtpSchema), sendOtp);
router.post("/verify-otp", authLimiter, validateBody(verifyOtpSchema), verifyOtp);
router.post("/reset-password", authLimiter, validateBody(resetPasswordSchema), resetPassword);
router.post("/firebase-login", authLimiter, validateBody(firebaseLoginSchema), firebaseLogin);

module.exports = router;
