const admin = require("firebase-admin");
const path = require("path");

let appInstance = null;
let isInitialized = false;

function initFirebase() {
  if (appInstance) {
    return appInstance;
  }

  const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (!serviceAccountPath) {
    console.warn("FIREBASE_SERVICE_ACCOUNT_PATH is missing. FCM notifications will be disabled.");
    return null;
  }

  const resolvedPath = path.isAbsolute(serviceAccountPath)
    ? serviceAccountPath
    : path.join(process.cwd(), serviceAccountPath);

  try {
    const serviceAccount = require(resolvedPath);
    appInstance = admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    isInitialized = true;
    console.log("Firebase Admin initialized successfully.");
    return appInstance;
  } catch (error) {
    console.warn(`Firebase initialization failed (not found at ${resolvedPath}):`, error.message);
    return null;
  }
}

// Initialize on startup
initFirebase();

function isFirebaseReady() {
  return isInitialized && appInstance !== null;
}

async function verifyIdToken(idToken) {
  if (!isFirebaseReady()) {
    throw new Error("Firebase Admin is not initialized");
  }
  return admin.auth().verifyIdToken(idToken);
}

async function sendToTokens(tokens, notification, data) {
  if (!isFirebaseReady()) {
    console.warn("Firebase not ready. Cannot send FCM notification.");
    return { success: false, error: "Firebase not initialized" };
  }

  // Filter out invalid/empty tokens
  const validTokens = tokens.filter(t => typeof t === 'string' && t.trim() !== '');
  if (validTokens.length === 0) {
    return { success: true, responses: [] };
  }

  const message = {
    tokens: validTokens,
  };

  if (notification && (notification.title || notification.body)) {
    message.notification = {
      title: notification.title || "",
      body: notification.body || ""
    };
  }

  if (data) {
    message.data = {};
    for (const [key, val] of Object.entries(data)) {
      message.data[key] = String(val);
    }
  }

  try {
    const response = await admin.messaging().sendEachForMulticast(message);
    console.log(`FCM Sent multicast: ${response.successCount} success, ${response.failureCount} failure.`);
    return response;
  } catch (error) {
    console.error("FCM Send multicast error:", error);
    throw error;
  }
}

async function sendMulticast(payload) {
  if (!isFirebaseReady()) {
    throw new Error("Firebase not initialized");
  }
  return admin.messaging().sendMulticast(payload);
}

module.exports = { 
  verifyIdToken, 
  sendToTokens, 
  sendMulticast, 
  isFirebaseReady 
};
