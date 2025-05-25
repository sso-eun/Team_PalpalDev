// 2025-05-24
// notification router
// author : eunjae

const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');

// 전체 알림 목록 조회 - 성공
router.get('/', notificationController.getAllNotifications);
// 알림 등록 - 성공
// router.post('/', notificationController.createNotification);
// 알림 수정 - 성공
router.put('/:id', notificationController.updateNotification);
// 알림 삭제 - 성공
router.delete('/:id', notificationController.deleteNotification);
// 알림 전송 (nt_result = 1로 변경)
router.post('/:id/send', notificationController.sendNotification);

// 어떤 방식이 좋을까요?
// 명시적인 경로 방식이 앱에서는 이해가 쉬울 거 같은데,,,
router.post('/register', notificationController.createNotification);
// router.get('/getAll', notificationController.getAllNotifications);
// router.put('/edit/:id', notificationController.updateNotification);
// router.delete('/remove/:id', notificationController.deleteNotification);
// router.post('/send/:id', notificationController.sendNotification);
// http://localhost:3000/notifications/register

module.exports = router;