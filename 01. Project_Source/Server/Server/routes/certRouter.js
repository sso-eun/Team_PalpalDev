// 2025-08-01
// cert_Router
// author : Soeun

const express = require('express');
const router = express.Router();
const certController = require('../controllers/certController');

router.get('/list', certController.list);
router.get('/getlistById', certController.getlistById);
router.get('/getlistByNum', certController.getlistByNum);
router.put('/update/:req_no', certController.update);


module.exports = router;