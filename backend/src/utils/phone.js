function normalizePhoneE164(phone, defaultCountryCode = "84") {
  if (!phone) {
    throw new Error("Phone number is required");
  }

  const cleaned = String(phone).trim().replace(/[\s-]/g, "");

  if (cleaned.startsWith("+")) {
    const digits = cleaned.slice(1);
    if (!/^[0-9]{8,15}$/.test(digits)) {
      throw new Error("Invalid phone number");
    }
    return `+${digits}`;
  }

  if (cleaned.startsWith("00")) {
    const digits = cleaned.slice(2);
    if (!/^[0-9]{8,15}$/.test(digits)) {
      throw new Error("Invalid phone number");
    }
    return `+${digits}`;
  }

  if (!/^[0-9]{8,15}$/.test(cleaned)) {
    throw new Error("Invalid phone number");
  }

  if (cleaned.startsWith("0")) {
    return `+${defaultCountryCode}${cleaned.slice(1)}`;
  }

  return `+${defaultCountryCode}${cleaned}`;
}

module.exports = { normalizePhoneE164 };

