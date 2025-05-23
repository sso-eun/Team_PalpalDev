const express = require('express');
const router = express.Router();
const cultureController = require('../controllers/cultureController'); // ✔ 수정됨

router.get('/', cultureController.getNearestCultureCenter);

module.exports = router;