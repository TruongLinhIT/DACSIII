const Joi = require("joi");

const createOrderSchema = Joi.object({
  package_type: Joi.string().valid("electronics", "food", "bulky", "others").required(),
  weight_kg: Joi.number().positive().required(),
  order_description: Joi.string().allow("").optional(),
  pickup_address: Joi.string().min(3).required(),
  delivery_address: Joi.string().min(3).required(),
  pickup_lat: Joi.number().required(),
  pickup_lng: Joi.number().required(),
  delivery_lat: Joi.number().required(),
  delivery_lng: Joi.number().required(),
  distance_km: Joi.number().min(0).optional(),
  total_price: Joi.number().positive().optional(),
  photo_before_booking: Joi.string().min(1).required(),
  sender_name: Joi.string().min(2).required(),
  sender_phone: Joi.string().min(8).required(),
  recipient_name: Joi.string().min(2).required(),
  recipient_phone: Joi.string().min(8).required(),
  pickup_note: Joi.string().allow("").optional(),
  delivery_note: Joi.string().allow("").optional(),
  package_size: Joi.string().valid("S", "M", "L").required(),
  cod_amount: Joi.number().min(0).optional(),
  payment_method: Joi.string().valid("sender_cash", "recipient_cash").required()
});

module.exports = { createOrderSchema };
