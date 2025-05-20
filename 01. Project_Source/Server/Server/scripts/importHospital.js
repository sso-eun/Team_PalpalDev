// 2025-05-20
// CSV파일 DB에 저장
// author : eunjae

const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');
const csv = require('csv-parser');
const mysql = require('mysql2/promise');

// DB 연결 설정
const pool = mysql.createPool({
    host: 'svc.sel4.cloudtype.app',
    port: 31030,
    user: 'dundun',
    password: '여기에_비밀번호', // TODO: 실제 비밀번호로 교체
    database: 'dundunhi',
    waitForConnections: true,
    connectionLimit: 10
});

// 병원 CSV 파일 경로
const filePath = path.join(__dirname, '../data/cheongju_hospital_2022.csv');

(async () => {
    const rows = [];

    // CSV 파일을 CP949 인코딩으로 읽고 파싱
    fs.createReadStream(filePath)
        .pipe(iconv.decodeStream('cp949'))
        .pipe(csv({ separator: ',' }))
        .on('data', (row) => {
            const lat = parseFloat(row['위도']);
            const lon = parseFloat(row['경도']);

            // 병원 중 "안전상비의약품"은 제외 (편의점 포함 방지)
            if (
                row['의료시설구분'] !== '안전상비의약품' &&
                !isNaN(lat) &&
                !isNaN(lon)
            ) {
                rows.push([
                    row['기관명'],             // pl_name
                    '',                       // pl_postNumber
                    row['기관 소재지'],       // pl_addr
                    '',                       // pl_detailAddr
                    row['전화번호'],          // pl_tel
                    lat,                      // pl_lat
                    lon,                      // pl_lon
                    1,                        // pl_display
                    1,                        // pl_type (병원: 1)
                    new Date(),              // pl_write
                    new Date()               // pl_update
                ]);
            }
        })
        .on('end', async () => {
            console.log(`${rows.length}개 병원 데이터 준비 완료.`);

            const sql = `
        INSERT INTO place
        (pl_name, pl_postNumber, pl_addr, pl_detailAddr, pl_tel, pl_lat, pl_lon, pl_display, pl_type, pl_write, pl_update)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `;

            const conn = await pool.getConnection();
            try {
                for (const row of rows) {
                    await conn.query(sql, row);
                }
                console.log('병원 데이터 DB 삽입 완료');
            } catch (err) {
                console.error('삽입 중 오류:', err);
            } finally {
                conn.release();
                process.exit();
            }
        });
})();
