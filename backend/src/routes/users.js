const express = require("express");

const { authenticate } = require("../middleware/auth");
const { validateBody } = require("../middleware/validate");
const { upload } = require("../middleware/upload");
const { updateProfileSchema, changePasswordSchema, deviceTokenSchema } = require("../validators/users");
const {
  getMe,
  updateProfile,
  uploadIdentity,
  uploadAvatar,
  sendChangePasswordOtp,
  changePasswordWithOtp,
  updateDeviceToken
} = require("../controllers/users");

const router = express.Router();

router.get("/me", authenticate, getMe);
router.put("/me/profile", authenticate, validateBody(updateProfileSchema), updateProfile);
router.post(
  "/me/identity",
  authenticate,
  upload.fields([
    { name: "id_card_front", maxCount: 1 },
    { name: "id_card_back", maxCount: 1 },
    { name: "portrait", maxCount: 1 }
  ]),
  uploadIdentity
);
router.post("/me/avatar", authenticate, upload.single("avatar"), uploadAvatar);
router.post("/me/password/otp", authenticate, sendChangePasswordOtp);
router.post(
  "/me/password",
  authenticate,
  validateBody(changePasswordSchema),
  changePasswordWithOtp
);
router.post(
  "/me/device-token",
  authenticate,
  validateBody(deviceTokenSchema),
  updateDeviceToken
);

module.exports = router;
