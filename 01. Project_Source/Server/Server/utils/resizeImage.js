// // 2025-08-04
// // author : Soeun
// // utils/resizeImage.js
// const path = require('path');
// const fs = require('fs');
// const sharp = require('sharp');
//
// const resizeImage = async (req, res, next) => {
//     try {
//         if (!req.file) return next();
//
//         const inputPath = req.file.path;
//         const ext = path.extname(req.file.originalname);
//         const baseName = path.basename(req.file.originalname, ext);
//
//
//         // ────── 요청 URL에서 서브폴더 추출 (/cert일 경우 cert) ──────
//         const uploadRoutes = ['cert', 'talk', 'profile','supa','upload',]; // upload는 디폴트 처리 폴더. 항상 마지막에
//         const matched = uploadRoutes.find(route => req.originalUrl.includes(`/${route}`));
//         const uploadBase = matched || 'upload'; // 기본 upload로 fallback
//         // const uploadBase = path.basename(req.file.destination);
//         console.log(req.file.destination)
//         const outputDir = path.join('uploads', uploadBase); // uploads/cert
//         const outputName = `${uploadBase}_${baseName}_${Date.now()}${ext}`;
//         const outputPath = path.join(outputDir, outputName);
//
//         if (!fs.existsSync(outputDir)) {
//             fs.mkdirSync(outputDir, { recursive: true });
//         }
//
//         // await sharp(inputPath)
//         //     .resize({ width: 800 })
//         //     .jpeg({ quality: 70 })
//         //     .toFile(outputPath);
//
//         const sharpInstance = sharp(inputPath).resize({ width: 300 });
//         if (ext === '.png') {
//             await sharpInstance.png({ quality: 70 }).toFile(outputPath);
//         } else {
//             await sharpInstance.jpeg({ quality: 70 }).toFile(outputPath);
//         }
//
//
//         req.file.resizedPath = outputPath;
//
//         // 원본 파일 삭제
//         fs.unlinkSync(inputPath);
//
//         next();
//     } catch (error) {
//         console.error('resizeImage error:', error);
//         res.status(500).json({ message: '이미지 리사이즈 실패' });
//     }
// };
//
// module.exports = resizeImage;


// // utils/resizeImage.js
// const path = require('path');
// const fs = require('fs');
// const sharp = require('sharp');
//
// //cludyType tmp 폴더 경로 추가
// const UPLOAD_ROOT = process.env.UPLOAD_DIR || path.join(process.cwd(), 'uploads');
//
//
// const ensureDir = async (dir) => {
//     await fs.promises.mkdir(dir, { recursive: true, mode: 0o775 });
// };
//
// const toPosixAbs = (p) => {
//     const s = String(p).replace(/\\/g, '/'); // 윈도우→POSIX 처리
//     return path.isAbsolute(s) ? s : path.join(process.cwd(), s);
// };
//
// const resizeImage = async (req, res, next) => {
//     try {
//         if (!req.file) return next();
//
//         const inputPath = toPosixAbs(req.file.path);
//
//         const rawExt = path.extname(req.file.originalname || '').toLowerCase();
//         const ext = rawExt || '.jpg';
//         const baseName = path.basename(req.file.originalname || 'image', rawExt);
//
//         // const uploadRoutes = ['cert','talk','profile','supa','upload'];
//         // const matched = uploadRoutes.find(route => req.originalUrl.includes(`/${route}`));
//         // const uploadBase = matched || 'upload';
//         //
//         // const UPLOAD_ROOT = process.env.UPLOAD_DIR || path.join(process.cwd(), 'uploads');
//         // const outputDir = toPosixAbs(path.join(UPLOAD_ROOT, uploadBase));
//         // await ensureDir(outputDir);
//
//         const uploadRoutes = ['cert','talk','profile','supa','upload'];
//         const matched = uploadRoutes.find(route => req.originalUrl.includes(`/${route}`));
//         const uploadBase = matched || 'upload';
//
//         const outputDir = path.join(UPLOAD_ROOT, uploadBase);
//         await fs.promises.mkdir(outputDir, { recursive: true, mode: 0o775 });
//
//         const outputName = `${uploadBase}_${baseName}_${Date.now()}${ext}`;
//         const outputPath = toPosixAbs(path.join(outputDir, outputName));
//
//         const sharpInstance = sharp(inputPath).resize({ width: 300 });
//
//         if (ext === '.png') {
//             await sharpInstance.png({ compressionLevel: 7 }).toFile(outputPath);
//         } else if (ext === '.webp') {
//             await sharpInstance.webp({ quality: 70 }).toFile(outputPath);
//         } else {
//             await sharpInstance.jpeg({ quality: 70 }).toFile(outputPath);
//         }
//         req.file.resizedPath = outputPath;
//
//         try { await fs.promises.unlink(inputPath); } catch (_) {}
//
//         next();
//     } catch (error) {
//         console.error('resizeImage error:', error);
//         res.status(500).json({ message: '이미지 리사이즈 실패' });
//     }
// };
//
// module.exports = resizeImage;


