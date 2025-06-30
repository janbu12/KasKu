const express = require('express');
const router = express.Router();
const { User } = require('../models/User');

function validatePassword(password) {
  if (password.length < 6) {
    return 'Password must be at least 6 characters long.';
  }

  if (!/[A-Z]/.test(password)) {
    return 'Password must contain at least one uppercase letter.';
  }

  if (!/[a-z]/.test(password)) {
    return 'Password must contain at least one lowercase letter.';
  }

  if (!/[0-9]/.test(password)) {
    return 'Password must contain at least one number.';
  }

  if (!/[^A-Za-z0-9]/.test(password)) {
    return 'Password must contain at least one special character/symbol.';
  }
  return null;
}

router.post('/register', async (req, res) => {
  const { username, email, password } = req.body;
  const db = req.db;
  const auth = req.auth;

  if (!username || !email || !password) {
    return res.status(400).send({ message: 'Username, email, and password are required.' });
  }

  const passwordError = validatePassword(password);
  if (passwordError) {
    return res.status(400).send({ message: passwordError });
  }

  try {
    const userRecord = await auth.createUser({
      email: email,
      password: password,
      displayName: username,
    });

    const newUser = new User(
      userRecord.uid,
      username,
      email,
      new Date(),
      false
    );

    // Menggunakan userRecord.uid sebagai ID dokumen untuk konsistensi
    const userRef = db.collection('users').doc(newUser.uid);
    await userRef.set(newUser.toFirestore());

    res.status(201).send({
      message: 'User registered successfully!',
      userId: newUser.uid,
      email: newUser.email,
      username: newUser.username
    });

  } catch (error) {
    console.error('Error during registration:', error);

    if (error.code === 'auth/email-already-exists') {
      return res.status(409).send({ message: 'Email already in use. Please use a different email.' });
    }
    if (error.code === 'auth/invalid-email') {
        return res.status(400).send({ message: 'Invalid email format.' });
    }
    res.status(500).send({ message: 'Failed to register user.', error: error.message });
  }
});

module.exports = router;