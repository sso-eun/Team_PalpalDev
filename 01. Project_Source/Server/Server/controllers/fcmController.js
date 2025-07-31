// fcmController.js (수정된 코드)
// const path = require('path');
// const mysql = require('mysql2/promise');


const admin = require('../firebaseInit');

// 단계 4에서 완성한 서비스 파일을 불러옴
// 'scheduleService.js' 찾음
const scheduleService = require('../utils/scheduleService');

// 일정이 없을 때 사용될 메시지 풀 (오전/오후 분리) - 이 메시지 풀은 컨트롤러에 있습니다.
// TODO: 여건 되면 날씨에 따라 메세지 다르게 뜨도록 해보기
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
 * @description Node-cron 스케줄러에 의해 호출될 핵심 알림 전송 함수.
 * 모든 사용자에게 일정을 확인하고 맞춤형 알림 전송
 */

exports.sendScheduledNotifications = async () => {
    // 로그: 현재 시간과 함께 작업 시작알림
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Scheduled notification check started...`);

    try {
        // 1단계: scheduleService를 통해 DB에서 모든 사용자 정보 (user_num과 user_token 포함) 가져옴
        // scheduleService.js에서 정의한 getAllUsersWithFcmTokens 함수 호출
        const users = await scheduleService.getAllUsersWithFcmTokens();

        if (!users || users.length === 0) {
            console.log('No registered users with FCM tokens found. Skipping notification.');
            return; // 알림을 보낼 사용자가 없으면 함수 종료
        }

        // 2단계: 현재 시간을 기준으로 사용될 메시지 풀 (오전/오후)을 결정.
        const currentHour = new Date().getHours();
        let selectedMessagePool;

        // 오전 5시부터 11시 59분까지를 '오전'으로 간주
        if (currentHour >= 5 && currentHour < 12) {
            selectedMessagePool = dayMessagesPool;

        } else { // 그 외 시간 (주로 오후 6시 알림이 실행될 때)
            selectedMessagePool = nightMessagesPool;
        }

        // 3단계: 모든 사용자에게 반복하며 알림을 준비하고 전송합니다.
        for (const user of users) {
            const { user_num, user_token } = user; // 각 사용자의 user_num과 user_token을 추출

            // FCM 토큰이 없거나 유효하지 않으면 (null, undefined, 빈 문자열 등) 해당 사용자는 건너띔
            // 로그 띄우기
            if (!user_token) {
                console.log(`User ${user_num} has no valid FCM token. Skipping notification.`);
                continue; // 다음 사용자로 넘어갑니다.
            }

            // TODO: 나중에 알림 종류에 따라 제목 바꾸는 것도 고려해볼만 함
            let notificationTitle = "팔팔한 하루"; // 알림의 기본 제목
            let notificationBody = "";           // 알림의 본문 내용

            try {
                // (1) scheduleService를 통해 해당 사용자의 다음날/모레 일정을 조회
                // scheduleService.js에서 정의한 getUpcomingScheduleForUser 함수를 호출
                // 일정이 있다면 일정 제목이랑 날짜를 반환함
                const upcomingSchedule = await scheduleService.getUpcomingScheduleForUser(user_num);

                let scheduleDetails = null;
                if (upcomingSchedule) {
                    
                    // 일정이 존재하면, scheduleDetails 객체에 날짜와 제목을 저장
                    scheduleDetails = {
                        date: upcomingSchedule.date,    // DB에서 가져온 user_date_time (Date 객체 또는 문자열)
                        title: upcomingSchedule.title
                    };
                }

                // (2) 일정 유무에 따라 알림 본문 내용을 결정합니다.
                // 일정이 있을 경우: "[일정날짜] 에 [일정제목]이 예정되어있네요~" 형식으로 메시지 구성
                // DB에서 가져온 날짜(`scheduleDetails.date`)가 Date 객체가 아닐 수 있으므로 `new Date()`로 변환하여 포매팅함.
                if (scheduleDetails) {
                    const scheduleDateObj = new Date(scheduleDetails.date);
                    const formattedDate = `${scheduleDateObj.getMonth() + 1}월 ${scheduleDateObj.getDate()}일`;
                    notificationBody = `${formattedDate}에 ${scheduleDetails.title}이 예정되어있네요~`;
                } else {
                    // 일정이 없을 경우: 현재 시간에 맞는 메시지 풀에서 랜덤으로 메시지 선택
                    const randomIndex = Math.floor(Math.random() * selectedMessagePool.length);
                    notificationBody = selectedMessagePool[randomIndex];
                }

                // (3) Firebase Cloud Messaging(FCM) 메시지 객체를 구성
                const message = {
                    notification: { // 사용자 기기에 표시될 알림의 시각적인 부분
                        title: notificationTitle,
                        body: notificationBody
                    },
                    // 이 알림을 받을 특정 기기의 FCM 토큰 (각 사용자마다 다름)
                    token: user_token,
                    // 앱이 백그라운드나 종료 상태일 때도 수신될 수 있는 추가 데이터 (모두 문자열 형태여야 함)
                    data: {
                        // 일정이 있는 경우 schedule / 일정이 없는 경우 general
                        type: scheduleDetails ? 'schedule' : 'general', // 알림 유형: 'schedule' 또는 'general'
                        scheduleDate: scheduleDetails ? new Date(scheduleDetails.date).toISOString() : '', // 일정 날짜 (ISO 8601 형식 문자열)
                        scheduleTitle: scheduleDetails ? scheduleDetails.title : '', // 일정 제목
                        userNum: String(user_num) // 사용자 번호를 문자열로 변환하여 보냅니다.
                    }
                };

                // (4) Firebase Admin SDK를 사용하여 구성된 FCM 메시지를 전송
                // 여기서 최종적으로 FCM 서버에 실제 알림 요청을 하는 함수
                // 그리고 알림 요청에 대한 결과를 response가 받게 된다!
                const response = await admin.messaging().send(message);

                // 성공적으로 전송된 로그를 남깁니다. 토큰은 길기 때문에 앞부분만 표시
                console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Successfully sent message to user ${user_num} (token: ${user_token.substring(0, 10)}...):`, response);

                // TODO: (선택 사항 - 현재 보류 기능) 알림 전송 성공 시 알림 DB에 기록 로직 추가
                // 이 부분은 나중에 구현 (예: 어떤 사용자에게, 어떤 내용으로, 언제, 성공/실패 여부 등)
                // 예: await notificationLogService.saveNotificationLog(user_num, notificationTitle, notificationBody, new Date(), 'SUCCESS');
                // notification 관련 API 만들어놔서 그대로 사용하면 됨.

                // 2025-07-30 추가
                // notification API 사용해서 기록
                try {
                    await notificationController.createNotificationRecord(notificationTitle, notificationBody);
                    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Notification successfully recorded in DB for user ${user_num}.`);
                } catch (dbError) {
                    // DB 기록 실패는 FCM 전송 실패와 별개이므로 분리하여 에러 처리
                    console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Failed to record notification in DB for user ${user_num}:`, dbError);
                }

            } catch (error) {
                // 개별 사용자에게 알림 전송 중 발생한 에러를 로깅
                console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Error sending message to user ${user_num} (token: ${user_token.substring(0, 10)}...):`, error);

                // 유효하지 않은 FCM 토큰 에러 코드 확인 및 DB에서 해당 토큰 삭제
                if (error.code === 'messaging/registration-token-not-registered' || error.code === 'messaging/invalid-argument') {
                    console.log(`Invalid FCM token for user ${user_num}. Deleting from DB.`);

                    // scheduleService에 토큰 삭제 함수를 호출
                    await scheduleService.deleteInvalidFcmToken(user_num, user_token);
                }
            }
        }
    } catch (mainError) {
        // FCM 토큰을 DB에서 가져오는 과정이나, 전체 프로세스에서 발생한 치명적인 에러를 로깅
        console.error(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Critical error during notification process:`, mainError);
    }

    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Scheduled notification check finished.`);
};