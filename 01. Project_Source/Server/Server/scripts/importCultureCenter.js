

// importCultureCenter.js
const fs = require('fs');
const path = require('path');
// const iconv = require('iconv-lite');
const csv = require('csv-parser');
const mysql = require('mysql2/promise');

require('dotenv').config({ path: path.join(__dirname, '../.env') });

// 로컬 DB 설정
const pool = mysql.createPool({
    // local DB
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    // user: process.env.DB_USER_MY,
    // password: process.env.DB_PASSWORD_MY,

    // Dundun DB
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
});

// CSV 파일 경로
const filePath = path.join(__dirname, '../data/cheongju_CultureCenter_2025.csv');

(async () => {
    const rows = [];

    fs.createReadStream(filePath)
        .pipe(csv({ separator: ',' }))
        .on('data', (row) => {


            // 모든 key 정리 (BOM, 공백 제거)
            const cleanedRow = {};
            for (let key in row) {
                const cleanKey = key.replace(/^\uFEFF/, '').trim();
                cleanedRow[cleanKey] = row[key];
            }
            console.log(Object.keys(row));

            const name = cleanedRow['시설명'];
            const lat = parseFloat(cleanedRow['위도']);
            const lon = parseFloat(cleanedRow['경도']);

            if (!isNaN(lat) && !isNaN(lon)) {
                rows.push([
                    // row['시설명'],       // cc_name
                    name,
                    row['우편주소'] || '',   // cc_postNumber
                    row['시설주소'] || '',   // cc_addr
                    row['상세주소'] || '',   // cc_detailAddr
                    row['전화번호'] || '',   // cc_tel
                    row['홈페이지'] || '',   // cc_link
                    1,                       // cc_display
                    lat,                     // cc_lat
                    lon,                     // cc_lon
                    new Date(),              // cc_write
                    new Date(),              // cc_update
                ]);
            }
        })
        .on('end', async () => {
            console.log(`${rows.length}개 문화센터 데이터 준비 완료.`);

            const sql = `
                INSERT INTO culture_center
                (cc_name, cc_postNumber, cc_addr, cc_detailAddr, cc_tel, cc_link, cc_display, cc_lat, cc_lon, cc_write, cc_update)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `;

            const conn = await pool.getConnection();
            try {
                for (const row of rows) {
                    await conn.query(sql, row);
                }
                console.log('문화센터 데이터 DB 삽입 완료');
            } catch (err) {
                console.error('삽입 중 오류:', err);
            } finally {
                conn.release();
                process.exit();
            }
        });
})();