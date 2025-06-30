require('dotenv').config();

const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');

const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const app = express();
const PORT = process.env.PORT;

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.send('Welcome to KasKu Backend API!');
});

app.listen(PORT, () => {
  console.log(`KasKu Backend Server is running on port ${PORT}`);
});