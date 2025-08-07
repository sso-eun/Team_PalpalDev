const path = require('path');
// const db = require('../config/database');
const mysql = require("mysql2/promise"); // DB 연결

const supabase = require('../utils/supabaseClient');
const fs = require('fs');

const db = mysql.createPool({
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});


//supabase_Upload
async function supabaseUploader(filePath, fileName,subfolder) {
    const fileBuffer = fs.readFileSync(filePath);

    const { data, error } = await supabase.storage
        .from('uploads')
        .upload(`${subfolder}/${fileName}`, fileBuffer, {
            contentType: 'image/*',
            upsert: true,
        });

    if (error) throw error;
    return data;
}

exports.uploadToSupabaseCert = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ rsCode:404, message: '파일이 존재하지 않습니다.' });
        }

        // const filePath = req.file.path;
        const filePath = path.resolve(req.file.resizedPath || req.file.path);
        const fileName = `${Date.now()}_${req.file.originalname}`;

        const result = await supabaseUploader(filePath, fileName,'cert');

        // 로컬 파일 삭제
        fs.unlinkSync(filePath);

        //db filename 저장
        const { user_num } = req.params;
        const { senior_num } = req.params;
        const sql = `
                            INSERT INTO guardian_auth_upload
                                (guardian_no, senior_num, certificate_img, status, submitted_at)
                            VALUES (?, ?, ?, 0, NOW())
                        `;

        await db.execute(sql, [user_num, senior_num, fileName]);

        res.status(200).json({
            rsCode : 200,
            message: '업로드 성공',
            path: result.path,
            // fullURL: `${process.env.SUPABASE_URL}/storage/v1/object/public/cert/${fileName}`,
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ rsCode:-1, message: '업로드 실패', error: err.message });
    }
};

exports.supaUploadProfileImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode :404, message: '파일이 존재하지 않습니다.' });
    }

    try {
        const filePath = path.resolve(req.file.resizedPath || req.file.path);
        const fileName = `${Date.now()}_${req.file.originalname}`;
        const result = await supabaseUploader(filePath, fileName,'profile');
        // 로컬 파일 삭제
        fs.unlinkSync(filePath);
        const sql = `
                          UPDATE member
                          SET user_profile_img = ?
                          WHERE user_num = ?
                         `;
        await db.execute(sql, [fileName, user_num]);

        res.status(200).json({
            rsCode :200,
            message: '프로필 이미지 업로드 성공',
            path: result.path,
            });

    } catch (error) {
        console.error(error);
        res.status(500).json({ rsCode :-1, message: '요청값을 다시 확인하세요.', error });
    }
};


exports.supaUploadTalkImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode: 404, message: '파일이 존재하지 않습니다.' });
    }

    try {
        const filePath = path.resolve(req.file.resizedPath || req.file.path);
        const fileName = `${Date.now()}_${req.file.originalname}`;

        const result = await supabaseUploader(filePath, fileName,'talk');

        const sql = `
                        INSERT INTO talk_upload (img_user, img_url)
                        VALUES (?, ?)
                    `;

        await db.execute(sql, [user_num, fileName]);

        return res.status(200).json({
            rsCode: 200,
            message: '토크 이미지 업로드 성공',
            path: result.path,
        });
    } catch (error) {
        console.error("토크 이미지 업로드 실패:", error);
        res.status(500).json({ rsCode: -1, message: '요청값을 다시 확인하세요.', error });
    }
};

//=================================아래는 25.08.06 부터 안씁니다===========================================//

exports.uploadProfileImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode :404, message: '파일이 존재하지 않습니다.' });
    }

    try {
        // const filePath = path.join('uploads', file.filename);
        // const fileName = file.filename;
        const fileName = path.basename(file.resizedPath); // e.g. abc_123_resized.jpg
        const filePath = path.join('uploads', 'profile', fileName);

        const sql = `
                          UPDATE member
                          SET user_profile_img = ?
                          WHERE user_num = ?
                         `;
        await db.execute(sql, [fileName, user_num]);

        res.status(200).json({ rsCode :200, message: '프로필 이미지 업로드 성공', filePath });
    } catch (error) {
        console.error(error);
        res.status(500).json({ rsCode :-1, message: '요청값을 다시 확인하세요.', error });
    }
};


exports.uploadTalkImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode: 404, message: '파일이 존재하지 않습니다.' });
    }

    try {
        // const filePath = path.join('uploads', 'talk', file.filename);
        // const fileName = file.filename;

        const fileName = path.basename(file.resizedPath); // e.g. abc_123_resized.jpg
        const filePath = path.join('uploads', 'talk', fileName);

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
        res.status(500).json({ rsCode: -1, message: '요청값을 다시 확인하세요.', error });
    }
};

exports.uploadCertImage = async (req, res) => {
    const { user_num } = req.params;
    const { senior_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ rsCode: 404, message: '파일이 존재하지 않습니다.' });
    }

    try {
        // const filePath = path.join('uploads', 'cert', file.filename);
        // const fileName = file.filename;

        const fileName = path.basename(file.resizedPath); // e.g. abc_123_resized.jpg
        const filePath = path.join('uploads', 'cert', fileName);

        const sql = `
                            INSERT INTO guardian_auth_upload
                                (guardian_no, senior_num, certificate_img, status, submitted_at)
                            VALUES (?, ?, ?, 0, NOW())
                        `;

        await db.execute(sql, [user_num, senior_num, fileName]);

        return res.status(200).json({
            rsCode: 200,
            message: '증명서 이미지 업로드 성공',
            filePath
        });
    } catch (error) {
        console.error("증명서 이미지 업로드 실패:", error);
        res.status(500).json({ rsCode: -1, message: '요청값을 다시 확인하세요.', error });
    }
};
