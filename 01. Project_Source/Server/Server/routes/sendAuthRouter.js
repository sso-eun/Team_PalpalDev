// 2025-05-27
// send_Router
// author : Soeun

const express = require('express');
const router = express.Router();
const sendController = require('../controllers/sendAuthController');


router.post('/send', sendController.send);
router.post('/verifyCode', sendController.verifyCode);



module.exports = router;
