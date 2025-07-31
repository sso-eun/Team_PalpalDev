const multer = require('multer');
const path = require('path');

// 저장 위치와 파일명 정의
// const storage = multer.diskStorage({
//     destination: (req, file, cb) => {
//         cb(null, 'uploads/'); // uploads 폴더로 저장
//     },
//     filename: (req, file, cb )=> {
//         const ext = path.extname(file.originalname);
//         const fileName = `profile_${Date.now()}${ext}`;
//         cb(null, fileName);
//     },
// });
//
// const upload = multer({ storage });
// module.exports = upload;

const getStorage = (subfolder = '') => {
    return multer.diskStorage({
        destination: (req, file, cb) => {
            const uploadPath = path.join('uploads', subfolder);
            cb(null, uploadPath);
        },
        filename: (req, file, cb) => {
            const ext = path.extname(file.originalname);
            // const prefix = subfolder === 'talk' ? 'talk' : 'profile';
            const prefix = subfolder === subfolder || 'file'
            const fileName = `${prefix}_${Date.now()}${ext}`;
            cb(null, fileName);
        }
    });
};

// 프로필 업로더 (uploads/)
const uploadProfile = multer({ storage: getStorage() });

// 토크 업로더 (uploads/talk/)
const uploadTalk = multer({ storage: getStorage('talk') });

// 증명서 업로더 (uploads/cert/)
const uploadCert = multer({ storage: getStorage('cert') });

module.exports = { uploadProfile, uploadTalk, uploadCert };
