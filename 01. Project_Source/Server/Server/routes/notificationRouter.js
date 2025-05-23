// 2025-05-24
// notification router
// author : eunjae

const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');

// 전체 알림 목록 조회
router.get('/', notificationController.getAllNotifications);

// 알림 등록 - 성공
router.post('/', notificationController.createNotification);

// 알림 수정
router.put('/:id', notificationController.updateNotification);

// 알림 삭제
router.delete('/:id', notificationController.deleteNotification);

// 알림 전송 (nt_result = 1로 변경)
router.post('/:id/send', notificationController.sendNotification);

module.exports = router;


