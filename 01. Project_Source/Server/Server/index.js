console.log('Happy developing ✨')
// index.js
// 주석입니다.
// 안녕하세요.
const http = require('http');

const server = http.createServer((req, res) => {
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Hello Eunjae!');
});

server.listen(3000, () => {
    console.log('Server is running on http://localhost:3000');
});
