const express = require('express');
const router = express.Router();
const AuthController = require('../controllers/authController');
const { protect } = require('../middleware/authMiddleware');
const redisClient = require('../config/redis');

// Rate limiter middleware menggunakan Redis
const loginRateLimiter = async (req, res, next) => {
  const ip = req.ip;
  const key = `login:rate:${ip}`;
  const maxAttempts = 5;
  const windowSec = 15 * 60; // 15 menit

  let attempts = await redisClient.get(key);
  attempts = attempts ? parseInt(attempts) : 0;

  if (attempts >= maxAttempts) {
    return res.status(429).json({ message: 'Too many login attempts. Please try again later.' });
  }

  await redisClient.multi()
    .incr(key)
    .expire(key, windowSec)
    .exec();

  next();
};

router.post('/register', AuthController.register);

// For Testing purposes, with Firebase Client SDK
// This is not recommended for production use, as it exposes Firebase credentials
router.post('/login', loginRateLimiter, AuthController.login);
router.post('/logout', protect, AuthController.logout);

module.exports = router;