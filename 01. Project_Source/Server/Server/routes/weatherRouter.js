// 2025-05-22
// add router
// author : eunjae

const express = require('express');
const router = express.Router();
const weatherController = require('../controllers/weatherController');

router.use('/', weatherController);

module.exports = router;
