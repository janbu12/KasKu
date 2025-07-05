const admin = require('firebase-admin')
const firebase = require('firebase/app')

const serviceAccount = require(process.env.FIREBASE_SERVICE_ACCOUNT_PATH)

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: process.env.FIREBASE_REALTIME_DATABASE_URL
})

const firebaseConfig = {
    apiKey: process.env.FIREBASE_API_KEY,
    authDomain: process.env.FIREBASE_AUTH_DOMAIN,
    projectId: process.env.FIREBASE_PROJECT_ID,
    storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
    messagingSenderId: process.env.FIREBASE_MESSAGING_SENDER_ID,
    appId: process.env.FIREBASE_APP_ID,
    measurementId: process.env.FIREBASE_MEASUREMENT_ID
}

const app = firebase.initializeApp(firebaseConfig);
const dbFirestore = admin.firestore();
const auth = admin.auth();

module.exports = { admin, dbFirestore, app, auth};