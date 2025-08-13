// 2025-05-23
// calculate distancs
// author : eunjae

// Haversine: 하버사인 공식 이용
function getDistance(lat1, lon1, lat2, lon2) {
    // 지구 반지름 (단위: meter)
    const R = 6371e3; 
    const toRad = angle => angle * (Math.PI / 180);

    const φ1 = toRad(lat1);
    const φ2 = toRad(lat2);
    const Δφ = toRad(lat2 - lat1);
    const Δλ = toRad(lon2 - lon1);

    const a = Math.sin(Δφ / 2) ** 2 +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    // meter 단위 거리
    return R * c; 
}

module.exports = { getDistance };
