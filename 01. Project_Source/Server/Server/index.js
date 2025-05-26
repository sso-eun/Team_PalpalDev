//디버깅용 추후 삭제 예정
console.log('DB Host:', process.env.DB_LOCAL_HOST);
console.log('PORT:', process.env.PORT);

require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const memberRouter = require('./routes/memberRouter');

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