// utils/resizeImage.js
const path = require('path');
const fs = require('fs');
const sharp = require('sharp');

const UPLOAD_ROOT = process.env.UPLOAD_DIR || path.join(process.cwd(), 'uploads');

const ensureWritableDir = async (dir) => {
    try {
        await fs.promises.mkdir(dir, { recursive: true, mode: 0o775 });
        const testFile = path.join(dir, '.write_test');
        await fs.promises.writeFile(testFile, 'ok');
        await fs.promises.unlink(testFile);
        return true; // 쓰기 가능
    } catch {
        return false; // 쓰기 불가
    }
};

const pickSubfolder = (url) => {
    const allow = new Set(['cert','talk','profile','supa','upload']);
    const segs = url.split('?')[0].split('/').filter(Boolean);
    const idx = segs.indexOf('upload');
    const cand = (idx >= 0 && segs[idx+1]) ? segs[idx+1] : null;
    return allow.has(cand) ? cand : 'upload';
};

const toPosixAbs = (p) => (path.isAbsolute(p) ? p : path.join(process.cwd(), p)).replace(/\\/g, '/');
const sanitizeBase = (name) => name.replace(/[^\w.-]+/g, '_');

const resizeImage = async (req, res, next) => {
    try {
        if (!req.file) return next();

        const inputPath = toPosixAbs(req.file.path);
        const rawExt = path.extname(req.file.originalname || '').toLowerCase();
        const ext = rawExt || '.jpg';
        const baseName = sanitizeBase(path.basename(req.file.originalname || 'image', rawExt));
        const subfolder = pickSubfolder(req.originalUrl);

        // 1) 우선순위: UPLOAD_ROOT/subfolder → 실패 시 /tmp/uploads/subfolder
        let outputDir = path.join(UPLOAD_ROOT, subfolder);
        if (!(await ensureWritableDir(outputDir))) {
            outputDir = path.join('/tmp/uploads', subfolder);
            await ensureWritableDir(outputDir); // /tmp는 거의 항상 OK
        }

        const outputName = `${subfolder}_${baseName}_${Date.now()}${ext}`;
        const outputPath = toPosixAbs(path.join(outputDir, outputName));

        const s = sharp(inputPath).rotate().resize({ width: 300 });
        if (ext === '.png')      await s.png({ compressionLevel: 7 }).toFile(outputPath);
        else if (ext === '.webp') await s.webp({ quality: 70 }).toFile(outputPath);
        else                      await s.jpeg({ quality: 70 }).toFile(outputPath);

        req.file.resizedPath   = outputPath;
        req.file.resizedFileName = outputName;
        req.file.resizedMime   = ext === '.png' ? 'image/png' : ext === '.webp' ? 'image/webp' : 'image/jpeg';
        req.file.subfolder     = subfolder;

        try { await fs.promises.unlink(inputPath); } catch {}

        next();
    } catch (e) {
        console.error('resizeImage error:', e);
        res.status(500).json({ message: '이미지 리사이즈 실패' });
    }
};

module.exports = resizeImage;
