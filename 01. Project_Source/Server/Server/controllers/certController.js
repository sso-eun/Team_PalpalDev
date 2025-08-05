const mysql = require("mysql2/promise");
const db = mysql.createPool({
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});

//전체조회
exports.list = async (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const offset = (page - 1) * limit;

    try {
        const [countRows] = await db.execute(`SELECT COUNT(*) AS total FROM guardian_auth_upload`);
        const total = countRows[0].total;
        const totalPages = Math.ceil(total / limit);

        const sql = `
            SELECT
                gau.req_no,
                gau.guardian_no,
                g.user_id AS guardian_id,
                gau.senior_num,
                s.user_id AS senior_id,
                gau.certificate_img,
                gau.status,
                gau.submitted_at,
                gau.reviewed_at,
                gau.reviewer_admin_no,
                gau.reviewer_note
            FROM guardian_auth_upload gau
                     LEFT JOIN member g ON gau.guardian_no = g.user_num
                     LEFT JOIN member s ON gau.senior_num = s.user_num
            ORDER BY gau.req_no DESC
                LIMIT ${limit} OFFSET ${offset};

        `;

        const [rows] = await db.query(sql);

        res.status(200).json({
            rsCode :200,
            totalResults: total,
            totalPages,
            currentPage: page,
            limit,
            results: rows,
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ rsCode :-1 ,message: '요청값을 다시 확인하세요.', error });
    }
};
// end list

//단일조회
exports.getlistById = async (req, res) => {
    const { req_no } = req.params;
    try {
        const sql = `SELECT * FROM guardian_auth_upload WHERE req_no = ?`;
        const [rows] = await db.execute(sql, [req_no]);

        if (rows.length === 0) {
            return res.status(404).json({rsCode :404, message: '해당 요청 없음' });
        }
        res.json(rows[0]);
    } catch (err) {
        res.status(500).json({rsCode :-1, message: '요청값을 다시 확인하세요.' });
    }
};

//단일조회
exports.getlistByNum = async (req, res) => {
    const { req_no } = req.params;
    try {
        const sql = `SELECT * FROM guardian_auth_upload WHERE guardian_no  = ?`;
        const [rows] = await db.execute(sql, [req_no]);

        if (rows.length === 0) {
            return res.status(404).json({rsCode :404, message: '해당 요청 없음' });
        }
        res.json(rows[0]);
    } catch (err) {
        res.status(500).json({rsCode :-1, message: '요청값을 다시 확인하세요.' });
    }
};

exports.update = async (req, res) => {
    const { req_no } = req.params;
    const { status, reviewer_admin_no, reviewer_note } = req.body;

    try {
        const sql = `
      UPDATE guardian_auth_upload
      SET status = ?, reviewed_at = NOW(), reviewer_admin_no = ?, reviewer_note = ?
      WHERE req_no = ?
    `;
        await db.execute(sql, [status, reviewer_admin_no, reviewer_note, req_no]);

        res.json({rsCode :200, message: '수정 완료' });
    } catch (err) {
        res.status(500).json({rsCode :res.status, message: '수정 실패', error: err });
    }
};