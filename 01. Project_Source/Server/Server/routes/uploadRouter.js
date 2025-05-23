const express = require('express');
const router = express.Router();
const upload = require('../config/multerConfig'); // multer 설정
const uploadController = require('../controllers/uploadController');


// POST /upload/profile/:user_num
router.post('/profile/:user_num', upload.single('file'), uploadController.uploadProfileImage);

module.exports = router;
