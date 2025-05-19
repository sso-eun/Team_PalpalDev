// 2025-05-16
// Member_Router
// author : Soeun

const express = require('express');
const router = express.Router();
const memberController = require('../controllers/memberController');


router.post('/signup', memberController.signup);
router.post('/login', memberController.login);
router.post('/findid', memberController.findId);
router.put('/profile/:user_num', memberController.updateProfile);
router.put('/password/:user_num', memberController.updatePassword);
router.get('/allusers', memberController.getAllMembers);
router.get('/getmember/:user_num', memberController.getMember);
router.get('/searchmember', memberController.searchMembers);



module.exports = router;
