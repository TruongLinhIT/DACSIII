// SMS delivery is disabled in this project.

async function sendSms() {
  throw new Error("SMS delivery is disabled");
}

module.exports = { sendSms };
