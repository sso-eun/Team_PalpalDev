const path = require('path');
// const db = require('../config/database');
const mysql = require("mysql2/promise"); // DB 연결
const mime = require('mime-types');
const supabase = require('../utils/supabaseClient');
const fs = require('fs');

const db = mysql.createPool({
    // host: process.env.DB_LOCAL_HOST,
    // port: process.env.DB_LOCAL_PORT,
    host: process.env.DB_SERVER_HOST,
    port: process.env.DB_SERVER_PORT,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});


exports.getCert = async (req, res) => {
    const {req_no} = req.params;
    // const bucketName = 'cert';
    const bucketName = 'uploads';

    const sql = `SELECT certificate_img FROM guardian_auth_upload WHERE req_no = ?`;
    const [rows] = await db.execute(sql, [req_no]);

    if (!rows.length) {
        return res.status(404).json({rsCode: 404, message: '파일이 없습니다'});
    }

    const filename = rows[0].certificate_img;
    const filePath = `${filename}`;


    const {data, error} = await supabase
        .storage
        .from(bucketName)
        .download(filePath);

    if (error || !data) {
        return res.status(404).json({rsCode:404, message: '저장소에 이미지가 없습니다.'});
    }

    const arrayBuffer = await data.arrayBuffer();  // ← 핵심
    const buffer = Buffer.from(arrayBuffer);

    const contentType = mime.lookup(filename) || 'application/octet-stream';
    res.setHeader('Content-Type', contentType);
    res.send(buffer);
};


exports.getProfile = async (req, res) => {
    const {user_num} = req.params;
    const bucketName = 'uploads';

    const sql = `SELECT user_profile_img FROM member WHERE user_num = ?`;
    const [rows] = await db.execute(sql, [user_num]);

    if (!rows.length) {
        return res.status(404).json({rsCode: 404, message: '파일이 없습니다'});
    }

    const filename = rows[0].user_profile_img;
    const filePath = `${filename}`;


    const {data, error} = await supabase
        .storage
        .from(bucketName)
        .download(filePath);

    if (error || !data) {
        return res.status(404).json({rsCode:404, message: '저장소에 이미지가 없습니다.'});
    }

    const arrayBuffer = await data.arrayBuffer();  // ← 핵심
    const buffer = Buffer.from(arrayBuffer);

    const contentType = mime.lookup(filename) || 'application/octet-stream';
    res.setHeader('Content-Type', contentType);
    res.send(buffer);
};

exports.getTalkImage = async (req, res) => {
    const {talk_id} = req.params;
    const bucketName = 'uploads';

    const sql = `SELECT image_url FROM talk_list WHERE talk_id= ?`;
    const [rows] = await db.execute(sql, [talk_id]);

    if (!rows.length) {
        return res.status(404).json({rsCode: 404, message: '파일이 없습니다'});
    }

    const filename = rows[0].image_url;
    const filePath = `${filename}`;


    const {data, error} = await supabase
        .storage
        .from(bucketName)
        .download(filePath);

    console.log(error);

    if (error || !data) {
        return res.status(404).json({rsCode:404, message: '저장소에 이미지가 없습니다.'});
    }

    const arrayBuffer = await data.arrayBuffer();  // ← 핵심
    const buffer = Buffer.from(arrayBuffer);

    const contentType = mime.lookup(filename) || 'application/octet-stream';
    res.setHeader('Content-Type', contentType);
    res.send(buffer);
};