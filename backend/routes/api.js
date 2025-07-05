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


// Stuct routes
router.get('/structs', async (req, res) => res.send("Ini Api Struct"))
router.get('/structs/:userUid', getMyStructs);
router.post('/upload/struct', upload.single('struk_image'), uploadStrukHandler);
router.post('/struct/add', addStruct);
router.put('/struct/edit/:id', editStruct);
router.delete('/struct/delete/:id', deleteStruct);

// User routes
router.get('/users', getAllUser);
router.get('/users/:uid', getProfileUser);
router.put('/users/:uid/profile', updateProfileUser);

module.exports = router;