// 2025-05-17
// author: eunjae

const express = require('express');
const router = express.Router();
const placeController = require('../controllers/placeController');

router.get('/', placeController.getPlaces);

// 관리자용 CRUD
router.get('/admin', placeController.getAllPlacesForAdmin);
router.post('/admin', placeController.createPlace);
router.delete('/admin/:pl_no', placeController.deletePlace);

// 수정
router.patch('/admin/:pl_no', placeController.patchPlace);
// router.put('/admin/:pl_no', placeController.updatePlace);

module.exports = router;
