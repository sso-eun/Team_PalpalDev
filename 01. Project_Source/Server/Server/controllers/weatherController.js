// /controllers/weatherController.js (수정된 코드)

const express = require('express');
const router = express.Router();
const weatherService = require('../utils/weatherService'); // 새로 만든 서비스 import

// 날씨 API
router.get('/', async (req, res) => {
    const { lat, lon } = req.query;

    if (!lat || !lon) {
        return res.status(400).json({ error: 'lat, lon 파라미터가 필요합니다.' });
    }

    try {
        const weatherData = await weatherService.getWeatherForLocation(parseFloat(lat), parseFloat(lon));

        if (!weatherData) {
            return res.status(500).json({ error: '날씨 정보를 가져오는데 실패했습니다.' });
        }

        res.status(200).json({ status: 200, data: weatherData });

    } catch (error) {
        console.error('날씨 API 처리 중 오류:', error.message);
        res.status(500).json({ error: '서버 내부 오류' });
    }
});

module.exports = router;

// // 2025-05-10
// // Weather_API
// // author : eunjae
// // 테스트용 http://localhost:3000/weather?lat=37.5665&lon=126.9780&base_date=20250522&base_time=0500
// const express = require('express'); // 웹서버 만들기
// const axios = require('axios');     // http 요청 보내기(기상청 API 호출)
// const xml2js = require('xml2js');   // 기상청 XML 파일 JSON으로 변환
// const router = express.Router();
//
// // index.js에서 처리하므로 필요없음 - 추후 삭제 예정
// // .etv 파일에서 API 인증키 읽기 위함
// // const path = require('path');
// // require('dotenv').config({ path: path.join(__dirname, '.env.weather') });
//
// // 서버 객체 만들고 포트 번호를 3000으로 설정
// const app = express();
// const port = 3000;
//
// // 2025-05-17 은재 수정
// // 위도/경도 → 기상청 격자 좌표 변환 함수
// const convertToGrid = (lat, lon) => {
//     const RE = 6371.00877;
//     const GRID = 5.0;
//     const SLAT1 = 30.0;
//     const SLAT2 = 60.0;
//     const OLON = 126.0;
//     const OLAT = 38.0;
//     const XO = 43;
//     const YO = 136;
//
//     const DEGRAD = Math.PI / 180.0;
//     const re = RE / GRID;
//     const slat1 = SLAT1 * DEGRAD;
//     const slat2 = SLAT2 * DEGRAD;
//     const olon = OLON * DEGRAD;
//     const olat = OLAT * DEGRAD;
//
//     // 격자 변환 수식
//     let sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
//     sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
//
//     let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
//     sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
//
//     let ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
//     ro = re * sf / Math.pow(ro, sn);
//
//     let ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
//     ra = re * sf / Math.pow(ra, sn);
//
//     let theta = lon * DEGRAD - olon;
//     if (theta > Math.PI) theta -= 2.0 * Math.PI;
//     if (theta < -Math.PI) theta += 2.0 * Math.PI;
//     theta *= sn;
//
//     const x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
//     const y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
//     return { nx: x, ny: y };
// };
//
//
// // 날씨 API
// // 클라이언트에서 GET 요청 시 실행되는 함수
// router.get('/', async (req, res) => {
//     let { nx, ny, lat, lon, base_date, base_time } = req.query;
//
//     // 필수 파라미터 검사
//     if (!base_date || !base_time) {
//         return res.status(400).json({ error: 'base_date, base_time 파라미터가 필요합니다.' });
//     }
//
//     // 위도/경도 -> 격자 좌표 변환 (공공데이터에서 x좌표와 y좌표를 필요로 함)
//     if (lat && lon) {
//         const grid = convertToGrid(parseFloat(lat), parseFloat(lon));
//         nx = grid.nx;
//         ny = grid.ny;
//     }
//
//     // 변환 실패 시 에러
//     if (!nx || !ny) {
//         return res.status(400).json({ error: 'nx, ny 또는 lat, lon 중 하나가 필요합니다.' });
//     }
//
//     try {
//
//         // 기상청 API 요청 URL 구성
//         const baseUrl = `http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst`;
//         //const query = `?serviceKey=${process.env.SERVICE_KEY_WT}&pageNo=1&numOfRows=1000&dataType=XML&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;
//
//         // 2025-06-05 수정
//         // 공공데이터 기상청 요청 파라미터 변경
//         // const query = `?serviceKey=${process.env.SERVICE_KEY_WT}&pageNo=1&dataType=XML&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;
//         const query = `?serviceKey=${process.env.SERVICE_KEY_WT}&pageNo=1&numOfRows=500&dataType=XML&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;
//
//         const fullUrl = baseUrl + query;
//
//         // 디버깅용
//         // 추후 삭제 예정
//         // console.log('사용 중인 SERVICE_KEY_WT:', process.env.SERVICE_KEY_WT);
//         // console.log('요청 URL:', fullUrl);
//
//         // 기상청 API로 GET 요청 전송
//         const response = await axios.get(fullUrl);
//
//         //console.log('기상청 응답 원본:', response.data);
//
//         // 응답 XML 데이터를 JSON 형태로 파싱
//         const result = await xml2js.parseStringPromise(response.data, { explicitArray: false });
//
//         // 디버깅용
//         console.log('파싱 결과', result);
//
//         // 응답 구조 중 item 배열 추출 (필요한 데이터만 추출)
//         const items = result?.response?.body?.items?.item;
//         if (!items) {
//             return res.status(500).json({ error: '기상청 응답 형식이 예상과 다릅니다.' });
//         }
//         // 디버깅용
//
//
//         // 클라이언트에 보낼 날씨 정보 객체, 초기화
//         const weatherData = {
//             currentTemp: null,
//             sky: null,
//             minTemp: null,
//             maxTemp: null,
//         };
//
//         // 각 항목 순회하면서 필요한 정보만 추출
//         for (const item of Array.isArray(items) ? items : [items]) {
//             const category = item.category;
//             const value = item.fcstValue;
//
//             if (category === 'TMP' && !weatherData.currentTemp) weatherData.currentTemp = value;
//             if (category === 'SKY' && !weatherData.sky) {
//                 if (value === '1') weatherData.sky = '맑음';
//                 else if (value === '3') weatherData.sky = '구름 많음';
//                 else if (value === '4') weatherData.sky = '흐림';
//             }
//             if (category === 'TMN' && !weatherData.minTemp) weatherData.minTemp = value;
//             if (category === 'TMX' && !weatherData.maxTemp) weatherData.maxTemp = value;
//         }
//
//         res.status(200).json({ status: 200, data: weatherData });
//     } catch (error) {
//         console.error('기상청 API 오류:', error.message);
//         res.status(500).json({ error: '기상청 API 호출 실패' });
//     }
// });
// module.exports = router;
//
//
//
