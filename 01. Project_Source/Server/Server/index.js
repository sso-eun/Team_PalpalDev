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

// 250517_은재_라우터 등록
const weatherRouter = require('./routes/weatherRouter');
const placeRouter = require('./routes/placeRouter');
const cultureRouter = require('./routes/cultureRouter');
const notificationRouter = require('./routes/notificationRouter');

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

console.log(`Server running on http://localhost:${PORT}`);

