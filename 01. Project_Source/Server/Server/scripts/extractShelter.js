// 2025-05-23
// 무더위 쉼터 API CSV로 저장 (충청북도 기준 필터링)
// author : eunjae

require('dotenv').config();
const axios = require('axios');
const fs = require('fs');
const { parse } = require('json2csv');
const path = require('path');

const API_URL = 'https://www.safetydata.go.kr/V2/api/DSSP-IF-10942';

const fetchShelterData = async () => {
    try {
        const { data } = await axios.get(API_URL, {
            params: {
                serviceKey: process.env.SERVICE_KEY_ST
                // returnType 제거됨
            }
        });

        // 응답 로그 확인
        const items = data?.body?.items;

        if (!Array.isArray(items) || items.length === 0) {
            console.log('응답 데이터가 비어있거나 형식이 잘못되었습니다.');
            return;
        }

        // "충청북도" 포함 주소만 필터링
        const filtered = items.filter(item =>
            item.DTL_ADRES?.includes('충청북도')
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
        const csv = parse(filtered, {
            fields: [
                'RSTR_NM',              // 쉼터명
                'DTL_ADRES',            // 상세주소
                'AR',                   // 면적
                'USE_PSB_NMPR',         // 이용 가능 인원
                'COLR_HOLD_ELEFN',      // 냉방기 보유 여부
                'COLR_HOLD_ARCDTN',     // 에어컨 보유 여부
                'CHCK_MATTER_NIGHT_OPN_AT' // 야간개방 여부
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
