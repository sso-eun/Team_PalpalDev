const path = require('path');
const db = require('../config/database'); // DB 연결

exports.uploadProfileImage = async (req, res) => {
    const { user_num } = req.params;
    const file = req.file;

    if (!file) {
        return res.status(400).json({ message: '파일이 존재하지 않습니다.' });
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

        res.status(200).json({ message: '프로필 이미지 업로드 성공', filePath });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};
