// const path = require('path');
// const mysql = require('mysql2/promise');
// const admin = require('../firebaseInit');

const db = require('../db');

// 모든 유저에 대해서 토큰 불러옴
// 토큰 없는 사람은 NULL로 세팅돼있음
exports.getAllUsersWithFcmTokens = async () => {
    try {
        // 'member' 테이블에서 'user_num'과 'user_token' 컬럼을 조회
        // NULL이 아니고 빈 문자열이 아닌 유효한 FCM 토큰만 가져옴
        const sql = `
            SELECT user_num, user_token
            FROM member
            WHERE user_token IS NOT NULL AND user_token != '' 
        `;
        const [rows] = await db.execute(sql);

        // 유저 고유번호랑 유저 토큰 반환시킴
        return rows.map(row => ({
            user_num: row.user_num,
            user_token: row.user_token
        }));
    } catch (error) {
        console.error('Error fetching all users with FCM tokens:', error);
        throw error;
    }
};
// 일정이 있다면 일정 제목이랑 날짜를 반환함
exports.getUpcomingScheduleForUser = async (userNum) => {
    try {
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        const dayAfterTomorrow = new Date(today);
        dayAfterTomorrow.setDate(today.getDate() + 2);

        const formatDateForDb = (date) => {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };

        const tomorrowStr = formatDateForDb(tomorrow);
        const dayAfterTomorrowStr = formatDateForDb(dayAfterTomorrow);

        const sql = `
            SELECT user_date_time, user_date_title
            FROM member_date  
            WHERE user_num = ?
              AND DATE(user_date_time) IN (?, ?)
            ORDER BY user_date_time ASC
            LIMIT 1
        `;

        const [rows] = await db.execute(sql, [userNum, tomorrowStr, dayAfterTomorrowStr]);

        if (rows.length > 0) {
            return {
                date: rows[0].user_date_time,
                title: rows[0].user_date_title
            };
        }
        return null;
    } catch (error) {
        console.error(`Error fetching upcoming schedule for user ${userNum}:`, error);
        throw error;
    }
};
/**
 * @function deleteInvalidFcmToken
 * @description DB에서 유효하지 않거나 만료된 FCM 토큰을 삭제
 * @param {number} userNum - 사용자 번호.
 * @param {string} fcmToken - 삭제할 FCM 토큰.
 */
// 유효하지 않은 FCM 토큰 삭제함
exports.deleteInvalidFcmToken = async (userNum, fcmToken) => {
    try {
        // TODO: 사용하는 DB 스키마에 맞게 다음 SQL 쿼리 선택 및 최종 확인할 것!
        // Option 1: 'member' 테이블에 user_token 컬럼이 있는 경우 (1:1 매핑)
        // 해당 user_num의 user_token을 NULL로 업데이트
        const sql = `
            UPDATE member
            SET user_token = NULL
            WHERE user_num = ? AND user_token = ?;
        `;

        // Option 2: 'member_fcm_token'과 같은 별도 테이블을 사용하는 경우 (1:N 매핑)
        // 해당 user_num과 fcmToken에 매칭되는 레코드를 삭제합니다.
        /*
        const sql = `
            DELETE FROM member_fcm_token
            WHERE user_num = ? AND fcm_token = ?;
        `;
        */
        // 만약 fcm_token이 고유하고 user_num이 갱신되는 방식이라면
        /*
        const sql = `
            DELETE FROM member_fcm_token
            WHERE fcm_token = ?;
        `;
        */

        const [result] = await db.execute(sql, [userNum, fcmToken]);
        console.log(`Successfully processed invalid FCM token for user ${userNum}. Rows affected: ${result.affectedRows}`);

    } catch (error) {
        console.error(`Error deleting invalid FCM token for user ${userNum} (token: ${fcmToken.substring(0, 10)}...):`, error);
        throw error;
    }
};