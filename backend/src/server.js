require("dotenv").config();
const app = require("./app");
const db = require("./db/knex");
const http = require("http");
const { initSocket } = require("./socket");

const port = Number(process.env.PORT || 3000);

async function start() {
  try {
    await db.raw("SELECT 1");
    const server = http.createServer(app);
    initSocket(server);
    server.listen(port, () => {
      console.log(`Server listening on port ${port}`);
    });
  } catch (error) {
    console.error("Database connection failed", error);
    process.exit(1);
  }
}

start();
