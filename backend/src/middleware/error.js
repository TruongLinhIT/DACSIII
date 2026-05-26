function notFoundHandler(_req, res) {
  res.status(404).json({ success: false, message: "Not found" });
}

function errorHandler(err, _req, res, _next) {
  const status = err.statusCode || 500;
  const message = err.message || "Internal server error";

  res.status(status).json({ success: false, message });
}

module.exports = { notFoundHandler, errorHandler };

