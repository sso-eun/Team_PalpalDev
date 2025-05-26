const multer = require('multer');
const path = require('path');

// 저장 위치와 파일명 정의
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/'); // uploads 폴더로 저장
    },
    filename: (req, file, cb )=> {
        const ext = path.extname(file.originalname);
        const fileName = `profile_${Date.now()}${ext}`;
        cb(null, fileName);
    },
});

const upload = multer({ storage });
module.exports = upload;
