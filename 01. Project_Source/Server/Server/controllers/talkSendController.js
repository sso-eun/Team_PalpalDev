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

//발송처리
exports.send = async (req, res) => {
    const { sender_type, sender_id, receiver_id, image_url } = req.body;

    const sql = `
                    INSERT INTO talk_list (
                      sender_type, sender_id, receiver_id, image_url, send_at, is_read
                    ) VALUES (?, ?, ?, ?, NOW(), 0);
                     `;

    try {
        await db.execute(sql, [sender_type, sender_id, receiver_id, image_url]);
        return res.status(201).json({ rsCode :200,  message: '전송 완료' });
    } catch (error) {
        console.error('이미지 전송 실패:', error);
        return res.status(500).json({rsCode :502,  message: '서버 오류', error });
    }
};
// end send


//메세지 조회
exports.list = async (req, res) => {
    const user_num = parseInt(req.body.user_num);

    const sql = `
                        SELECT talk_id, sender_type, sender_id, image_url, send_at, is_read, read_at
                        FROM talk_list 
                        WHERE receiver_id = ? OR sender_id = ?
                        ORDER BY send_at ASC
                      `;

    try {
        const [rows] = await db.execute(sql, [user_num,user_num]);
        return res.status(200).json({
            rsCode :200,
            message: '조회 성공',
            data: rows
        });
    } catch (error) {
        console.error('조회 실패:', error);
        return res.status(500).json({
            rsCode :502,
            message: '서버 오류',
            error
        });
    }
};
// end list

exports.isRead = async (req, res) => {
    const { talk_id } = req.body;

    const sql = `
                UPDATE talk_list
                SET is_read = 1,
                    read_at = NOW()
                WHERE talk_id = ?
              `;

    try {
        const [result] = await db.execute(sql, [talk_id]);

        if (result.affectedRows === 0) {
            return res.status(404).json({ rsCode: 404, message: '대상이 없습니다.' });
        }

        return res.status(200).json({ rsCode: 200, message: '읽음 처리 완료' });

    } catch (error) {
        console.error('읽음 처리 실패:', error);
        return res.status(500).json({ rsCode: 502, message: '서버 오류', error });
    }
};
// end isREad
