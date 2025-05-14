// 2025-05-10
// Weather_API
// author : eunjae
// 테스트용 http://localhost:3000/api/weather?nx=60&ny=127&base_date=20250513&base_time=0500
const express = require('express'); // 웹서버 만들기
const axios = require('axios');     // http 요청 보내기(기상청 API 호출)
const xml2js = require('xml2js');   // 기상청 XML 파일 JSON으로 변환
require('dotenv').config();         // .etv 파일에서 API 인증키 읽기 위함

// 서버 객체 만들고 포트 번호를 3000으로 설정
const app = express();
const port = 3000;


// 2025-05-14 은재 수정. 클라이언트가 날짜와 시간을 전달하는 방식으로 변경
// 안드로이드 개발 담당자와 협의 필요

// 날짜 및 시간 계산 함수: 요청 파라미터 전달을 위한 처리
// const getBaseDateTime = () => {
//     const now = new Date();
//     const yyyy = now.getFullYear();
//     const mm = String(now.getMonth() + 1).padStart(2, '0');
//     const dd = String(now.getDate()).padStart(2, '0');
//     const base_date = `${yyyy}${mm}${dd}`;
//
//     // const base_time = '0500'; // 05시 고정 발표 기준 사용
//     // return { base_date, base_time };
//
// };

// 날씨 API
// 클라이언트한테 http 요청 받은거 처리
app.get('/api/weather', async (req, res) => {
    const { nx, ny, base_date, base_time } = req.query;
    
    // 파라미터 관련 예외 처리
    if (!nx || !ny) return res.status(400).json({ error: 'nx, ny 좌표가 필요합니다.' });
    if (!base_date || !base_time) {
        return res.status(400).json({ error: 'base_date, base_time 파라미터가 필요합니다.' });
    }

    // const { base_date} = getBaseDateTime();

    try {
        // 1. 기상청 요청 URL 구성
        const baseUrl = `http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst`;
        const query = `?serviceKey=${process.env.SERVICE_KEY}&pageNo=1&numOfRows=1000&dataType=XML&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;
        const fullUrl = baseUrl + query;

        console.log('요청 URL:', fullUrl); // 디버깅 확인용

        // 2. 기상청 API에게 요청 보냄
        const response = await axios.get(fullUrl);

        // 3. 응답 받은 형식을 XML → JSON로 변환
        const result = await xml2js.parseStringPromise(response.data, { explicitArray: false });

        // 2025-05-14 은재 수정. 디버깅 확인용이므로 추후 삭제.
        // 전체 파싱된 결과 확인
        console.log('파싱 결과:', result);

        // 응답 구조 확인 -> (기상청 파일에 응답 예제 확인)
        // 구조가 맞아야 데이터를 활용할 수 있겠지!

        const items = result?.response?.body?.items?.item;
        if (!items) {
            return res.status(500).json({ error: '기상청 응답 형식이 예상과 다릅니다.' });
        }
        // 사용자에게 보내줄 데이터
        // 홈페이지 일부 섹션에 띄움 (일 최저기온, 일 최고기온, 하늘상태만 필요함)
        const weatherData = {
            currentTemp: null,
            sky: null,
            minTemp: null,
            maxTemp: null,
        };

        for (const item of Array.isArray(items) ? items : [items]) {
            const category = item.category;
            const value = item.fcstValue;

            if (category === 'TMP' && !weatherData.currentTemp) {
                weatherData.currentTemp = value; // 현재 기준 첫 TMP를 사용
            }

            if (category === 'SKY' && !weatherData.sky) {
                if (value === '1') weatherData.sky = '맑음';
                else if (value === '3') weatherData.sky = '구름 많음';
                else if (value === '4') weatherData.sky = '흐림';
            }
            if (category === 'TMN' && !weatherData.minTemp) {
                weatherData.minTemp = value;
            }
            if (category === 'TMX' && !weatherData.maxTemp) {
                weatherData.maxTemp = value;
            }
        }
        // 사용자에게 전달
        res.json(weatherData);

    } catch (error) {
        console.error('API 호출 오류:', error.response?.data || error.message || error);
        res.status(500).json({ error: '기상청 API 호출 실패' });
    }
});

app.listen(port, () => {
    console.log(`Weather API server running on http://localhost:${port}`);
});
