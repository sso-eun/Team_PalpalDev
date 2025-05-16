const express = require('express');
const router = express.Router();
const memberController = require('../controllers/memberController');

// POST /member/signup
router.post('/signup', memberController.signup);
router.post('/login', memberController.login);
router.post('/findid', memberController.findId);
router.put('/profile/:user_num', memberController.updateProfile);
router.put('/password/:user_num', memberController.updatePassword);



module.exports = router;
