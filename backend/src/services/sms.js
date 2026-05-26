const twilio = require("twilio");
const { normalizePhoneE164 } = require("../utils/phone");

function getClient() {
  const accountSid = process.env.TWILIO_ACCOUNT_SID;
  const authToken = process.env.TWILIO_AUTH_TOKEN;

  if (!accountSid || !authToken) {
    throw new Error("Twilio credentials are missing");
  }

  return twilio(accountSid, authToken);
}

async function sendSms(to, body) {
  const from = process.env.TWILIO_FROM_NUMBER;

  if (!from) {
    throw new Error("TWILIO_FROM_NUMBER is missing");
  }

  const client = getClient();
  const toE164 = normalizePhoneE164(to);
  const fromE164 = normalizePhoneE164(from);
  await client.messages.create({ from: fromE164, to: toE164, body });
}

// Note: Twilio is optional when using Firebase Phone Auth.

module.exports = { sendSms };
