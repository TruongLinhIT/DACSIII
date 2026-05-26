const fs = require("fs");
const path = require("path");
const multer = require("multer");

const uploadRoot = process.env.UPLOAD_DIR || path.join(__dirname, "..", "..", "uploads");

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function mapFieldToDir(fieldname) {
  switch (fieldname) {
    case "id_card_front":
      return path.join(uploadRoot, "identity", "front");
    case "id_card_back":
      return path.join(uploadRoot, "identity", "back");
    case "portrait":
      return path.join(uploadRoot, "identity", "portrait");
    case "avatar":
      return path.join(uploadRoot, "avatar");
    case "order_photo_before":
      return path.join(uploadRoot, "orders", "before");
    case "order_photo_pickup":
      return path.join(uploadRoot, "orders", "pickup");
    case "order_photo_delivery":
      return path.join(uploadRoot, "orders", "delivery");
    default:
      return uploadRoot;
  }
}

function buildFileName(originalName) {
  const ext = path.extname(originalName || "");
  const stamp = new Date().toISOString().replace(/[-:.TZ]/g, "");
  const rand = Math.random().toString(16).slice(2, 10);
  return `${stamp}_${rand}${ext}`;
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = mapFieldToDir(file.fieldname);
    ensureDir(dir);
    cb(null, dir);
  },
  filename: (_req, file, cb) => {
    cb(null, buildFileName(file.originalname));
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 }
});

module.exports = { upload, uploadRoot };
