
export const formatDateTime = (isoString) => {
    if (!isoString) return '-'; // 값이 없으면 '-' 반환

    const noZ = isoString.replace('Z', '');
    const localDate = new Date(noZ);

    if (isNaN(localDate)) return '-'; // 유효하지 않은 날짜 처리

    const yyyy = localDate.getFullYear();
    const mm = String(localDate.getMonth() + 1).padStart(2, '0');
    const dd = String(localDate.getDate()).padStart(2, '0');
    const hh = String(localDate.getHours()).padStart(2, '0');
    const mi = String(localDate.getMinutes()).padStart(2, '0');
    const ss = String(localDate.getSeconds()).padStart(2, '0');

    return `${yyyy}.${mm}.${dd} ${hh}:${mi}:${ss}`;
};

// 연월일만 출력하는 함수
export const formatDateOnly = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    });
};