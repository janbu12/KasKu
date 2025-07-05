const { User } = require('../models/User');
const { app, auth } = require('../config/firebase');
const { signInWithEmailAndPassword, getAuth, signOut } = require('firebase/auth');

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

exports.register = async (req, res) => {
  const { username, email, password } = req.body;
  const db = req.db;
  const auth = req.auth;

  if (!username || !email || !password) {
    return res.status(400).send({ message: 'Username, email, and password are required.' });
  }

  const usernameSnapshot = await db.collection('users').where('username', '==', username).get();
  if (!usernameSnapshot.empty) {
    return res.status(409).send({ message: 'Username already in use. Please choose a different username.' });
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

    // Use userRecord.uid as document ID for consistency
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
};


// For Testing purposes, we can use Firebase's signInWithEmailAndPassword method
exports.login = async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).send({ message: 'Email and password are required.' });
  }
  try {
    const auth = getAuth(app);
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    const token = await user.getIdToken();
    res.status(200).send({
      message: 'Login successful!',
      uid: user.uid,
      email: user.email,
      token
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
    await auth.revokeRefreshTokens(uid);
    res.status(200).send({ message: 'Logout successful!' });
  } catch (error) {
    res.status(500).send({ message: 'Logout failed.', error: error.message });
  }
};
