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
const coolsms = require('coolsms-node-sdk').default;
// apiKey, apiSecret 설정
const messageService = new coolsms(process.env.COOL_API, process.env.COOL_API_SECRET);

//  send API
const generateAuthCode = () => Math.floor(100000 + Math.random() * 900000).toString();

exports.send = async (req, res) => {
    const { tel_num } = req.body;
    const tel_num_str = tel_num.toString();
    const from_num = process.env.COOL_FROM_CALL.toString();
    const authCode = generateAuthCode();

    const createAt = new Date();
    const expiresAt = new Date(createAt.getTime() + 10 * 60 * 1000); // 10분 후

    try {
        messageService.sendOne({
            to: tel_num_str,
            from: from_num,
            text: `[든든하이!] \n 인증번호는 ${authCode} 입니다.`
        })
            .then(async result => {
                const statusCode = result.statusCode;
                const statusMessage = result.statusMessage;


                if (statusCode === '2000') {
                    await insertAuthCodeLog(db, tel_num_str, authCode, 1);
                    return res.status(200).json({ rsCode :`${statusCode}`, message: `문자 발송 성공: ${statusMessage}` });
                } else {
                    await insertAuthCodeLog(db, tel_num_str, authCode, 0);
                    return res.status(400).json({ rsCode :`${statusCode}`, message: `문자 발송 실패: ${statusMessage} (코드: ${statusCode})` });
                }
            })
            .catch(async err => {
                console.error('문자 발송 오류:', err);
                await insertAuthCodeLog(db, tel_num_str, authCode, 0);
                return res.status(500).json({ rsCode :`${statusCode}`, message: '문자 발송 실패', error: err });
            });

    } catch (error) {
        console.error('요청값을 다시 확인하세요.:', error);
        return res.status(500).json({ rsCode :502, message: '요청값을 다시 확인하세요.', error });
    }
};
// end send API
//
// verify-code
exports.verifyCode = async (req, res) => {
    const { tel_num, auth_code } = req.body;

    const sql = `
            SELECT * FROM tel_auth_code
            WHERE tel_num = ?
            ORDER BY create_at DESC
            LIMIT 1;
          `;

    try {
        const [rows] = await db.execute(sql, [tel_num]);

        if (rows.length === 0) {
            return res.status(400).json({ rsCode :404,message: '인증번호가 존재하지 않습니다.' });
        }

        const record = rows[0];

        if (record.is_verified) {
            return res.status(400).json({rsCode :400, message: '이미 인증된 번호입니다.' });
        }

        const now = new Date();
        const expiresAt = new Date(record.expires_at);

        if (now > expiresAt) {
            return res.status(400).json({ rsCode :500,message: '인증번호가 만료되었습니다.' });
        }

        if (record.tel_auth_code !== auth_code) {
            return res.status(400).json({ rsCode :400, message: '인증번호가 일치하지 않습니다.' });
        }

        // ✅ 인증 성공 → 인증 상태 업데이트
        const updateQuery = `
                              UPDATE tel_auth_code
                              SET is_verified = 1
                              WHERE code_no = ?;
                            `;
        await db.execute(updateQuery, [record.code_no]);

        return res.status(200).json({ rsCode :200, message: '인증 성공!' });

    } catch (error) {
        console.error('인증 오류:', error);
        return res.status(500).json({ rsCode :500, message: '요청값을 다시 확인하세요.', error });
    }



};
//end verifyCode



async function insertAuthCodeLog(db, tel_num_str, authCode, isSendFlag) {
    const insertSQL = `
    INSERT INTO tel_auth_code (
                      tel_num,
                      tel_auth_code,
                      create_at,
                      expires_at,
                      is_verified,
                      is_send
                    )
                    VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 10 MINUTE), 0, ?);
                  `;

    try {
        await db.execute(insertSQL, [tel_num_str, authCode, isSendFlag]);
        console.log('인증번호 발송 내역 저장 완료');
    } catch (err) {
        console.error('인증번호 저장 실패:', err);
        throw err;
    }
}