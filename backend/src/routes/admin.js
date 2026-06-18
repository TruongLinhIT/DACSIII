const express = require("express");
const { authenticate, authorize } = require("../middleware/auth");
const { validateBody } = require("../middleware/validate");
const { verifyIdentitySchema, lockUserSchema, revokeEkycSchema } = require("../validators/admin");
const { getAllUsers, verifyUserIdentity, getUserDetail, lockUser, unlockUser, revokeEkyc, getCommissionSummary } = require("../controllers/admin");

const router = express.Router();

// Tất cả các route admin đều yêu cầu đăng nhập và quyền admin
router.use(authenticate);
router.use(authorize("admin"));

router.get("/users", getAllUsers);
router.get("/commission-summary", getCommissionSummary);
router.get("/users/:id", getUserDetail);
router.put("/users/:id/verify", validateBody(verifyIdentitySchema), verifyUserIdentity);
router.put("/users/:id/lock", validateBody(lockUserSchema), lockUser);
router.put("/users/:id/unlock", unlockUser);
router.put("/users/:id/revoke-ekyc", validateBody(revokeEkycSchema), revokeEkyc);

module.exports = router;