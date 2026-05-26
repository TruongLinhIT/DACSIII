const Joi = require("joi");

const verifyIdentitySchema = Joi.object({
  status: Joi.string().valid("verified", "rejected").required(),
  reason: Joi.string().min(3).max(255).allow("")
});

module.exports = { verifyIdentitySchema };
