const db = require("../db/knex");

/**
 * @desc    Gửi báo cáo tài xế (Khách hàng)
 * @route   POST /reports
 */
async function createReport(req, res, next) {
  try {
    const reporter_id = req.user.user_id;
    const { order_id, driver_id, reason_type, description } = req.body;

    if (reporter_id === Number(driver_id)) {
      return res.status(400).json({ success: false, message: "Bạn không thể tự báo cáo chính mình." });
    }

    // Kiểm tra tài xế tồn tại
    const driver = await db("users")
      .where({ user_id: driver_id, role: "driver" })
      .first();

    if (!driver) {
      return res.status(404).json({ success: false, message: "Không tìm thấy tài xế bị báo cáo." });
    }

    // Nếu có đơn hàng, kiểm tra tính hợp lệ
    if (order_id) {
      const order = await db("orders")
        .where({ order_id })
        .first();

      if (!order) {
        return res.status(404).json({ success: false, message: "Không tìm thấy đơn hàng liên quan." });
      }

      if (order.customer_id !== reporter_id) {
        return res.status(403).json({ success: false, message: "Bạn không có quyền báo cáo cho đơn hàng này." });
      }

      if (order.driver_id !== Number(driver_id)) {
        return res.status(400).json({ success: false, message: "Tài xế bị báo cáo không khớp với tài xế nhận đơn hàng." });
      }
    }

    // Tạo báo cáo
    await db("driver_reports").insert({
      order_id: order_id || null,
      reporter_id,
      driver_id,
      reason_type,
      description,
      status: "pending"
    });

    res.status(201).json({ success: true, message: "Gửi báo cáo tài xế thành công." });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Lấy danh sách báo cáo (Admin)
 * @route   GET /reports
 */
async function getAllReports(req, res, next) {
  try {
    const { status, search } = req.query;

    let query = db("driver_reports")
      .join("users as reporter", "reporter.user_id", "driver_reports.reporter_id")
      .join("users as driver", "driver.user_id", "driver_reports.driver_id")
      .leftJoin("drivers", "drivers.driver_id", "driver_reports.driver_id")
      .select(
        "driver_reports.*",
        "reporter.full_name as reporter_name",
        "reporter.phone as reporter_phone",
        "driver.full_name as driver_name",
        "driver.phone as driver_phone",
        "drivers.license_plate"
      );

    if (status) {
      query = query.where("driver_reports.status", status);
    }

    if (search) {
      query = query.where(function() {
        this.where("driver.full_name", "like", `%${search}%`)
            .orWhere("reporter.full_name", "like", `%${search}%`)
            .orWhere("driver_reports.reason_type", "like", `%${search}%`);
      });
    }

    const reports = await query.orderBy("driver_reports.created_at", "desc");

    res.json({ success: true, reports });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Chi tiết báo cáo (Admin)
 * @route   GET /reports/:id
 */
async function getReportDetail(req, res, next) {
  try {
    const { id } = req.params;

    const report = await db("driver_reports")
      .join("users as reporter", "reporter.user_id", "driver_reports.reporter_id")
      .join("users as driver", "driver.user_id", "driver_reports.driver_id")
      .leftJoin("drivers", "drivers.driver_id", "driver_reports.driver_id")
      .select(
        "driver_reports.*",
        "reporter.full_name as reporter_name",
        "reporter.phone as reporter_phone",
        "driver.full_name as driver_name",
        "driver.phone as driver_phone",
        "drivers.license_plate"
      )
      .where("driver_reports.report_id", id)
      .first();

    if (!report) {
      return res.status(404).json({ success: false, message: "Không tìm thấy báo cáo." });
    }

    res.json({ success: true, report });
  } catch (error) {
    next(error);
  }
}

/**
 * @desc    Giải quyết báo cáo (Admin)
 * @route   PUT /reports/:id/resolve
 */
async function resolveReport(req, res, next) {
  try {
    const { id } = req.params;

    const report = await db("driver_reports").where({ report_id: id }).first();
    if (!report) {
      return res.status(404).json({ success: false, message: "Không tìm thấy báo cáo." });
    }

    if (report.status === "resolved") {
      return res.status(400).json({ success: false, message: "Báo cáo này đã được giải quyết từ trước." });
    }

    await db("driver_reports")
      .where({ report_id: id })
      .update({
        status: "resolved",
        resolved_at: db.fn.now()
      });

    res.json({ success: true, message: "Giải quyết báo cáo thành công." });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  createReport,
  getAllReports,
  getReportDetail,
  resolveReport
};
