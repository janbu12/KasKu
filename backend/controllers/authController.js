const { User } = require('../models/User');
const { app, auth } = require('../config/firebase');
const { signInWithEmailAndPassword, getAuth, signOut } = require('firebase/auth');
const authService = require('../services/authService');

exports.register = async (req, res) => {
  const { username, email, password } = req.body;
  const db = req.db;
  const auth = req.auth;

  if (!username || !email || !password) {
    return res.status(400).send({ message: 'Username, email, and password are required.' });
  }

  try {
    const userData = await authService.registerUser(username, email, password, auth, db);
    res.status(201).send({
      message: 'User registered successfully!',
      userId: userData.uid,
      email: userData.email,
      username: userData.username
    });

  } catch (error) {
    console.error('Error during registration:', error);

    if (error.code === 'auth/email-already-exists') {
      return res.status(409).send({ message: 'Email already in use. Please use a different email.' });
    }
    if (error.code === 'auth/invalid-email') {
      return res.status(400).send({ message: 'Invalid email format.' });
    }
    if (error.code === 'auth/invalid-password-format' || error.code === 'auth/invalid-email') {
          return res.status(400).send({ message: error.message });
      }
    res.status(500).send({ message: 'Failed to register user.', error: error.message });
  }
};


// For Testing purposes, we can use Firebase's signInWithEmailAndPassword method
exports.login = async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).send({ message: 'Email and password are required.' });
  }
  try {
    const auth = getAuth(app);
    const loginData = await authService.loginUser(email, password, auth);
    res.status(200).send({
      message: 'Login successful!',
      uid: loginData.uid,
      email: loginData.email,
      token: loginData.token
    });
  } catch (error) {
    let message = 'Login failed.';
    console.log(error.code)
    if (error.code === 'auth/user-not-found' || error.code === 'auth/wrong-password') {
      message = 'Invalid email or password.';
      return res.status(401).send({ message });
    }
    if (error.code === 'auth/invalid-email') {
      message = 'Invalid email format.';
      return res.status(400).send({ message });
    }
    if (error.code === 'auth/invalid-credential') {
      message = 'Invalid credentials provided. Please check your email and password.';
      return res.status(400).send({ message });
    }
    res.status(500).send({ message, error: error.message });
  }
};

exports.logout = async (req, res) => {
  try {
    const uid = req.user.uid;
    authService.logoutUser(uid);
    res.status(200).send({ message: 'Logout successful!' });
  } catch (error) {
    res.status(500).send({ message: 'Logout failed.', error: error.message });
  }
};
