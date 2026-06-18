const Joi = require("joi");

const updateDriverProfileSchema = Joi.object({
  license_plate: Joi.string().min(3).required(),
  vehicle_type: Joi.string().min(2).optional()
});

const driverLocationSchema = Joi.object({
  current_lat: Joi.number().optional(),
  current_lng: Joi.number().optional(),
  is_online: Joi.number().valid(0, 1).optional()
})
  .and("current_lat", "current_lng")
  .or("is_online", "current_lat");

module.exports = { updateDriverProfileSchema, driverLocationSchema };
