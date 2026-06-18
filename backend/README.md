# Delivery Backend (Step 1)

Node.js + Express backend for OTP login using MySQL (Knex).

## Requirements
- Node.js 18+
- MySQL/MariaDB
- Firebase service account (for Firebase login)

## Quick Start
1) Copy env file and fill values:

```
# PowerShell
Copy-Item .env.example .env
```

2) Install dependencies:

```
# PowerShell
npm install
```

3) Start server:

```
# PowerShell
npm run dev
```

## API
### POST /auth/send-otp
Request body:
```
{
  "phone": "0123456789"
}
```
Response:
```
{
  "success": true,
  "message": "OTP sent"
}
```

### POST /auth/verify-otp
Request body:
```
{
  "phone": "0123456789",
  "otp": "123456"
}
```
Response:
```
{
  "success": true,
  "token": "<jwt>",
  "user": {
    "user_id": 1,
    "phone": "0123456789",
    "role": "customer"
  }
}
```

### POST /auth/firebase-login
Request body:
```
{
  "idToken": "<firebase_id_token>"
}
```
Response:
```
{
  "success": true,
  "token": "<jwt>",
  "user": {
    "user_id": 1,
    "phone": "+84765539316",
    "role": "customer"
  }
}
```

### GET /users/me
Headers:
```
Authorization: Bearer <jwt>
```
Response:
```
{
  "success": true,
  "user": {
    "user_id": 1,
    "phone": "0123456789",
    "full_name": "Nguyen Van A",
    "email": "a@example.com",
    "role": "customer",
    "cccd_number": "012345678901",
    "id_card_front_url": "/uploads/identity/front/xxx.jpg",
    "id_card_back_url": "/uploads/identity/back/xxx.jpg",
    "portrait_url": "/uploads/identity/portrait/xxx.jpg",
    "is_verified": "pending",
    "created_at": "2026-04-28 10:00:00",
    "updated_at": "2026-04-28 10:05:00"
  }
}
```

### PUT /users/me/profile
Headers:
```
Authorization: Bearer <jwt>
```
Request body:
```
{
  "full_name": "Nguyen Van A",
  "email": "a@example.com",
  "role": "driver",
  "cccd_number": "012345678901",
  "avatar_url": ""
}
```
Response:
```
{ "success": true, "message": "Profile updated" }
```

### POST /users/me/identity
Headers:
```
Authorization: Bearer <jwt>
Content-Type: multipart/form-data
```
Form fields:
- id_card_front: image file
- id_card_back: image file
- portrait: image file

Response:
```
{
  "success": true,
  "message": "Identity uploaded",
  "files": {
    "id_card_front_url": "/uploads/identity/front/xxx.jpg",
    "id_card_back_url": "/uploads/identity/back/xxx.jpg",
    "portrait_url": "/uploads/identity/portrait/xxx.jpg",
    "is_verified": "pending"
  }
}
```

### POST /users/me/avatar
Headers:
```
Authorization: Bearer <jwt>
Content-Type: multipart/form-data
```
Form fields:
- avatar: image file

Response:
```
{
  "success": true,
  "message": "Avatar uploaded",
  "avatar_url": "/uploads/avatar/xxx.jpg"
}
```

## Database Note
This backend hashes OTP before storing. Update the schema to avoid truncation:

```
ALTER TABLE users MODIFY otp_code VARCHAR(64) DEFAULT NULL;
```

## Environment Variables
See `.env.example`.

Required for Firebase login:
- `FIREBASE_SERVICE_ACCOUNT_PATH`: absolute or relative path to Firebase service account JSON.

Optional:
- `UPLOAD_DIR`: custom upload folder (default: `./uploads`).

## Demo Script
Update profile quickly after you have a JWT token:
```
# PowerShell
$env:DEMO_TOKEN = "<jwt>"
node scripts/profile-demo.js
```

## Test
This repo includes a simple smoke test script:

```
# PowerShell
npm test
```
