const express = require('express');
const router = express.Router();

const { getAllUser, getMyProfile, updateMyProfile } = require("../controllers/userController");

//Test Purpose
router.get('/', getAllUser);

// User Profile Routes
router.get('/me', getMyProfile);
router.put('/me/profile', updateMyProfile);

module.exports = router;