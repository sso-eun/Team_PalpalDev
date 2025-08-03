// 2025-05-16
// Member_API
// author : Soeun
// const db = require('../config/database'); // 상대 경로 주의
const bcrypt = require('bcrypt');
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



// 회원가입 API
// 회원 선택값 필드 없어도 되게끔 수정 필요.
exports.signup = async (req, res) => {
    const {
        user_type,
        user_id,
        user_pw,
        user_tel,
        user_profile_img,
        user_home_lat,
        user_home_lot,
        user_condition,
    } = req.body;

    if ((!user_type && user_type !== 0) || !user_id || !user_pw || !user_tel) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const [existRows] = await db.execute(
            'SELECT COUNT(*) AS count FROM member WHERE user_id = ?',
            [user_id]
        );
        if (existRows[0].count > 0) {
            return res.status(409).json({ message: '이미 존재하는 ID입니다.' });
        }


        const hashedPw = await bcrypt.hash(user_pw, 10); // saltRounds: 10

        const userData = {
            user_type,
            user_id,
            user_pw: hashedPw,
            user_tel,
            user_profile_img,
            user_home_lat,
            user_home_lot,
            user_condition
        };

        const values = Object.values(userData);

        const sql = `
            INSERT INTO member (
                ${Object.keys(userData).join(', ')}, user_signup, user_update, user_recent
            ) VALUES (
                ${new Array(values.length).fill('?').join(', ')}, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), NOW()
            )
        `;

        const [result] = await db.execute(sql, values);

        res.status(201).json({ message: '회원가입 성공', user_num: result.insertId });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// end Signup API

//Login
exports.login = async (req, res) => {
    const { user_id, user_pw } = req.body;

    if (!user_id || !user_pw) {
        return res.status(400).json({ message: 'ID와 비밀번호를 입력하세요.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_num, user_pw FROM member WHERE user_id = ?',
            [user_id]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '존재하지 않는 ID입니다.' });
        }

        const user = rows[0];

        const isMatch = await bcrypt.compare(user_pw, user.user_pw);

        if (!isMatch) {
            return res.status(401).json({ message: '비밀번호가 일치하지 않습니다.' });
        }

        return res.status(200).json({ message: '로그인 성공', user_num: user.user_num });

    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// end Login

// find myId
exports.findId = async (req, res) => {
    const { user_tel } = req.body;

    if (!user_tel) {
        return res.status(400).json({ message: '전화번호를 입력해주세요.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_id, user_num FROM member WHERE user_tel = ?',
            [user_tel]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '해당 전화번호로 등록된 사용자가 없습니다.' });
        }

        return res.status(200).json({
            message: 'ID 조회 성공',
            user_id: rows[0].user_id,
            user_num: rows[0].user_num,
        });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// end find My ID

//Update MyProfile
exports.updateProfile = async (req, res) => {
    const { user_num } = req.params;
    const updateFields = req.body;

    if (!user_num) {
        return res.status(400).json({ message: '회원 번호가 필요합니다.' });
    }

    if (Object.keys(updateFields).length === 0) {
        return res.status(400).json({ message: '수정할 항목이 없습니다.' });
    }


    try {
        const setClause = Object.keys(updateFields)
            .map(field => `${field} = ?`)
            .join(', ');

    const sql = `
                      UPDATE member SET
                        ${setClause},
                        user_update = CURRENT_TIMESTAMP()
                      WHERE user_num = ?
                     `;

        const values = [...Object.values(updateFields), user_num];


        const [result] = await db.execute(sql, values);

        if (result.affectedRows === 0) {
            return res.status(404).json({ message: '회원 정보를 찾을 수 없습니다.' });
        }

        return res.status(200).json({ message: '회원정보가 성공적으로 수정되었습니다.' });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// end update myProfile

//update user Password
exports.updatePassword = async (req, res) => {
    const { user_num } = req.params;
    const { current_pw, new_pw } = req.body;

    if (!user_num || !current_pw || !new_pw) {
        return res.status(400).json({ message: '필수 항목이 누락되었습니다.' });
    }

    try {
        const [rows] = await db.execute(
            'SELECT user_pw FROM member WHERE user_num = ?',
            [user_num]
        );

        if (rows.length === 0) {
            return res.status(404).json({ message: '사용자를 찾을 수 없습니다.' });
        }

        const isMatch = await bcrypt.compare(current_pw, rows[0].user_pw);
        if (!isMatch) {
            return res.status(401).json({ message: '현재 비밀번호가 일치하지 않습니다.' });
        }

        const hashedNewPw = await bcrypt.hash(new_pw, 10);
        await db.execute(
            'UPDATE member SET user_pw = ?, user_update = CURRENT_TIMESTAMP() WHERE user_num = ?',
            [hashedNewPw, user_num]
        );

        return res.status(200).json({ message: '비밀번호가 성공적으로 변경되었습니다.' });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// end update user Password

// select all user & pagiNation
exports.getAllMembers = async (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const offset = (page - 1) * limit;

    try {
        const [countRows] = await db.execute('SELECT COUNT(*) AS total FROM member');
        const total = countRows[0].total;
        const totalPages = Math.ceil(total / limit);

        const sql = `
                              SELECT user_num, user_type, user_id, user_tel, user_profile_img,
                                     user_home_lat, user_home_lot, user_condition, user_signup
                              FROM member
                              ORDER BY user_num DESC
                              LIMIT ${limit} OFFSET ${offset}
                            `;

        const [rows] = await db.query(sql);
        res.status(200).json({
            totalResults : total,
            totalPages : totalPages,
            currentPage: page,
            limit,
            results: rows,
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};

//특정 회원 단일 조회
exports.getMember = async (req, res) => {
    const user_num = parseInt(req.params.user_num);

    if (isNaN(user_num)) {
        return res.status(400).json({ message: '잘못된 회원 번호입니다.' });
    }

    try {

        const sql =   `SELECT user_num, user_type, user_id, user_tel, user_profile_img,
                                     user_home_lat, user_home_lot, user_condition, user_signup
                              FROM member
                              WHERE user_num = ${user_num}
                              `;

        const [rows] = await db.query(sql);

        if (rows.length === 0) {
            return res.status(404).json({ message: '해당 회원을 찾을 수 없습니다.' });
        }

        res.status(200).json(rows[0]);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
//end getMember

//회원 검색 검색필드 + 검색어(2자이상)
exports.searchMembers = async (req, res) => {
    const { field, keyword } = req.query;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const offset = (page - 1) * limit;

    const allowedFields = [
        'user_type',
        'user_condition',
        'user_id',
        'user_tel',
        'user_home_lat',
        'user_home_lot'
    ];


    if (!allowedFields.includes(field)) {
        return res.status(400).json({ message: '잘못된 필드입니다.' });
    }

    try {
        let sql;
        if (['user_type', 'user_condition'].includes(field)) {

            sql = `
                    SELECT user_num, user_type, user_id, user_tel, user_profile_img,
                           user_home_lat, user_home_lot, user_condition, user_signup
                    FROM member
                    WHERE ${field} = ${db.escape(keyword)}
                    ORDER BY user_num DESC
                 `;
        } else {
            if (!field || !keyword || keyword.length < 2) {
                return res.status(400).json({ message: '검색어는 2자 이상 입력해주세요.' });
            }
            sql = `
                    SELECT user_num, user_type, user_id, user_tel, user_profile_img,
                           user_home_lat, user_home_lot, user_condition, user_signup
                    FROM member
                    WHERE ${field} LIKE '%${keyword}%'
                    ORDER BY user_num DESC
                 `;
        }

        const [rows] = await db.execute(sql, [`%${keyword}%`]);
        const total = rows.length;
        const pagedRows = rows.slice(offset, offset + limit);


        res.status(200).json({
            // totalResults: total,
            // totalPages,
            // currentPage: parseInt(page),
            // results: rows,
            totalResults : total,
            totalPages: Math.ceil(total / limit),
            currentPage : page,
            results: pagedRows
        });


    } catch (error) {
        console.error(error);
        res.status(500).json({ message: '요청값을 다시 확인하세요.', error });
    }
};
// search Member

//멤버 연락처 저장_단일
exports.saveMylist = async (req, res) => {
    const { user_num, user_og_name, user_og_tel_num, user_nw_name } = req.body;

    const sql = `
                INSERT INTO member_tel_list 
                (user_num, user_og_name, user_og_tel_num, user_nw_name)
                VALUES (?, ?, ?, ?)
            `;

    try {
        await db.execute(sql, [user_num, user_og_name, user_og_tel_num, user_nw_name]);

        return res.status(201).json({
            rsCode: 200,
            message: "연락처 저장 성공"
        });
    } catch (error) {
        console.error("연락처 저장 실패:", error);
        return res.status(500).json({
            rsCode: -1,
            message: "요청값을 다시 확인하세요.",
            error
        });
    }
};
//end saveMylist

//update MyList
exports.updateMyList = async (req, res) => {
    const { tel_no, user_nw_name } = req.body;

    const sql = `
                UPDATE member_tel_list
                SET user_nw_name = ?
                WHERE tel_no = ?
            `;

    try {
        const [result] = await db.execute(sql, [user_nw_name, tel_no]);

        if (result.affectedRows === 0) {
            return res.status(404).json({
                rsCode: 404,
                message: "수정할 데이터가 없습니다"
            });
        }

        return res.status(200).json({
            rsCode: 200,
            message: "연락처 이름 수정 성공"
        });
    } catch (error) {
        console.error("연락처 이름 수정 실패:", error);
        return res.status(500).json({
            rsCode: -1,
            message: "요청값을 다시 확인하세요.",
            error
        });
    }
};
//end updateMyList

//delete mylist
exports.deleteMyList = async (req, res) => {
    const { tel_no } = req.body;

    const sql = `
        DELETE FROM member_tel_list
        WHERE tel_no = ?
    `;

    try {
        const [result] = await db.execute(sql, [tel_no]);

        if (result.affectedRows === 0) {
            return res.status(404).json({
                rsCode: 404,
                message: "삭제할 데이터가 없습니다"
            });
        }

        return res.status(200).json({
            rsCode: 200,
            message: "연락처 삭제 성공"
        });
    } catch (error) {
        console.error("연락처 삭제 실패:", error);
        return res.status(500).json({
            rsCode: -1,
            message: "요청값을 다시 확인하세요.",
            error
        });
    }
};
//end delete myList

//유저넘버로 연락처 조회_페이지네이션x
exports.getMyList = async (req, res) => {
    const { user_num } = req.body;

    const sql = `
                    SELECT tel_no, user_num, user_og_name, user_og_tel_num, user_nw_name
                    FROM member_tel_list
                    WHERE user_num = ?
                    ORDER BY user_og_name ASC
                `;

    try {
        const [rows] = await db.execute(sql, [user_num]);

        return res.status(200).json({
            rsCode: 200,
            message: "연락처 조회 성공",
            data: rows
        });
    } catch (error) {
        console.error("연락처 조회 실패:", error);
        return res.status(500).json({
            rsCode: -1,
            message: "요청값을 다시 확인하세요.",
            error
        });
    }
};

// FCM_user_token_update
exports.updateFCM = async (req, res) => {
    const { user_num, user_token } = req.body;

    if (!user_num) {
        return res.status(400).json({rsCode :res.status, message: '회원 번호가 필요합니다.' });
    }

    try {
        const sql = `
                UPDATE member
                SET user_token = ?
                WHERE user_num = ?
            `;

        const [result] = await db.execute(sql, [user_token, user_num]);


        if (result.affectedRows === 0) {
            return res.status(404).json({ rsCode :res.status, message: '회원 정보를 찾을 수 없습니다.' });
        }

        return res.status(200).json({rsCode :res.status, message: '회원정보가 성공적으로 수정되었습니다.' });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ rsCode :res.status, message: '요청값을 다시 확인하세요.', error });
    }
};