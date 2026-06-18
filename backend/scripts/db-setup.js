const db = require("../src/db/knex");

async function setup() {
  try {
    const hasTable = await db.schema.hasTable("driver_reports");
    if (!hasTable) {
      await db.schema.createTable("driver_reports", (table) => {
        table.increments("report_id").primary();
        table.integer("order_id").nullable();
        table.integer("reporter_id").notNullable();
        table.integer("driver_id").notNullable();
        table.string("reason_type", 255).notNullable();
        table.text("description").notNullable();
        table.enu("status", ["pending", "resolved"]).defaultTo("pending");
        table.timestamp("created_at").defaultTo(db.fn.now());
        table.timestamp("resolved_at").nullable();

        // Foreign keys
        table.foreign("order_id").references("order_id").inTable("orders").onDelete("SET NULL");
        table.foreign("reporter_id").references("user_id").inTable("users").onDelete("CASCADE");
        table.foreign("driver_id").references("user_id").inTable("users").onDelete("CASCADE");
      });
      console.log("Table 'driver_reports' created successfully.");
    } else {
      console.log("Table 'driver_reports' already exists.");
    }
  } catch (err) {
    console.error("Error setting up database:", err);
  } finally {
    await db.destroy();
  }
}

setup();
