const Joi = require("joi");

const emailSchema = Joi.string().email().required();

const sendOtpSchema = Joi.object({
  email: emailSchema
});

const verifyOtpSchema = Joi.object({
  email: emailSchema,
  otp: Joi.string().pattern(/^[0-9]{6}$/).required()
});

const loginSchema = Joi.object({
  email: emailSchema,
  password: Joi.string().min(6).required()
});

const resetPasswordSchema = Joi.object({
  email: emailSchema,
  otp: Joi.string().pattern(/^[0-9]{6}$/).required(),
  newPassword: Joi.string().min(6).required()
});

const firebaseLoginSchema = Joi.object({
  idToken: Joi.string().min(50).required()
});

module.exports = {
  sendOtpSchema,
  verifyOtpSchema,
  loginSchema,
  resetPasswordSchema,
  firebaseLoginSchema
};
