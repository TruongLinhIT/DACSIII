const express = require("express");
const helmet = require("helmet");
const cors = require("cors");

const authRoutes = require("./routes/auth");
const userRoutes = require("./routes/users");
const adminRoutes = require("./routes/admin");
const orderRoutes = require("./routes/orders"); // Import order routes
const driverRoutes = require("./routes/driver");
const notificationRoutes = require("./routes/notifications");
const reportRoutes = require("./routes/reports");

const { uploadRoot } = require("./middleware/upload");
const { notFoundHandler, errorHandler } = require("./middleware/error");

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json());
app.use("/uploads", express.static(uploadRoot));

app.get("/health", (_req, res) => {
  res.json({ success: true, message: "OK" });
});

app.use("/auth", authRoutes);
app.use("/users", userRoutes);
app.use("/admin", adminRoutes);
app.use("/orders", orderRoutes); // Register order routes
app.use("/driver", driverRoutes); // Register driver routes
app.use("/notifications", notificationRoutes); // Register notifications routes
app.use("/reports", reportRoutes); // Register reports routes

app.use(notFoundHandler);
app.use(errorHandler);

module.exports = app;
