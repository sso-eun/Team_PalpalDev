// 2025-05-16
// Member_..
// author : Soeun

const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');

// DB 연결 설정
const db = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// 회원가입 API
exports.signup = async (req, res) => {
    const {
        user_type,
        user_id,
        user_pw,
        user_tel,
        user_profile_img,
        user_home_lat,
        user_home_lot,
        user_condition,
    } = req.body;

    // 필수 값 체크
    if ((!user_type && user_type !== 0) || !user_id || !user_pw || !user_tel) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        // ID 중복 체크
        const [existRows] = await db.execute(
            'SELECT COUNT(*) AS count FROM Member WHERE user_id = ?',
            [user_id]
        );
        if (existRows[0].count > 0) {
            return res.status(409).json({ message: '이미 존재하는 ID입니다.' });
        }

        const test_ID = "testID"

        const hashedPw = await bcrypt.hash(user_pw, 10); // saltRounds: 10

        const userData = {
            user_type,
            test_ID,
            user_pw: hashedPw,
            user_tel,
            user_profile_img,
            user_home_lat,
            user_home_lot,
            user_condition
        };

        const values = Object.values(userData);

        const sql = `
            INSERT INTO Member (
                ${Object.keys(userData).join(', ')}, user_signup, user_update, user_recent
            ) VALUES (
                ${new Array(values.length).fill('?').join(', ')}, CURDATE(), CURDATE(), NOW()
            )
        `;

        const [result] = await db.execute(sql, values);

        res.status(201).json({ message: '회원가입 성공', user_num: result.insertId });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};
