const Joi = require("joi");

const updateDriverProfileSchema = Joi.object({
  license_plate: Joi.string().min(3).required(),
  vehicle_type: Joi.string().min(2).optional()
});

const driverLocationSchema = Joi.object({
  current_lat: Joi.number().required(),
  current_lng: Joi.number().required(),
  is_online: Joi.number().valid(0, 1).optional()
});

module.exports = { updateDriverProfileSchema, driverLocationSchema };
