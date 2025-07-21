const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/authMiddleware');
const UserRoutes = require('./userRoutes');
const StructRoutes = require('./structRoutes');
const DashboardRoutes = require('./dashboardRoutes');

router.use('/structs', protect, StructRoutes);
router.use('/users', protect, UserRoutes);
router.use('/dashboard', protect, DashboardRoutes);

module.exports = router;