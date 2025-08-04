// 2025-08-04
// author : Soeun
// utils/resizeImage.js
const path = require('path');
const fs = require('fs');
const sharp = require('sharp');

const resizeImage = async (req, res, next) => {
    try {
        if (!req.file) return next();

        const inputPath = req.file.path;
        const ext = path.extname(req.file.originalname);
        const baseName = path.basename(req.file.originalname, ext);

        // ────── 요청 URL에서 서브폴더 추출 (/cert일 경우 cert) ──────
        const uploadRoutes = ['cert', 'talk', 'profile','upload',]; // upload는 디폴트 처리 폴더. 항상 마지막에
        const matched = uploadRoutes.find(route => req.originalUrl.includes(`/${route}`));
        const uploadBase = matched || 'upload'; // 기본 upload로 fallback

        const outputDir = path.join('uploads', uploadBase); // uploads/cert
        const outputName = `${uploadBase}_${baseName}_${Date.now()}${ext}`;
        const outputPath = path.join(outputDir, outputName);

        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }

        // await sharp(inputPath)
        //     .resize({ width: 800 })
        //     .jpeg({ quality: 70 })
        //     .toFile(outputPath);

        const sharpInstance = sharp(inputPath).resize({ width: 800 });
        if (ext === '.png') {
            await sharpInstance.png({ quality: 70 }).toFile(outputPath);
        } else {
            await sharpInstance.jpeg({ quality: 70 }).toFile(outputPath);
        }


        req.file.resizedPath = outputPath;

        // 원본 파일 삭제
        fs.unlinkSync(inputPath);

        next();
    } catch (error) {
        console.error('resizeImage error:', error);
        res.status(500).json({ message: '이미지 리사이즈 실패' });
    }
};

module.exports = resizeImage;
