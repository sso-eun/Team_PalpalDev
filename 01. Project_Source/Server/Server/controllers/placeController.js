// 2025-05-21
// 사용자 위치 기반 장소 API (DB 기반으로 리팩토링 완료)
// author: eunjae
// test
// http://localhost:3000/places?category=hospital&lat=36.63&lon=127.45&range=0.5
// http://localhost:3000/places?category=senior_center&lat=36.63&lon=127.45&range=1

const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });
const mysql = require('mysql2/promise');
const { getDistance } = require('../utils/distance');

// 2025-05-22
// DB에서 가져오는 방식으로 변겅함
// DB 커넥션 풀 설정
const pool = mysql.createPool({
    // 로컬 DB
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    // user: process.env.DB_USER_MY,
    // password: process.env.DB_PASSWORD_MY,
    // database: process.env.DB_NAME,

    // Dundun DB
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// 앱 요청
// 유저 근처 장소 가져오기
exports.getPlaces = async (req, res) => {

    // 요청 파라미터
    const { category, lat, lon, range } = req.query;

    const userLat = parseFloat(lat);
    const userLon = parseFloat(lon);
    const searchRadius = parseFloat(range) || 1;

    if (isNaN(userLat) || isNaN(userLon)) {
        return res.status(400).json({ error: 'lat, lon은 유효한 숫자여야 합니다.' });
    }
    // 카테고리 → pl_type 매핑
    // let pl_type;
    // if (category === 'hospital') pl_type = 0;
    // else if (category === 'shelter') pl_type = 1;
    // else if (category === 'care') pl_type = 2;

    const CATEGORY_MAP = {
        'hospital': 0,
        'shelter': 1,
        'care': 2
    };

    const pl_type = CATEGORY_MAP[category];
    if (pl_type === undefined) {
        return res.status(400).json({ error: '지원하지 않는 category입니다.' });
    }

    // DB에서 해당 카테고리에 맞는 장소 불러온다.
    try {
        const conn = await pool.getConnection();
        const [rows] = await conn.query(
            `SELECT 
                pl_name AS name, 
                pl_addr AS address, 
                pl_tel AS phone, 
                pl_lat AS lat, 
                pl_lon AS lon 
             FROM place 
             WHERE pl_type = ? AND pl_display = 1`,
            [pl_type]
        );
        conn.release();

        // 각 장소에 대해 사용자와의 거리 계산
        // getDistance() 외부 파일에 함수 존재
        const result = rows
            .map(place => ({
                ...place,
                distance: getDistance(userLat, userLon, place.lat, place.lon)
            }))

            .filter(p => p.distance <= searchRadius)        // 반경 범위 내 장소만 필터링
            .sort((a, b) => a.distance - b.distance);       // 거리 오름차순 정렬(가까운 순으로)

        console.log(`[응답] ${category} ${result.length}개 반환 (반경 ${searchRadius}km 내)`);
        return res.json(result);

    } catch (err) {
        // Db 조회 실패시 500서버 오류 반환
        console.error('DB 조회 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};

// 2025-05-25
// CRUD For admin use
// 전체 조회
// exports.getAllPlacesForAdmin = async (req, res) => {
//     try {
//         const conn = await pool.getConnection();
//         const [rows] = await conn.query("SELECT * FROM place");
//         conn.release();
//         return res.json(rows);
//     } catch (err) {
//         console.error('조회 실패:', err);
//         return res.status(500).json({ error: '서버 오류' });
//     }
// };

// 2025-05-27 조회 방식 update
// read all notification & pagiNation
exports.getAllPlacesForAdmin = async (req, res) => {
    const page = parseInt(req.query.page) || 1;
    const unit = parseInt(req.query.limit) || 10; // 여기까진 OK

    if (page < 1 || unit < 1) {
        return res.status(400).json({ error: 'page와 limit은 1 이상의 정수여야 합니다.' });
    }

    const offset = (page - 1) * unit;

    try {
        const conn = await pool.getConnection();

        // 전체 개수 조회 (총 페이지 계산용)
        const [[{ count }]] = await conn.query("SELECT COUNT(*) AS count FROM place");

        // 실제 데이터 조회
        const [rows] = await conn.query(
            "SELECT * FROM place ORDER BY pl_no DESC LIMIT ? OFFSET ?",
            [unit, offset]
        );

        conn.release();

        return res.json({
            currentPage: page,
            unit: unit,
            totalCount: count,
            totalPages: Math.ceil(count / unit),
            data: rows
        });

    } catch (err) {
        console.error('페이지네이션 조회 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};

// 장소 등록
// pl_name, pl_addr, pl_tel, pl_lat, pl_lon, pl_type는 필수로 받아와야함
// 나머지는 NULL로 채워 넣음
exports.createPlace = async (req, res) => {
    const {
        pl_name,
        pl_postNumber,
        pl_addr,
        pl_detailAddr,
        pl_tel,
        pl_lat,
        pl_lon,
        pl_type,
        pl_display
    } = req.body;

    // 필수값 체크
    if (
        !pl_name ||
        !pl_addr ||
        !pl_tel ||
        pl_lat == null ||
        pl_lon == null ||
        pl_type == null
    ) {
        return res.status(400).json({
            error: '필수 항목(pl_name, pl_addr, pl_tel, pl_lat, pl_lon, pl_type)이 누락됨'
        });
    }

    try {
        const conn = await pool.getConnection();
        await conn.query(
            `INSERT INTO place
             (pl_name, pl_postNumber, pl_addr, pl_detailAddr, pl_tel, pl_lat, pl_lon, pl_type, pl_display, pl_write, pl_update)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())`,
            [
                pl_name,
                pl_postNumber || '',
                pl_addr,
                pl_detailAddr || '',
                pl_tel,
                pl_lat,
                pl_lon,
                pl_type,
                pl_display ?? 1
            ]
        );
        conn.release();
        return res.json({ status: "success", message: "장소 등록 완료" });
    } catch (err) {
        console.error('등록 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};

// 장소 삭제
exports.deletePlace = async (req, res) => {
    const { pl_no } = req.params;

    try {
        const conn = await pool.getConnection();
        const [result] = await conn.query(`DELETE FROM place WHERE pl_no = ?`, [pl_no]);
        conn.release();

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: "pl_no에 해당하는 장소 없음" });
        }

        return res.json({ status: "deleted", message: "장소 삭제 완료" });
    } catch (err) {
        console.error('삭제 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};


// 장소 일부 수정 (PATCH)
exports.patchPlace = async (req, res) => {
    // 클라이언트가 보낸 장소 번호 추출
    const { pl_no } = req.params;
    
    // 클라이언트가 보내준 필드 -> 수정할 내용
    const updateFields = req.body;
    
    // pl_no는 필수 파라미터
    if (!pl_no) {
        return res.status(400).json({ error: 'pl_no 누락' });
    }
    // body가 비었을 경우 -> 수정할 내용이 없음
    if (!updateFields || Object.keys(updateFields).length === 0) {
        return res.status(400).json({ error: '수정할 필드가 없습니다' });
    }

    // 허용된 필드만 업데이트 허용
    // 하나만 수정하고 싶으면 하나만 body에 작성해주면 됨
    // ex)
    // { "pl_name":"수정 병원" }

    const allowedFields = [
        'pl_name', 'pl_postNumber', 'pl_addr', 'pl_detailAddr',
        'pl_tel', 'pl_lat', 'pl_lon', 'pl_type', 'pl_display'
    ];
    
    // 실제로 요청한 필드에서 필터링해서 추출
    const validUpdates = Object.keys(updateFields)
        .filter(key => allowedFields.includes(key));

    if (validUpdates.length === 0) {
        return res.status(400).json({ error: '유효한 수정 필드가 없습니다' });
    }

    try {
        // DB 커넥션
        const conn = await pool.getConnection();

        const setClause = validUpdates
            .map(field => `${field} = ?`)
            .join(', ');
        const values = validUpdates.map(field => updateFields[field]);

        const sql = `UPDATE place SET ${setClause}, pl_update = NOW() WHERE pl_no = ?`;

        const [result] = await conn.query(sql, [...values, pl_no]);
        conn.release();

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: '해당 pl_no의 장소를 찾을 수 없습니다' });
        }

        return res.json({ status: 'patched', message: '장소 일부 수정 완료' });
    } catch (err) {
        console.error('장소 일부 수정 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};

// 전체 데이터를 모두 작성해야하는 방식
// // 장소 수정
// exports.updatePlace = async (req, res) => {
//     const { pl_no } = req.params;
//     const {
//         pl_name,
//         pl_postNumber,
//         pl_addr,
//         pl_detailAddr,
//         pl_tel,
//         pl_lat,
//         pl_lon,
//         pl_type,
//         pl_display
//     } = req.body;
//
//     try {
//         const conn = await pool.getConnection();
//         const [result] = await conn.query(
//             `UPDATE place SET
//                 pl_name = ?,
//                 pl_postNumber = ?,
//                 pl_addr = ?,
//                 pl_detailAddr = ?,
//                 pl_tel = ?,
//                 pl_lat = ?,
//                 pl_lon = ?,
//                 pl_type = ?,
//                 pl_display = ?,
//                 pl_update = NOW()
//              WHERE pl_no = ?`,
//             [pl_name, pl_postNumber || '', pl_addr, pl_detailAddr || '', pl_tel || '', pl_lat, pl_lon, pl_type, pl_display, pl_no]
//         );
//         conn.release();
//
//         if (result.affectedRows === 0) {
//             return res.status(404).json({ error: "pl_no에 해당하는 장소 없음" });
//         }
//
//         return res.json({ status: "updated", message: "장소 수정 완료" });
//     } catch (err) {
//         console.error('수정 실패:', err);
//         return res.status(500).json({ error: '서버 오류' });
//     }
// };