// 2025-05-25
// notification CRUD API
// author : eunjae

const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// 전체 알림 조회
exports.getAllNotifications = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM notification_list ORDER BY nt_date DESC');
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};


// 알림 등록
exports.createNotification = async (req, res) => {


    console.log('요청 body:', req.body);
    const { nt_title, nt_content } = req.body;
    try {
        await pool.query(
            'INSERT INTO notification_list (nt_title, nt_content, nt_result) VALUES (?, ?, ?)',
            [nt_title, nt_content, 0]
        );
        res.status(201).json({ message: '알림이 등록되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 알림 수정
exports.updateNotification = async (req, res) => {

    // 디버깅용 추후 삭제 예정
    console.log('req.params.id:', req.params.id);
    console.log('req.body:', req.body);

    const { id } = req.params;
    const { nt_title, nt_content } = req.body;
    try {
        const [result] = await pool.query(
            'UPDATE notification_list SET nt_title = ?, nt_content = ? WHERE nt_no = ?',
            [nt_title, nt_content, id]
        );
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });
        res.json({ message: '알림이 수정되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 알림 삭제
exports.deleteNotification = async (req, res) => {
    const { id } = req.params;
    try {
        const [result] = await pool.query('DELETE FROM notification_list WHERE nt_no = ?', [id]);
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });
        res.json({ message: '알림이 삭제되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 알림 전송 처리
exports.sendNotification = async (req, res) => {
    const { id } = req.params;
    try {
        const [result] = await pool.query(
            'UPDATE notification_list SET nt_result = 1 WHERE nt_no = ?',
            [id]
        );
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });

        // 실제 앱 푸시 연동 로직 추가 가능 (예: FCM, 알림 API 등)

        res.json({ message: '알림이 전송 처리되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
