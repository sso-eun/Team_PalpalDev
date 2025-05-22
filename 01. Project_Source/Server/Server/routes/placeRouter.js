// 2025-05-17
// author: eunjae

const express = require('express');
const router = express.Router();
const placeController = require('../controllers/placeController');

router.get('/', placeController.getPlaces);

module.exports = router;
