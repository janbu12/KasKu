const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/authMiddleware');
const UserRoutes = require('./userRoutes');
const StructRoutes = require('./structRoutes');

router.use('/structs', protect, StructRoutes);
router.use('/users', protect, UserRoutes);

module.exports = router;