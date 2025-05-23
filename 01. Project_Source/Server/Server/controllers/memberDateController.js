// 2025-05-16
// Member_DATE_API
// author : Soeun
const db = require('../config/database');

//생성
exports.createUserDate = async (req, res) => {
    const {
        user_num,
        user_date_title,
        user_date_time,
        user_date_info
    } = req.body;

    if (!user_num || !user_date_title || !user_date_time) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const sql = `
                  INSERT INTO member_date (
                    user_num, user_date_title, user_date_time, user_date_info
                  ) VALUES (?, ?, ?, ?)
                         `;

        const values = [user_num, user_date_title, user_date_time, user_date_info || null];
        const [result] = await db.execute(sql, values);

        res.status(201).json({
            message: '일정이 성공적으로 생성되었습니다.',
            user_date_no: result.insertId
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};


//일정 수정
exports.updateUserDate = async (req, res) => {
    const {
        user_date_title,
        user_date_time,
        user_date_info
    } = req.body;

    const { user_date_no } = req.params;

    if (!user_date_no || !user_date_title || !user_date_time) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const sql = `
                          UPDATE member_date
                          SET user_date_title = ?, user_date_time = ?, user_date_info = ?
                          WHERE user_date_no = ?
                        `;

        await db.execute(sql, [
            user_date_title,
            user_date_time,
            user_date_info || null,
            user_date_no
        ]);

        res.status(200).json({ message: '일정 수정 성공' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};

//삭제
exports.deleteUserDate = async (req, res) => {
    const { user_date_no } = req.params;

    if (!user_date_no) {
        return res.status(400).json({ message: 'user_date_no가 필요합니다.' });
    }

    try {
        const sql = `DELETE FROM member_date WHERE user_date_no = ?`;
        await db.execute(sql, [user_date_no]);

        res.status(200).json({ message: '일정 삭제 성공' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};

//조회
exports.getUserDates = async (req, res) => {
    const { user_num } = req.query;
    // const user_num = parseInt(req.params);
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const offset = (page - 1) * limit;

    if (!user_num) {
        return res.status(400).json({ message: 'user_num이 필요합니다.' });
    }

    try {
        // 전체 개수 조회
        const countSql = `SELECT COUNT(*) as total FROM member_date WHERE user_num = ?`;
        const [countRows] = await db.execute(countSql, [user_num]);
        const total = countRows[0].total;
        const totalPages = Math.ceil(total / limit);

        // 페이징된 데이터 조회
        const sql = `
            SELECT user_date_no, user_date_title, user_date_time, user_date_info
            FROM member_date
            WHERE user_num = ${user_num}
            ORDER BY user_date_time DESC
            LIMIT ${limit} OFFSET ${offset}
        `;
        const [rows] = await db.execute(sql, [parseInt(user_num), limit, offset]);

        // console.log(sql);

        res.status(200).json({
            totalResults: total,
            totalPages,
            currentPage: page,
            results: rows
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
    }
};
