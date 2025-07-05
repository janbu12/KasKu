const express = require('express');
const router = express.Router();
const AuthController = require('../controllers/authController');
const { protect } = require('../middleware/authMiddleware');

router.post('/register', AuthController.register);

// For Testing purposes, with Firebase Client SDK
// This is not recommended for production use, as it exposes Firebase credentials
router.post('/login', AuthController.login);
router.post('/logout', protect, AuthController.logout);

module.exports = router;