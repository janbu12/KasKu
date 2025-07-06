const express = require('express');
const { getMyStructs, addStruct, editStruct, deleteStruct, getMyStructById } = require('../controllers/structController');
const { uploadStrukHandler } = require('../controllers/uploadController');
const { upload } = require('../config/multer');
const router = express.Router();

// Struct routes for handling CRUD operations
router.get('/', getMyStructs);
router.get('/:id', getMyStructById);
router.post('/', addStruct);
router.put('/:id', editStruct);
router.delete('/:id', deleteStruct);

// Endpoint for uploading struk images to text and output to JSON
router.post('/upload', upload.single('struk_image'), uploadStrukHandler);

module.exports = router;