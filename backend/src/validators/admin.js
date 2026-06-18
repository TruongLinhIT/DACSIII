const Joi = require("joi");

const verifyIdentitySchema = Joi.object({
  status: Joi.string().valid("verified", "rejected").required(),
  reason: Joi.string().min(3).max(255).allow("")
});

const lockUserSchema = Joi.object({
  reason: Joi.string().min(3).max(255).required()
    .messages({ "string.min": "Lý do phải có ít nhất 3 ký tự." })
});

const revokeEkycSchema = Joi.object({
  reason: Joi.string().min(3).max(255).required()
    .messages({ "string.min": "Lý do phải có ít nhất 3 ký tự." })
});

module.exports = { verifyIdentitySchema, lockUserSchema, revokeEkycSchema };
