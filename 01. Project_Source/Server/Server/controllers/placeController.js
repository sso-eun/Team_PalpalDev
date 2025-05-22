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
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// 장소 API - GET /api/places?category=hospital&lat=36.63&lon=127.45&range=1
exports.getPlaces = async (req, res) => {
    const { category, lat, lon, range } = req.query;

    if (!lat || !lon) {
        return res.status(400).json({ error: 'lat, lon 필수' });
    }

    const userLat = parseFloat(lat);
    const userLon = parseFloat(lon);
    const searchRadius = parseFloat(range) || 1;

    // 카테고리 → pl_type 매핑
    let pl_type;
    if (category === 'hospital') pl_type = 0;
    else if (category === 'senior_center') pl_type = 1;
    else return res.status(400).json({ error: '지원하지 않는 category입니다.' });

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

        // 거리 계산 후 필터링 및 정렬
        const result = rows
            .map(place => ({
                ...place,
                distance: getDistance(userLat, userLon, place.lat, place.lon)
            }))
            .filter(p => p.distance <= searchRadius)
            .sort((a, b) => a.distance - b.distance);

        console.log(`[응답] ${category} ${result.length}개 반환 (반경 ${searchRadius}km 내)`);
        return res.json(result);

    } catch (err) {
        console.error('DB 조회 실패:', err);
        return res.status(500).json({ error: '서버 오류' });
    }
};
