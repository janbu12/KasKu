const express = require('express');
const router = express.Router();
const AuthController = require('../controllers/AuthController');
const { protect } = require('../middleware/authMiddleware');

router.post('/register', AuthController.register);

// For Testing purposes, we can use Firebase's signInWithEmailAndPassword method
router.post('/login', AuthController.login);
router.post('/logout', protect, AuthController.logout);

module.exports = router;