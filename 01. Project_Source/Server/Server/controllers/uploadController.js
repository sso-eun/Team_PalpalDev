const path = require('path');
// const db = require('../config/database');
const mysql = require("mysql2/promise"); // DB 연결

const db = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    // host: process.env.DB_SERVER_HOST,
    // port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});


exports.uploadProfileImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode :400, message: '파일이 존재하지 않습니다.' });
    }

    try {
        const filePath = path.join('uploads', file.filename);
        const fileName = file.filename;

        const sql = `
                          UPDATE member
                          SET user_profile_img = ?
                          WHERE user_num = ?
                         `;
        await db.execute(sql, [fileName, user_num]);

        res.status(200).json({ rsCode :200, message: '프로필 이미지 업로드 성공', filePath });
    } catch (error) {
        console.error(error);
        res.status(500).json({ rsCode :502, message: '서버 오류', error });
    }
};


exports.uploadTalkImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode: 400, message: '파일이 존재하지 않습니다.' });
    }

    try {
        const filePath = path.join('uploads', 'talk', file.filename);
        const fileName = file.filename;

        const sql = `
                            INSERT INTO talk_upload (img_user, img_url)
                            VALUES (?, ?)
                        `;

        await db.execute(sql, [user_num, fileName]);

        return res.status(200).json({
            rsCode: 200,
            message: '토크 이미지 업로드 성공',
            filePath
        });
    } catch (error) {
        console.error("토크 이미지 업로드 실패:", error);
        res.status(500).json({ rsCode: 502, message: '서버 오류', error });
    }
};

