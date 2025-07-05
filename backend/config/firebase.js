const admin = require('firebase-admin')
const serviceAccount = require(process.env.FIREBASE_SERVICE_ACCOUNT_PATH)

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: process.env.FIREBASE_REALTIME_DATABASE_URL
})

const dbFirestore = admin.firestore();
const auth = admin.auth();

module.exports = { admin, dbFirestore, auth }