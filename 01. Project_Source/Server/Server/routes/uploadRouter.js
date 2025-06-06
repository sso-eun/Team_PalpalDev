const express = require('express');
const router = express.Router();
// const upload = require('../controllers/multerConfig'); // multer 설정
const { uploadProfile, uploadTalk } = require('../controllers/multerConfig');

const uploadController = require('../controllers/uploadController');

// POST /upload/profile/:user_num
router.post('/profile/:user_num', uploadProfile.single('file'), uploadController.uploadProfileImage);
router.post('/talk/:user_num', uploadTalk.single('file'), uploadController.uploadTalkImage);

module.exports = router;
