const express = require('express');
const router = express.Router();
const fcmController = require('../controllers/fcmController');

/**
 * @route POST /fcm/send-test-notifications
 * @description 개발/테스트 목적으로, 스케줄링된 알림 전송 로직을 수동으로 트리거하는 API 엔드포인트.
 * 이 엔드포인트로 POST 요청을 보내면 즉시 알림 전송 로직이 실행됩니다.
 */
router.post('/send-test-notifications', async (req, res) => {
    try {
        // 로그: 수동 트리거가 발생했음을 알림
        console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Manual trigger for sendScheduledNotifications initiated.`);

        // fcmController의 핵심 알림 전송 함수를 호출
        await fcmController.sendScheduledNotifications();

        // 성공 응답을 보냄
        res.status(200).send('Scheduled notifications triggered manually.');
    } catch (error) {
        // 오류 발생 시, 콘솔에 에러를 로깅하고 클라이언트에 오류 응답 보냄
        console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Error triggering scheduled notifications manually:`, error);
        res.status(500).send('Failed to trigger scheduled notifications manually.');
    }
});



// 이 라우터 객체를 내보내어 main 애플리케이션 파일(index.js)에서 사용할 수 있도록 함
module.exports = router;