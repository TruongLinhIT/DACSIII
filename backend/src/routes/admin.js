const express = require("express");
const { authenticate, authorize } = require("../middleware/auth");
const { validateBody } = require("../middleware/validate");
const { verifyIdentitySchema } = require("../validators/admin");
const { getAllUsers, verifyUserIdentity, getUserDetail } = require("../controllers/admin");

const router = express.Router();

// Tất cả các route admin đều yêu cầu đăng nhập và quyền admin
router.use(authenticate);
router.use(authorize("admin"));

router.get("/users", getAllUsers);
router.get("/users/:id", getUserDetail);
router.put("/users/:id/verify", validateBody(verifyIdentitySchema), verifyUserIdentity);

module.exports = router;
