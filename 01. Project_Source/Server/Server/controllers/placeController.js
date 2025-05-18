// 2025-05-17
// Place_API
// author: eunjae
// GET http://localhost:3000/api/places?category=hospital&lat=36.63&lon=127.45&range=0.5
// GET http://localhost:3000/api/places?category=senior_center&lat=36.63&lon=127.45&range=1

const fs = require('fs');
const iconv = require('iconv-lite');
const path = require('path');
const csv = require('csv-parser');
const { getDistance } = require('../utils/distance');

const hospitals = [];


// 2025-05-18 수정
// '의료시설구분' 이 '안전상비의약품' 인 경우는 제외시킴 => 장소가 편의점이라서 필요없는 정보임
// 추후 삭제 예정
// fs.createReadStream(path.join(__dirname, '../data/cheongju_hospital_2022.csv'))
//     .pipe(iconv.decodeStream('cp949')) // 인코딩 변환
//     .pipe(csv({ separator: ',' }))     // CSV 파싱
//     .on('data', (row) => {
//        
//         // 필수 필드가 있을 경우만 저장
//         if (row['위도'] && row['경도']) {
//             hospitals.push({
//                 name: row['기관명'],
//                 address: row['기관 소재지'],
//                 phone: row['전화번호'],
//                 lat: parseFloat(row['위도']),
//                 lon: parseFloat(row['경도'])
//             });
//         }
//     })
//     .on('end', () => {
//         console.log(`병원 데이터 ${hospitals.length}건 로딩 완료`);
//     });
//
const centers = [];

fs.createReadStream(path.join(__dirname, '../data/cheongju_SeniorCenter_2024.CSV'))
    .pipe(iconv.decodeStream('cp949'))
    .pipe(csv({ separator: ',' }))
    .on('data', (row) => {
        const lat = parseFloat(row['위도']);
        const lon = parseFloat(row['경도']);
        if (!isNaN(lat) && !isNaN(lon)) {
            centers.push({
                name: row['시설명'],
                address: row['소재지도로명주소'],
                phone: row['전화번호'],
                lat: lat,
                lon: lon
            });
        }
    })
    .on('end', () => {
        console.log(`경로당 데이터 ${centers.length}건 로딩 완료`);
    });


// hospital - loading CSV file
// CSV 파일을 CP949 인코딩으로 읽어 UTF-8로 변환 후 파싱
fs.createReadStream(path.join(__dirname, '../data/cheongju_hospital_2022.csv'))
    .pipe(iconv.decodeStream('cp949'))
    .pipe(csv({ separator: ',' }))
    .on('data', (row) => {
        

        // 디버깅용 출력
        // console.log('[정상 CSV row]', row);
        
        // 한 줄씩 읽으며 조건에 맞는 병원만 필터링
        const lat = parseFloat(row['위도']);
        const lon = parseFloat(row['경도']);

        // 필수 조건: 좌표가 존재하고, 안전상비의약품이 아닌 의료시설만 포함
        if (
            row['의료시설구분'] !== '안전상비의약품' &&
            !isNaN(lat) &&
            !isNaN(lon)
        ) {
            hospitals.push({
                name: row['기관명'],
                address: row['기관 소재지'],
                phone: row['전화번호'],
                lat: lat,
                lon: lon
            });
        }
    })
    .on('end', () => {
        console.log(`병원 데이터 ${hospitals.length}건 로딩 완료`);
    });


// 사용자 위치 기반 장소 요청 처리
exports.getPlaces = (req, res) => {
    const { category, lat, lon, range } = req.query;
    if (!lat || !lon) return res.status(400).json({ error: 'lat, lon 필수' });

    const userLat = parseFloat(lat);
    const userLon = parseFloat(lon);
    const searchRadius = parseFloat(range) || 1; // 기본값 1km (range가 없으면)

    // Hospital
    if (category === 'hospital') {
        const result = hospitals
            .map(h => ({
                ...h,
                distance: getDistance(userLat, userLon, h.lat, h.lon) // 거리 계산
            }))
            // 주소 확대, 축소할 때는 반경 변화 있는가? 
            // -> 이것도 요청 파라미터로 만들어서 사용할 수 있을 듯

            // 2025-05-18
            // 앱에서 확대/축소 에 따라 반경 범위를 다르게 하도록 변경
            // 파라미터 하나 추가
            .filter(h => h.distance <= searchRadius)            // 반경 파라미터로 필터링
            .sort((a, b) => a.distance - b.distance) // 가까운 순 정렬

            // 최대 20개까지만 반환
            // 반경 1km로 줄이니까 반환 장소 수가 100개 정도여서 괜찮을 듯
            //.slice(0, 20);                        

        // 디버깅용
        console.log(`[응답] 병원 ${result.length}개 반환 (반경 ${searchRadius}km 내)`);

        return res.json(result);

    }

    // Senior Center
    if (category === 'senior_center') {
        const result = centers
            .map(c => ({
                ...c,
                distance: getDistance(userLat, userLon, c.lat, c.lon)
            }))
            .filter(c => c.distance <= searchRadius)
            .sort((a, b) => a.distance - b.distance);

        console.log(`[응답] 경로당 ${result.length}개 반환 (반경 ${searchRadius}km 내)`);
        return res.json(result);
    }
    else {
        return res.status(400).json({ error: '지원하지 않는 category입니다.' });
    }
};
