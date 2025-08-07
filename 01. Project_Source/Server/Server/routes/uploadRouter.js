const express = require('express');
const router = express.Router();
// const upload = require('../controllers/multerConfig'); // multer 설정
const { uploadProfile, uploadTalk,uploadCert, uploadSupa } = require('../controllers/multerConfig');

const uploadController = require('../controllers/uploadController');
const resizeImage = require("../utils/resizeImage");

// POST /upload/profile/:user_num
router.post('/profile/:user_num', uploadProfile.single('file'), resizeImage, uploadController.supaUploadProfileImage);
router.post('/talk/:user_num', uploadTalk.single('file'), resizeImage, uploadController.supaUploadTalkImage);
router.post('/cert/:user_num/:senior_num', uploadCert.single('file'), resizeImage, uploadController.uploadCertImage);
router.post('/supa-cert/:user_num/:senior_num', uploadSupa.single('file'), resizeImage, uploadController.uploadToSupabaseCert)

// router.post('/cert/:user_num/:senior_num', uploadCert.single('file'), uploadController.uploadCertImage);
// router.post('/upload-cert', uploadSupa.single('file'), resizeImage, uploadController.uploadToSupabaseCert)
// router.post('/profile/:user_num', uploadProfile.single('file'), resizeImage, uploadController.uploadProfileImage);
module.exports = router;
