// /utils/weatherService.js

const axios = require('axios');
const xml2js = require('xml2js');

// 위도/경도 → 기상청 격자 좌표 변환 함수 (weatherController.js에서 가져옴)
const convertToGrid = (lat, lon) => {
    const RE = 6371.00877;
    const GRID = 5.0;
    const SLAT1 = 30.0;
    const SLAT2 = 60.0;
    const OLON = 126.0;
    const OLAT = 38.0;
    const XO = 43;
    const YO = 136;
    const DEGRAD = Math.PI / 180.0;
    const re = RE / GRID;
    const slat1 = SLAT1 * DEGRAD;
    const slat2 = SLAT2 * DEGRAD;
    const olon = OLON * DEGRAD;
    const olat = OLAT * DEGRAD;
    let sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
    let ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);
    let ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
    ra = re * sf / Math.pow(ra, sn);
    let theta = lon * DEGRAD - olon;
    if (theta > Math.PI) theta -= 2.0 * Math.PI;
    if (theta < -Math.PI) theta += 2.0 * Math.PI;
    theta *= sn;
    const x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
    const y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
    return { nx: x, ny: y };
};

// 현재 시간에 맞는 base_date와 base_time을 계산하는 헬퍼 함수
const getBaseDateTime = () => {
    const now = new Date();
    let base_date = now.getFullYear().toString() +
        String(now.getMonth() + 1).padStart(2, '0') +
        String(now.getDate()).padStart(2, '0');
    let base_time = "";

    // 기상청 API는 특정 시간에 예보를 발표 (0200, 0500, 0800, ...)
    // 안정적인 데이터 수신을 위해 현재 시간보다 이전 발표 시간을 사용
    const currentHour = now.getHours();
    if (currentHour < 2) {
        // 자정 이전에는 전날 2300 예보 사용
        now.setDate(now.getDate() - 1);
        base_date = now.getFullYear().toString() +
            String(now.getMonth() + 1).padStart(2, '0') +
            String(now.getDate()).padStart(2, '0');
        base_time = "2300";
    } else if (currentHour < 5) {
        base_time = "0200";
    } else if (currentHour < 8) {
        base_time = "0500";
    } else if (currentHour < 11) {
        base_time = "0800";
    } else if (currentHour < 14) {
        base_time = "1100";
    } else if (currentHour < 17) {
        base_time = "1400";
    } else if (currentHour < 20) {
        base_time = "1700";
    } else if (currentHour < 23) {
        base_time = "2000";
    } else {
        base_time = "2300";
    }
    return { base_date, base_time };
};

/**
 * 특정 위치의 날씨 정보를 기상청 API로부터 가져옵니다.
 * @param {number} lat 위도
 * @param {number} lon 경도
 * @returns {object|null} 날씨 데이터 객체 또는 실패 시 null
 */
exports.getWeatherForLocation = async (lat, lon) => {
    const { nx, ny } = convertToGrid(lat, lon);
    const { base_date, base_time } = getBaseDateTime();

    try {
        const fullUrl = `http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst` +
            `?serviceKey=${process.env.SERVICE_KEY_WT}&pageNo=1&numOfRows=500&dataType=XML` +
            `&base_date=${base_date}&base_time=${base_time}&nx=${nx}&ny=${ny}`;

        const response = await axios.get(fullUrl);
        const result = await xml2js.parseStringPromise(response.data, { explicitArray: false });
        const items = result?.response?.body?.items?.item;

        if (!items) return null;

        const weatherData = { currentTemp: null, sky: null, minTemp: null, maxTemp: null, precipType: null, precipProbability: null };
        for (const item of Array.isArray(items) ? items : [items]) {
            const category = item.category;
            const value = item.fcstValue;

            if (category === 'TMP' && !weatherData.currentTemp) weatherData.currentTemp = value;
            if (category === 'TMN' && !weatherData.minTemp) weatherData.minTemp = value;
            if (category === 'TMX' && !weatherData.maxTemp) weatherData.maxTemp = value;
            if (category === 'SKY' && !weatherData.sky) {
                if (value === '1') weatherData.sky = '맑음';
                else if (value === '3') weatherData.sky = '구름 많음';
                else if (value === '4') weatherData.sky = '흐림';
            }
            if (category === 'PTY' && !weatherData.precipType) {
                if (value === '0') weatherData.precipType = '없음';
                else if (value === '1') weatherData.precipType = '비';
                else if (value === '2') weatherData.precipType = '비/눈';
                else if (value === '3') weatherData.precipType = '눈';
            }
            if (category === 'POP' && !weatherData.precipProbability) weatherData.precipProbability = value;
        }
        return weatherData;

    } catch (error) {
        console.error('기상청 API 오류:', error.message);
        return null;
    }
};