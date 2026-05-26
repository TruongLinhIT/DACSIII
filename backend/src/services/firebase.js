const admin = require("firebase-admin");
const path = require("path");

let appInstance = null;

function getFirebaseApp() {
  if (appInstance) {
    return appInstance;
  }

  const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (!serviceAccountPath) {
    throw new Error("FIREBASE_SERVICE_ACCOUNT_PATH is missing");
  }

  const resolvedPath = path.isAbsolute(serviceAccountPath)
    ? serviceAccountPath
    : path.join(process.cwd(), serviceAccountPath);

  let serviceAccount;
  try {
    serviceAccount = require(resolvedPath);
  } catch (error) {
    throw new Error(`Firebase service account not found at ${resolvedPath}`);
  }

  appInstance = admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

  return appInstance;
}

async function verifyIdToken(idToken) {
  getFirebaseApp();
  return admin.auth().verifyIdToken(idToken);
}

async function sendMulticast(payload) {
  getFirebaseApp();
  return admin.messaging().sendMulticast(payload);
}

module.exports = { verifyIdToken, sendMulticast };
