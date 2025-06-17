const express = require('express');
const router = express.Router();
const placeController = require('../controllers/fcmController');

router.post('/send', fcm)