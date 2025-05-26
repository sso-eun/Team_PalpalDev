// 2025-05-20
// CSV파일 DB에 저장
// author : eunjae

// dundun_sql 비밀번호 필요함
// 새로운 DB 데이터 열 추가해야함



const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');
const csv = require('csv-parser');
const mysql = require('mysql2/promise');

// .env 파일을 읽어서 process.env 변수에 로드
require('dotenv').config({ path: path.join(__dirname, '../.env') });



// 카테고리 번호 : (pl_type)
// 0 병원
// 1 경로당
// 2 쉼터

// 일단 로컬 서버 기준으로 작성
// DB 연결 설정 (.env에서 로컬 DB 접속 정보 불러오기)
const pool = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER_MY,
    password: process.env.DB_PASSWORD_MY,
    database: process.env.DB_NAME,
    // waitForConnections: true,
    // connectionLimit: 10
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
                    row['소재지도로명주소'],    // pl_addr
                    '.',                      // pl_detailAddr
                    row['전화번호'],            // pl_tel
                    lat,                     // pl_lat
                    lon,                     // pl_lon
                    1,                       // pl_display (공개 여부: 1)
                    2,                       // pl_type (보호센터: 2)
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
/* eslint-disable no-undef */
/* eslint-disable @typescript-eslint/no-unused-vars */

