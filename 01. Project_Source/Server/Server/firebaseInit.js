// project_root/firebaseInit.js

// 구글에서 제공하는 공식 라이브러리
const admin = require('firebase-admin');

// 서비스 계정 키 파일이 firebaseInit.js와 같은 폴더에 있으므로 './serviceAccountKey.json'
// 이 경로를 정확히 확인하고 수정하세요!
const serviceAccount = JSON.parse(process.env.FCM_KEY_JSON);

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

console.log('Firebase Admin SDK initialized successfully.');

// 초기화된 admin 객체를 다른 파일에서 사용할 수 있도록 내보냅니다.
module.exports = admin;