const express = require('express');
const router = express.Router();

const downController = require('../controllers/downloadController');

// POST /down/profile/:user_num
router.get('/cert/:req_no', downController.getCert);
router.get('/profile/:user_num', downController.getProfile);
router.get('/talk/:talk_id', downController.getTalkImage);
//지난이야기 공사 필요

module.exports = router;
