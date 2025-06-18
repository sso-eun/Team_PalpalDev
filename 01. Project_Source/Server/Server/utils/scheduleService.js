// const path = require('path');
// const mysql = require('mysql2/promise');
// const admin = require('../firebaseInit');

const db = require('../db');

exports.getAllUsersWithFcmTokens = async () => {
    try {
        // 'member' 테이블에서 'user_num'과 'user_token' 컬럼을 조회합니다.
        const sql = `
            SELECT user_num, user_token
            FROM member
            WHERE user_token IS NOT NULL AND user_token != '' -- NULL이 아니고 빈 문자열이 아닌 유효한 FCM 토큰만 가져옵니다.
        `;
        const [rows] = await db.execute(sql);

        return rows.map(row => ({
            user_num: row.user_num,
            user_token: row.user_token
        }));
    } catch (error) {
        console.error('Error fetching all users with FCM tokens:', error);
        throw error;
    }
};

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