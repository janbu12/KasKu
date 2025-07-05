require('dotenv').config();

const express = require('express');
const cors = require('cors');
const authRoutes = require('./routes/auth');
const apiRoutes = require('./routes/api');
const { dbFirestore, auth } = require('./config/firebase');

const app = express();
const PORT = process.env.PORT;

require('./config/firebase');

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
  req.db = dbFirestore;
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