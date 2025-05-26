// 2025-05-23
// culture center
// author : eunjae
// test
// http://localhost:3000/culture_center?lat=36.64&lon=127.48

// const pool = require('../db'); // DB 연결
const { getDistance } = require('../utils/distance_culture');
const mysql = require('mysql2/promise');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// 전체 동작 흐름
// 앱에서 사용자 위도/경도 (lat, lon)를 포함한 요청을 보냄
// 예: GET /culture_center?lat=36.64&lon=127.48
//
//     서버는 DB에서 모든 문화센터의 위도/경도 정보를 가져옴
//
// 각 문화센터에 대해 사용자 위치와의 거리를 계산
//
// 가장 가까운 문화센터를 선택하고, 이름/링크/거리 정보를 클라이언트에 응답함
// 함수 선언 방식 (이름이 있어야 module.exports가 찾을 수 있음)
async function getNearestCultureCenter(req, res) {
    const { lat, lon } = req.query;

    if (!lat || !lon) {
        return res.status(400).json({ error: '위도/경도 파라미터가 필요합니다.' });
    }

    const userLat = parseFloat(lat);
    const userLon = parseFloat(lon);

    if (isNaN(userLat) || isNaN(userLon)) {
        return res.status(400).json({ error: '위도/경도가 유효하지 않습니다.' });
    }
    // DB에서 모든 장소들 가져옴
    const conn = await pool.getConnection();
    try {
        const [rows] = await conn.query('SELECT * FROM culture_center');

        let nearest = null;
        let minDist = Infinity;
        
        // 각 문화센터에 대한 거리 계산 수행
        // 사용자 위도, 경도와 장소의 위도, 경도를 이용해 거리 계산
        // distance_culture에서 계산
        rows.forEach(center => {

            if(center.cc_lat && center.cc_lon){
                const dist = getDistance(userLat, userLon, center.cc_lat, center.cc_lon);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = { ...center, distance: dist };
                }
            }
        });
        // 이 경우는 DB에 아무 정보도 없을 때
        // 정확히는 위도 경도 등록된 게 아무것도 없을 때!
        if (!nearest) {
            return res.status(404).json({ error: '근처 시설을 찾을 수 없습니다.' });
        }

        res.json({
            name: nearest.cc_name,
            link: nearest.cc_link,
            distance: Math.round(nearest.distance)
        });

    } catch (e) {
        console.error(e);
        res.status(500).json({ error: '서버 오류' });
    } finally {
        conn.release();
    }
}

// export는 함수 선언 이후에!
module.exports = { getNearestCultureCenter };
