const express = require('express');
const router = express.Router();
const { 
  getAllUser, 
  getProfileUser, 
  updateProfileUser
} = require('../controllers/UserController');
const { uploadStrukHandler } = require('../controllers/UploadController');
const { upload } = require('../config/multer');
const { addStruct, editStruct, deleteStruct, getMyStructs } = require('../controllers/StructController');
const { protect } = require('../middleware/authMiddleware');


// Stuct routes
router.get('/structs', protect , getMyStructs);
router.post('/upload/struct', protect, upload.single('struk_image'), uploadStrukHandler);
router.post('/struct/add', protect, addStruct);
router.put('/struct/edit/:id', protect, editStruct);
router.delete('/struct/delete/:id', protect, deleteStruct);

// User routes
router.get('/users', getAllUser);
router.get('/users/:uid', protect, getProfileUser);
router.put('/users/:uid/profile', protect, updateProfileUser);

module.exports = router;