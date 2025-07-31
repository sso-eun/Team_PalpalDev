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
        req_no, guardian_no, senior_num, certificate_img, status,
        submitted_at, reviewed_at, reviewer_admin_no, reviewer_note
      FROM guardian_auth_upload
      ORDER BY req_no DESC
      LIMIT ${limit} OFFSET ${offset}
    `;

        const [rows] = await db.query(sql);

        res.status(200).json({
            totalResults: total,
            totalPages,
            currentPage: page,
            limit,
            results: rows,
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '서버 오류', error });
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
            return res.status(404).json({ message: '해당 요청 없음' });
        }
        res.json(rows[0]);
    } catch (err) {
        res.status(500).json({ message: '서버 오류' });
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

        res.json({ message: '수정 완료' });
    } catch (err) {
        res.status(500).json({ message: '수정 실패', error: err });
    }
};