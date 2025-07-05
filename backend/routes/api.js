const express = require('express');
const router = express.Router();
const { 
  getAllUser, 
  getProfileUser, 
  updateProfileUser,
  addStruct
} = require('../controllers/UserController');
const { uploadStrukHandler } = require('../controllers/UploadController');
const { upload } = require('../config/multer');


// Stuct routes
router.get('/structs', async (req, res) => res.send("Ini Api Struct"))
router.post('/upload/struct', upload.single('struk_image'), uploadStrukHandler);
router.post('/struct/add', addStruct);

// User routes
router.get('/users', getAllUser);
router.get('/users/:uid', getProfileUser);
router.put('/users/:uid/profile', updateProfileUser);

module.exports = router;