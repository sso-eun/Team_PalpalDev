const path = require('path');
const mysql = require('mysql2/promise');
const admin = require('../firebaseInit');









const dayMessagesPool = [
    "오늘도 좋은 하루 보내세요!",
    "날씨가 좋네요, 잠시 산책은 어떠세요?",
    "점심 식사는 맛있게 하셨나요?",
    "가족들에게 전화 한 통 어떠세요?"
];

const nightMessagesPool = [
    "저녁 식사는 맛있게 하셨나요?",
    "날씨가 좋네요, 잠시 산책은 어떠세요?",
    "내일은 무슨 일이 있을까요?",
    "이번 주 일정을 확인해보세요!"
];

/**
 * @function sendScheduledNotifications
 * @description 스케줄링에 따라 실행되어 사용자들에게 알림을 전송하는 핵심 함수.
 */
exports.sendScheduledNotifications = async () => {
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Scheduled notification check started...`);

    try {
        // ... (DB에서 FCM 토큰 가져오는 1단계 로직 - TODO 부분)
        const fcmTokens = [ /* YOUR_TEST_DEVICE_FCM_TOKEN_HERE */ ]; // 테스트용 토큰

        if (fcmTokens.length === 0) {
            console.log('No FCM tokens found for sending notifications. Skipping.');
            return;
        }

        // 현재 시간을 기준으로 오전/오후 메시지 풀을 선택
        const currentHour = new Date().getHours();
        let selectedMessagePool;
        if (currentHour >= 5 && currentHour < 12) { // 예를 들어 오전 5시부터 11시 59분까지를 오전으로 간주
            selectedMessagePool = dayMessagesPool;
        } else { // 그 외 시간 (오후 6시 알림에 해당)
            selectedMessagePool = nightMessagesPool;
        }

        for (const token of fcmTokens) {
            let notificationTitle = "팔팔한 하루";
            let notificationBody = "";

            try {
                // ... (멤버 일정 확인 로직 - TODO 2단계 부분)
                // 현재는 더미 데이터로 대체합니다.
                const hasSchedule = Math.random() < 0.7;
                let scheduleDetails = null;

                if (hasSchedule) {
                    const scheduleDate = new Date();
                    scheduleDate.setDate(scheduleDate.getDate() + (Math.random() < 0.5 ? 1 : 2));
                    scheduleDetails = {
                        date: scheduleDate,
                        title: "중요한 프로젝트 회의"
                    };
                }

                if (scheduleDetails) {
                    const formattedDate = `${scheduleDetails.date.getMonth() + 1}월 ${scheduleDetails.date.getDate()}일`;
                    notificationBody = `${formattedDate}에 ${scheduleDetails.title}이 예정되어있네요~`;
                } else {
                    // 일정이 없을 경우, 현재 시간에 맞는 메시지 풀에서 랜덤 선택
                    const randomIndex = Math.floor(Math.random() * selectedMessagePool.length);
                    notificationBody = selectedMessagePool[randomIndex];
                }

                // ... (FCM 메시지 구성 및 전송 3, 4단계 로직)
                const message = {
                    notification: {
                        title: notificationTitle,
                        body: notificationBody
                    },
                    token: token,
                    data: {
                        type: scheduleDetails ? 'schedule' : 'general',
                        scheduleDate: scheduleDetails ? scheduleDetails.date.toISOString() : '',
                        scheduleTitle: scheduleDetails ? scheduleDetails.title : ''
                    }
                };

                const response = await admin.messaging().send(message);
                console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Successfully sent message to token (${token.substring(0, 10)}...):`, response);

            } catch (error) {
                console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Error sending message to token (${token.substring(0, 10)}...):`, error);
            }
        }
    } catch (mainError) {
        console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Critical error during notification process:`, mainError);
    }

    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Scheduled notification check finished.`);
};