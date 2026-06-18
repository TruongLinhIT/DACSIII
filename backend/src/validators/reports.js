const Joi = require("joi");

const createReportSchema = Joi.object({
  order_id: Joi.number().integer().optional().allow(null),
  driver_id: Joi.number().integer().required(),
  reason_type: Joi.string().min(3).max(255).required(),
  description: Joi.string().min(5).required()
});

module.exports = { createReportSchema };
