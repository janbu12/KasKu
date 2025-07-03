require('dotenv').config();

const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const authRoutes = require('./routes/auth');
const apiRoutes = require('./routes/api');

const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

const app = express();
const PORT = process.env.PORT;

app.use(cors());
app.use(express.json());

app.use((req, res, next) => {
  req.db = db;
  req.auth = auth;
  next();
});


app.get('/', (req, res) => {
  res.send('Welcome to KasKu Backend API!');
});

app.use('/auth', authRoutes);
app.use('/api', apiRoutes);

app.listen(PORT, () => {
  console.log(`KasKu Backend Server is running on port ${PORT}`);
});