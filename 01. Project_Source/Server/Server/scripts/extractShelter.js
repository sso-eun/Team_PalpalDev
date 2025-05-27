// 2025-05-23
// 무더위 쉼터 API CSV로 저장 (충청북도 기준 필터링)
// author : eunjae

// require('dotenv').config();
// const axios = require('axios');
// const fs = require('fs');
// const { parse } = require('json2csv');
// const path = require('path');
// const dotenvPath = path.resolve(__dirname, '../../.env');

const axios = require('axios');
const path = require('path');
const { parse } = require('json2csv');
const fs = require('fs');
const dotenvPath = path.resolve(__dirname, '../.env');


require('dotenv').config({ path: dotenvPath });


// 디버깅용
console.log("찾으려는 .env 경로:", dotenvPath);
console.log("존재 여부:", fs.existsSync(dotenvPath));
console.log("API 키:", process.env.SERVICE_KEY_ST);  // ← 이제 여기서 값 나와야 함

const API_URL = 'https://www.safetydata.go.kr/V2/api/DSSP-IF-10942';

const fetchShelterData = async () => {
    try {
        const { data } = await axios.get(API_URL, {
            params: {
                serviceKey: process.env.SERVICE_KEY_ST,
                pageNo: 1,
                numOfRows: 1000,
                returnType: 'json',
                startLat: 36.56,
                endLat: 36.79,
                startLot: 127.42,
                endLot: 127.59
            }
        });

        // 응답 전체 구조 로그 출력
        console.log("응답 데이터 전체 구조 확인:");
        console.dir(data, { depth: null });

        // 응답 로그 확인
        // const items = data?.body?.items;
        const items = data?.body;

        if (!Array.isArray(items) || items.length === 0) {
            console.log('응답 데이터가 비어있거나 형식이 잘못되었습니다.');
            return;
        }
        // 시작 위도, 경도 / 종료 위도, 경도로 API요청할 때 필터링하는 방식으로 변경
        // "충청북도" 포함 주소만 필터링
        const filtered = items.filter(item =>
            item.RN_DTL_ADRES?.includes('충청북도') || item.DTL_POSITION?.includes('충청북도')
        );

        if (filtered.length === 0) {
            console.log(' 충청북도에 해당하는 데이터가 없습니다.');
            return;
        }

        // 저장 폴더 생성 (없으면)
        const outputDir = path.join(__dirname, 'data');
        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir);
        }

        // CSV 변환
        // 공공데이터에서 반환하는 값 많음
        // 그 중 필요할 거 같은 것들만 뽑아온다.
        const csv = parse(filtered, {
            fields: [
                'RSTR_NM',                      // 쉼터명 → pl_name
                'DTL_ADRES',                    // 상세주소 → pl_addr
                'RN_DTL_ADRES',                 // 실제 주소 '충청북도 청주시 상당구 ,,,"
                'LO',                           // 경도 → pl_lon
                'LA',                           // 위도 → pl_lat
                'USE_PSB_NMPR',                // 이용 가능 인원
                'COLR_HOLD_ELEFN',             // 냉방기 보유 여부
                'COLR_HOLD_ARCDTN',            // 에어컨 보유 여부
                'CHCK_MATTER_NIGHT_OPN_AT',    // 야간개방 여부
                'FCLTY_OPRN_AT'                // 운영 여부
            ],
            withBOM: true
        });

        const fileName = path.join(outputDir, `chungbuk_shelters_${new Date().toISOString().slice(0, 10)}.csv`);
        fs.writeFileSync(fileName, csv, 'utf-8');

        console.log(`충청북도 무더위쉼터 ${filtered.length}건을 '${fileName}'에 저장했습니다.`);
    } catch (error) {
        console.error('API 호출 실패:', error.response?.data || error.message);
    }
};

fetchShelterData();
