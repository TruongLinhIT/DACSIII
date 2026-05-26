const Joi = require("joi");

const updateProfileSchema = Joi.object({
  full_name: Joi.string().min(2).max(100),
  password: Joi.string().min(6).max(50),
  email: Joi.string().email().allow(""),
  cccd_number: Joi.string().pattern(/^\d{12}$/),
  avatar_url: Joi.string().max(255).allow("")
}).min(1);

const changePasswordSchema = Joi.object({
  otp: Joi.string().length(6).required(),
  newPassword: Joi.string().min(6).max(50).required()
});

const deviceTokenSchema = Joi.object({
  token: Joi.string().min(10).required()
});

module.exports = { updateProfileSchema, changePasswordSchema, deviceTokenSchema };
