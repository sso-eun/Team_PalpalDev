// 2025-05-16
// Member_API
// author : Soeun

const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');

// DB 연결 설정
const db = mysql.createPool({
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
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

    if ((!user_type && user_type !== 0) || !user_id || !user_pw || !user_tel) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const [existRows] = await db.execute(
            'SELECT COUNT(*) AS count FROM member WHERE user_id = ?',
            [user_id]
        );
        if (existRows[0].count > 0) {
            return res.status(409).json({ message: '이미 존재하는 ID입니다.' });
        }


        const hashedPw = await bcrypt.hash(user_pw, 10); // saltRounds: 10

        const userData = {
            user_type,
            user_id,
            user_pw: hashedPw,
            user_tel,
            user_profile_img,
            user_home_lat,
            user_home_lot,
            user_condition
        };

        const values = Object.values(userData);

        const sql = `
            INSERT INTO member (
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
// end Signup API

//Login
exports.login = async (req, res) => {
    const { user_id, user_pw } = req.body;

    if (!user_id || !user_pw) {
        return res.status(400).json({ message: 'ID와 비밀번호를 입력하세요.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_num, user_pw FROM member WHERE user_id = ?',
            [user_id]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '존재하지 않는 ID입니다.' });
        }

        const user = rows[0];

        const isMatch = await bcrypt.compare(user_pw, user.user_pw);

        if (!isMatch) {
            return res.status(401).json({ message: '비밀번호가 일치하지 않습니다.' });
        }

        return res.status(200).json({ message: '로그인 성공', user_num: user.user_num });

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '서버 오류', error });
    }
};
// end Login

// find myId
exports.findId = async (req, res) => {
    const { user_tel } = req.body;

    if (!user_tel) {
        return res.status(400).json({ message: '전화번호를 입력해주세요.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_id, user_num FROM member WHERE user_tel = ?',
            [user_tel]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '해당 전화번호로 등록된 사용자가 없습니다.' });
        }

        return res.status(200).json({
            message: 'ID 조회 성공',
            user_id: rows[0].user_id,
            user_num: rows[0].user_num,
        });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '서버 오류', error });
    }
};
// end find My ID

//Update MyProfile
exports.updateProfile = async (req, res) => {
    const { user_num } = req.params;
    const updateFields = req.body;

    if (!user_num) {
        return res.status(400).json({ message: '회원 번호가 필요합니다.' });
    }

    if (Object.keys(updateFields).length === 0) {
        return res.status(400).json({ message: '수정할 항목이 없습니다.' });
    }


    try {
        const setClause = Object.keys(updateFields)
            .map(field => `${field} = ?`)
            .join(', ');

    const sql = `
                      UPDATE member SET
                        ${setClause},
                        user_update = CURDATE()
                      WHERE user_num = ?
                     `;

        const values = [...Object.values(updateFields), user_num];


        const [result] = await db.execute(sql, values);

        if (result.affectedRows === 0) {
            return res.status(404).json({ message: '회원 정보를 찾을 수 없습니다.' });
        }

        return res.status(200).json({ message: '회원정보가 성공적으로 수정되었습니다.' });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '서버 오류', error });
    }
};
// end update myProfile

//update user Password
exports.updatePassword = async (req, res) => {
    const { user_num } = req.params;
    const { current_pw, new_pw } = req.body;

    if (!user_num || !current_pw || !new_pw) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_pw FROM member WHERE user_num = ?',
            [user_num]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '사용자를 찾을 수 없습니다.' });
        }

        const isMatch = await bcrypt.compare(current_pw, rows[0].user_pw);
        if (!isMatch) {
            return res.status(401).json({ message: '현재 비밀번호가 일치하지 않습니다.' });
        }

        const hashedNewPw = await bcrypt.hash(new_pw, 10);
        await db.execute(
            'UPDATE member SET user_pw = ?, user_update = CURDATE() WHERE user_num = ?',
            [hashedNewPw, user_num]
        );

        return res.status(200).json({ message: '비밀번호가 성공적으로 변경되었습니다.' });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '서버 오류', error });
    }
};
// end update user Password