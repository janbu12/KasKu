const express = require('express');
const router = express.Router();
const DashboardController = require('../controllers/dashboardController');  
const FinanceInsightController = require('../controllers/financeInsightController');
const { protect } = require('../middleware/authMiddleware');


router.get('/', protect, DashboardController.getDashboardData);
router.post('/insights', protect, FinanceInsightController.getMonthlyFinanceInsights);

module.exports = router;