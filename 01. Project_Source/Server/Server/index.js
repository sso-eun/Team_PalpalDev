require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const memberRouter = require('./routes/memberRouter');

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

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});