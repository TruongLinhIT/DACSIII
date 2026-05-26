require("dotenv").config();

const axios = require("axios");

async function run() {
  const baseUrl = process.env.API_BASE_URL || "http://localhost:3000";
  const token = process.env.DEMO_TOKEN;

  if (!token) {
    console.error("Missing DEMO_TOKEN env var");
    process.exit(1);
  }

  const profile = {
    full_name: "Demo User",
    email: "demo@example.com",
    role: "customer",
    cccd_number: "012345678901",
    avatar_url: ""
  };

  const response = await axios.put(`${baseUrl}/users/me/profile`, profile, {
    headers: { Authorization: `Bearer ${token}` }
  });

  console.log("Profile update response:", response.data);
}

run().catch((error) => {
  console.error("Demo failed:", error.response?.data || error.message);
  process.exit(1);
});

