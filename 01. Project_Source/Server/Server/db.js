// project_root/db.js

const mysql = require('mysql2/promise'); // mysql2 패키지의 promise 버전 불러오기

// MySQL DB 연결 풀 설정
const pool = mysql.createPool({
    // Dundun DB
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    waitForConnections: true,     // 풀에 사용 가능한 연결이 없을 때 대기할지 여부
    connectionLimit: 10,          // 풀에서 생성할 최대 연결 수
    queueLimit: 0                 // 연결 대기열의 최대 요청 수 (0은 무제한)
});

// 연결 테스트 (선택 사항이지만 권장)
pool.getConnection()
    .then(connection => {
        console.log('Successfully connected to MySQL database!');
        connection.release(); // 연결 사용 후 반환
    })
    .catch(err => {
        console.error('Error connecting to MySQL database:', err.message);
        // 실제 운영 환경에서는 앱이 시작되지 않도록 프로세스를 종료할 수도 있습니다.
        // process.exit(1);
    });

// 다른 모듈에서 이 DB 연결 풀을 사용할 수 있도록 내보냅니다.
module.exports = pool;