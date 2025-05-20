// 2025-05-20
// CSV파일 DB에 저장
// author : eunjae

const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');
const csv = require('csv-parser');
const mysql = require('mysql2/promise');

// DB 연결 설정 (실제 서버 환경에 맞게 설정)
const pool = mysql.createPool({
    host: 'svc.sel4.cloudtype.app',
    port: 31030,
    user: 'dundun',
    password: '여기에_비밀번호', // TODO: 실제 비밀번호로 교체하세요
    database: 'dundunhi',
    waitForConnections: true,
    connectionLimit: 10
});

// CSV 파일 경로 설정 (경로당용)
const filePath = path.join(__dirname, '../data/cheongju_SeniorCenter_2024.CSV');

(async () => {
    const rows = [];

    // CSV 파일을 CP949로 디코딩해서 한 줄씩 파싱
    fs.createReadStream(filePath)
        .pipe(iconv.decodeStream('cp949'))
        .pipe(csv({ separator: ',' }))
        .on('data', (row) => {
            const lat = parseFloat(row['위도']);
            const lon = parseFloat(row['경도']);

            if (!isNaN(lat) && !isNaN(lon)) {
                // DB에 삽입할 데이터 행 구성
                rows.push([
                    row['시설명'],            // pl_name
                    '',                      // pl_postNumber
                    row['소재지도로명주소'], // pl_addr
                    '',                      // pl_detailAddr
                    row['전화번호'],         // pl_tel
                    lat,                     // pl_lat
                    lon,                     // pl_lon
                    1,                       // pl_display (공개 여부: 1)
                    2,                       // pl_type (경로당: 2)
                    new Date(),             // pl_write (작성일)
                    new Date()              // pl_update (수정일)
                ]);
            }
        })
        .on('end', async () => {
            console.log(`${rows.length}개 경로당 데이터 준비 완료.`);

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
                console.log('경로당 데이터 DB 삽입 완료');
            } catch (err) {
                console.error('삽입 중 오류:', err);
            } finally {
                conn.release();
                process.exit();
            }
        });
})();
