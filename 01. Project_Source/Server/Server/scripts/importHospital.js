// 2025-05-20
// CSV파일 DB에 저장
// author : eunjae

// dundun_sql 비밀번호 필요함
// 새로운 DB 데이터 열 추가해야함

// 파일 시스템 및 경로, 인코딩, CSV 파싱, DB 모듈 불러오기
const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');
const csv = require('csv-parser');
const mysql = require('mysql2/promise');

// .env 파일을 읽어서 process.env 변수에 로드
require('dotenv').config({ path: path.join(__dirname, '../.env') });
// require('dotenv').config();

// DB 연결 설정 (실제 서버 환경에 맞게 설정)
// 일단 로컬 서버 기준으로 작성

// 카테고리 번호 : (pl_type)
// 0 병원
// 1 경로당
// 2 쉼터

// DB 연결 설정 (.env에서 로컬 DB 접속 정보 불러오기)
const pool = mysql.createPool({
    host: process.env.DB_LOCAL_HOST,
    port: process.env.DB_LOCAL_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    // waitForConnections: true,
    // connectionLimit: 10
});

// 병원 CSV 파일 경로
const filePath = path.join(__dirname, '../data/cheongju_hospital_2022.csv');

(async () => {

    // DB에 삽입할 병원 데이터 행을 저장할 배열
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
                    0,                        // pl_type (병원: 0)
                    new Date(),              // pl_write
                    new Date()               // pl_update
                ]);
            }
        })
        .on('end', async () => {
            console.log(`${rows.length}개 병원 데이터 준비 완료.`);

            // place 테이블 INSERT 쿼리 (컬럼 순서 주의)
            const sql = `
                INSERT INTO place
                (pl_name, pl_postNumber, pl_addr, pl_detailAddr, pl_tel, pl_lat, pl_lon, pl_display, pl_type, pl_write, pl_update)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            `;

            const conn = await pool.getConnection();
            try {
                // 반복문 통해서 하나씩 DB에 삽입
                for (const row of rows) {
                    await conn.query(sql, row);
                }
                console.log('병원 데이터 DB 삽입 완료');
            } catch (err) {
                console.error('삽입 중 오류:', err);
            } finally {
                conn.release();     // 커넥션 반환
                process.exit();     // 스크립트 종료
            }
        });
})();

/* eslint-disable no-undef */
/* eslint-disable @typescript-eslint/no-unused-vars */
