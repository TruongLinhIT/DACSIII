# AGENTS.md

## Overview
- Monorepo: Android client in `app/` and Node/Express API in `backend/` for delivery/OTP + eKYC flows.

## Repo layout (first stop)
- `app/`: Android screens for login/register/OTP/eKYC (see `app/README.md`).
- `backend/src/app.js`: Express app wiring, route registration, `/uploads` static.
- `backend/src/server.js`: boots app and checks DB connectivity via Knex.
- `backend/src/routes/*`: HTTP routes per domain (auth/users/admin/orders/driver).
- `backend/src/routes/notifications.js` + `backend/src/controllers/notifications.js`: notification list/read APIs.
- `backend/src/controllers/*`: route handlers with DB access and business logic.
- `backend/src/validators/*` + `backend/src/middleware/validate.js`: Joi schemas + body validation.
- `backend/src/middleware/auth.js`: JWT auth + RBAC authorize.
- `backend/src/middleware/upload.js`: Multer storage + upload folder mapping.
- `delivery_pro_db.sql`: MySQL schema and seed data (users/orders/system_settings).
- `delivery_pro_db.sql`: includes `notifications` table.

## Backend request flow patterns
- Routes call `validateBody(schema)` then controller (example: `backend/src/routes/auth.js`).
- Controllers talk to MySQL via Knex (`backend/src/db/knex.js`, config in `backend/knexfile.js`).
- JWT auth is enforced via `authenticate` and optional `authorize("admin")` or `authorize("driver")` (see `backend/src/routes/admin.js`, `backend/src/routes/driver.js`).
- File uploads are stored under `UPLOAD_DIR` (default `./uploads`) and served at `/uploads` (`backend/src/app.js`, `backend/src/middleware/upload.js`).
- `backend/src/middleware/upload.js` maps upload fields to subfolders (`identity/front`, `identity/back`, `identity/portrait`, `avatar`, `orders/before`, `orders/pickup`, `orders/delivery`) and caps file size at 5 MB.
- `POST /users/me/device-token` stores `users.fcm_token` for push delivery (see `backend/src/routes/users.js`).
- `/notifications` routes support list/read/read-all (see `backend/src/routes/notifications.js`).
- `GET /health` is a lightweight health check defined in `backend/src/app.js`.
- `backend/src/middleware/error.js` provides `notFoundHandler` and `errorHandler` registered in `backend/src/app.js`.

## Auth + OTP specifics (project conventions)
- OTP is generated/hashed in `backend/src/utils/otp.js`, stored in `users.otp_code`, and emailed via `backend/src/services/email.js` (see `backend/src/controllers/auth.js`).
- `POST /auth/verify-otp` clears OTP, then issues JWT via `backend/src/utils/jwt.js`.
- Firebase login uses `backend/src/services/firebase.js` and `FIREBASE_SERVICE_ACCOUNT_PATH`.
- OTP is hashed, so `users.otp_code` should be `VARCHAR(64)` to avoid truncation (see `backend/README.md`).

## Orders and settings
- `POST /orders` computes commission from `system_settings.commission_rate` (see `backend/src/controllers/orders.js` and `delivery_pro_db.sql`).

## Dev workflows (known working commands)
- Backend setup: copy `backend/.env.example` to `backend/.env` then `npm install` in `backend/`.
- Run backend (dev): `npm run dev` (entry `backend/src/server.js`).
- Smoke test: `npm test` (runs `backend/scripts/smoke.js`).
- Demo profile update: set `DEMO_TOKEN` then run `node backend/scripts/profile-demo.js` (see `backend/README.md`).
- Android quick compile: `& "D:\Lap Trinh Di Dong\DACSIII-V2\gradlew.bat" -p "D:\Lap Trinh Di Dong\DACSIII-V2" :app:compileDebugKotlin`.

## Integrations and external deps
- Node.js 18+ required for backend (see `backend/README.md`).
- MySQL/MariaDB required for API (`DB_*` vars in `backend/.env.example`).
- Nodemailer for OTP email (`MAIL_*` vars used in `backend/src/services/email.js`).
- Twilio SMS optional (`backend/src/services/sms.js`), Firebase optional (`backend/src/services/firebase.js`).
- Firebase Cloud Messaging is used for notifications.
