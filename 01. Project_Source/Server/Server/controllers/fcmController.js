const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });
const mysql = require('mysql2/promise');


const admin = require('firebase-admin');

// 서비스 계정 키 파일 경로 추가
const serviceAccount = require('../firebase-admin.json');

// Firebase Admin SDK 초기화 추가
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const message = {
    notification: {
        title: 'FCM Test',
        body: 'This is a test message'
    },
    token: 'FCM_DEVICE_TOKEN'
};

admin.messaging().send(message)
    .then((response) => {
        console.log('Successfully sent message:', response);
    })
    .catch((error) => {
        console.log('Error sending message:', error);
    });