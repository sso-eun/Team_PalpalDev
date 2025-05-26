// 2025-05-25
// notification CRUD API
// author : eunjae

const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
    // local DB
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    // user: process.env.DB_USER_MY,
    // password: process.env.DB_PASSWORD_MY,

    // Dundun DB
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// creat notification
// nt_title nt_content는 필수 값
exports.createNotification = async (req, res) => {


    console.log('요청 body:', req.body);
    const { nt_title, nt_content } = req.body;

    // 필수 값 검사
    if (!nt_title?.trim() || !nt_content?.trim()) {
        return res.status(400).json({ error: 'nt_title과 nt_content는 필수이며 공백일 수 없습니다.' });
    }
    // 제목이랑 본문 100자 이하로 작성
    if (nt_title.length > 100 || nt_content.length > 100) {
        return res.status(400).json({ error: '제목 100자 이하, 내용 100자 이하로 입력해주세요.' });
    }

    try {
        await pool.query(
            'INSERT INTO notification_list (nt_title, nt_content, nt_result) VALUES (?, ?, ?)',
            [nt_title, nt_content, 0]
        );
        // nt_result를 디폴트로 0 지정 - 이게 읽음 안읽음 혹은 알림함 알림안함 으로 구분?
        res.status(201).json({ message: '알림이 등록되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};


// 전체 알림 조회
exports.getAllNotifications = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM notification_list ORDER BY nt_date DESC');
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 2025-05-26 조회 방식 update
// read all notification & pagiNation
// 정렬기준 없으면 update 순(최신순)으로 끊어서 보여준다 -> mysql의 'ordered by'
exports.getAllNotificationsPage = async (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const offset = (page - 1) * limit;

    try{
        // 총 개수 조회
        // const [countRows] = await pool.query('SELECT * FROM notification_list ORDER BY nt_date DESC');
        const [countRows] = await pool.query(`SELECT COUNT(*) AS total FROM notification_list`);
        const total = countRows[0].total;
        const totalPages = Math.ceil(total / limit);

        // 페이지별 데이터 조회
        const [rows] = await pool.query(`
            SELECT *
            FROM notification_list
            ORDER BY nt_no DESC
            LIMIT ? OFFSET ?
        `, [limit, offset]);

        res.status(200).json({
            totalResults : total,
            totalPages : totalPages,
            currentPage: page,
            limit,
            results: rows,
        });

    }catch (error){
        console.error(error);
        return res.status(500).json({ message: '서버 오류', error });
    }
}

// update notification (데이터 수정)
exports.updateNotification = async (req, res) => {

    // 디버깅용 추후 삭제 예정
    console.log('req.params.nt_no:', req.params.nt_no);
    console.log('req.body:', req.body);

    const { nt_no } = req.params;
    const { nt_title, nt_content } = req.body;

    // 필수값 검사
    if (!nt_title || !nt_content) {
        return res.status(400).json({ error: 'nt_title과 nt_content는 필수입니다.' });
    }
    // 제목이랑 본문 100자 이하로 작성
    if (nt_title.length > 100 || nt_content.length > 100) {
        return res.status(400).json({ error: '제목 100자 이하, 내용 100자 이하로 입력해주세요.' });
    }
    try {
        // nt_title이랑 nt_content는 필수값
        const [result] = await pool.query(
            'UPDATE notification_list SET nt_title = ?, nt_content = ? WHERE nt_no = ?',
            [nt_title, nt_content, nt_no]
        );
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });
        res.json({ message: '알림이 수정되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 알림 삭제
exports.deleteNotification = async (req, res) => {
    const { nt_no } = req.params;
    try {
        const [result] = await pool.query('DELETE FROM notification_list WHERE nt_no = ?', [nt_no]);
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });
        res.json({ message: '알림이 삭제되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// 알림 전송 처리
exports.sendNotification = async (req, res) => {
    const { nt_no } = req.params;
    try {
        const [result] = await pool.query(
            'UPDATE notification_list SET nt_result = 1 WHERE nt_no = ?',
            [nt_no]
        );
        if (result.affectedRows === 0) return res.status(404).json({ error: '해당 알림 없음' });

        // 실제 앱 푸시 연동 로직 추가 가능 (예: FCM, 알림 API 등)

        res.json({ message: '알림이 전송 처리되었습니다.' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};




// 2025-05-26 update
// read all notification & pagiNation

// exports.getAllNotificationsPage = async (req, res) => {
//     const page = parseInt(req.query.page) || 1;
//     const limit = parseInt(req.query.limit) || 10;
//     const offset = (page - 1) * limit;
//
//     try{
//         const [countRows] = await pool.query('SELECT COUNT(*) AS total FROM notification_list');
//         const total = countRows[0].total;
//         const totalPages = Math.ceil(total / limit);

//         const sql = `
//                               SELECT nt_title
//                               FROM notification_list
//                               ORDER BY nt_no DESC
//                               LIMIT ${limit} OFFSET ${offset}
//                             `;
//
//         const [rows] = await db.query(sql);
//
//         res.status(200).json({
//             totalResults : total,
//             totalPages : totalPages,
//             currentPage: page,
//             limit,
//             results: rows,
//         });
//
//     }catch (error){
//         console.error(error);
//         return res.status(500).json({ message: '서버 오류', error });
//     }
//
// }
