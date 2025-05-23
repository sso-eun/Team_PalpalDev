// 2025-05-22
// MemberDate_Router
// author : Soeun

const express = require('express');
const router = express.Router();
const dateController = require('../controllers/memberDateController');



router.post('/setdate', dateController.createUserDate);
router.put('/update/:user_date_no', dateController.updateUserDate);
router.delete('/delete/:user_date_no', dateController.deleteUserDate);
router.get('/getdate', dateController.getUserDates);


module.exports = router;
