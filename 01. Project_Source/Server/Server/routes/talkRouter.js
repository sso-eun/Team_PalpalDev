// 2025-05-29
// talk_Router
// author : Soeun

const express = require('express');
const router = express.Router();
const sendController = require('../controllers/talkSendController');

router.post('/send', sendController.send);
router.post('/list', sendController.list);
router.post('/read', sendController.isRead);


module.exports = router;