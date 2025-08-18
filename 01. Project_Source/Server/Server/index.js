//디버깅용 추후 삭제 예정
// console.log('DB Host:', process.env.DB_LOCAL_HOST);
// console.log('PORT:', process.env.PORT);

require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');
const memberRouter = require('./routes/memberRouter');
const memberDateRouter = require('./routes/memberDateRouter');
const uploadRouter = require('./routes/uploadRouter');
const sendRouter = require('./routes/sendAuthRouter');
const talkRouter = require('./routes/talkRouter');
const certRouter = require('./routes/certRouter');
const downloadRouter = require('./routes/downloadRouter');

// 250517_은재_라우터 등록
const weatherRouter = require('./routes/weatherRouter');
const placeRouter = require('./routes/placeRouter');
const cultureRouter = require('./routes/cultureRouter');
const notificationRouter = require('./routes/notificationRouter');

const cron = require('node-cron');
// index.js
// const http = require('http');
//
// const server = http.createServer((req, res) => {
//     res.writeHead(200, {'Content-Type': 'text/plain'});
//     res.end('Hello! We are PalpalDev!!_Live Server');
// });
//
// server.listen(3000, () => {
//     console.log('Server is running on http://localhost:3000');
// });

// 250516_소은_라우터 등록
const app = express();
app.use(bodyParser.json());

app.get('/', (req, res) => {
    res.send('Hello! We are PalpalDev!! Live Server');
});

// /member
app.use('/member', memberRouter);
// 멤버 일정
app.use('/date', memberDateRouter);
// 파일업로드
app.use('/upload', uploadRouter);
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
//sms 인증
app.use('/code_auth',sendRouter);
//render 서버에서 _ 감지 불능으로 _제거용 주소 추가.
// 25.06.04_추가_ 현재는 주소 두개 다 감지 가능. _버전 우선으로 사용 요망
app.use('/codeauth',sendRouter);
// 멤버 대화내역
app.use('/talk', talkRouter);

//25.08.01
//증명서
app.use('/cert', certRouter);
//이미지 다운로더
app.use('/down',  downloadRouter);



// FCM 라우터를 연결합니다.
// fcmRouter.js는 project_root/routes 폴더에 있으므로 './routes/fcmRouter'로 불러옵니다.
const fcmRouter = require('./routes/fcmRouter');
app.use('/fcm', fcmRouter); // '/fcm' 경로로 들어오는 요청을 fcmRouter가 처리하도록 설정합니다. (예: /fcm/send-test-notifications)


// FCM 컨트롤러를 불러옵니다.
// controllers/fcmController.js는 project_root/controllers 폴더에 있으므로 './controllers/fcmController'로 불러옵니다.
const fcmController = require('./controllers/fcmController');

// 테스트
cron.schedule('0 18 30 * * *', () => { // 0초 20분 9시 (오전 9시 20분)에 실행
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 6 PM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});

// 테스트
cron.schedule('0 18 40 * * *', () => { // 0초 20분 9시 (오전 9시 20분)에 실행
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 6 PM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});

// 테스트
cron.schedule('0 18 50 * * *', () => { // 0초 20분 9시 (오전 9시 20분)에 실행
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 6 PM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});

// 테스트
cron.schedule('0 17 00 * * *', () => { // 0초 20분 9시 (오전 9시 20분)에 실행
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 6 PM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});


// 매일 오전 9시 (0분 0초)에 fcmController.sendScheduledNotifications 함수를 실행합니다.
cron.schedule('0 0 9 * * *', () => {
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 9 AM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});

// 매일 오후 6시 (0분 0초)에 fcmController.sendScheduledNotifications 함수를 실행합니다.
cron.schedule('0 0 18 * * *', () => {
    console.log(`[${new Date().toLocaleString('ko-KR', { timeZone: 'Asia/Seoul' })}] Triggering 6 PM daily notification.`);
    fcmController.sendScheduledNotifications(); // FCM 알림 전송 함수 호출
}, {
    timezone: "Asia/Seoul" // 한국 시간대(KST)로 설정
});

console.log('FCM scheduling tasks configured.'); // 스케줄링 등록 완료 메시지




// 250517_은재_추가
// /api/places로 들어온 요청을 .placeRouter에 넘기는 거임
app.use('/places', placeRouter);
app.use('/weather', weatherRouter);
app.use('/culture_center', cultureRouter);
app.use('/notifications', notificationRouter);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});


// 디버깅용 추후 삭제 예정
process.on('uncaughtException', (err) => {
    console.error('[uncaughtException]', err);
});

process.on('unhandledRejection', (reason, promise) => {
    console.error('[unhandledRejection]', reason);
});

// console.log(`Server running on http://localhost:${PORT}`);

//소은. 아래부터는 admin web 입니다.
const adminDist = path.resolve(__dirname, '../../Web/admin/dist');
const adminMount = '/admin';

// 1) 정적 파일 제공 (/admin 하위)
app.use(adminMount, express.static(adminDist, { index: false })); // index 자동서빙 끔

// 2) SPA 라우팅 (리프레시 404 방지)
//    정규식: ^/admin( /... )? 전부 매칭
app.get(/^\/admin(?:\/.*)?$/, (req, res) => {
    res.sendFile(path.join(adminDist, 'index.html'));
});
