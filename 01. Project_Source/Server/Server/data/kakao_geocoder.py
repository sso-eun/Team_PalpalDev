# 2025-05-18
# 도로명 주소 -> 위도/경도로 변환
# author : eunjae

import pandas as pd
import requests
import time
import re

# Kakao REST API 키 (본인의 키로 교체 가능)
KAKAO_API_KEY = "d0562198606a341b7832c14130744155"
headers = {"Authorization": f"KakaoAK {KAKAO_API_KEY}"}

# 주소 전처리 함수 (괄호, 상세주소 제거 + 도 이름 붙이기)
def clean_address(raw_addr):
    addr = raw_addr.strip()
    addr = re.sub(r"\(.*?\)", "", addr)  # 괄호 제거
    addr = re.sub(r",.*", "", addr)        # 쉼표 뒤 제거
    addr = "충청북도 " + addr
    return addr.strip()

# Kakao API 호출 함수
def get_coords(address):
    url = "https://dapi.kakao.com/v2/local/search/address.json"
    params = {"query": address}
    res = requests.get(url, headers=headers, params=params)
    try:
        if res.status_code == 200:
            documents = res.json().get("documents")
            if documents:
                x = float(documents[0]['x'])  # 경도
                y = float(documents[0]['y'])  # 위도
                return y, x
    except:
        return None, None
    return None, None

# CSV 파일을 읽고 좌표 변환 후 저장
def geocode_csv(input_path, output_path):
    df = pd.read_csv(input_path, encoding='cp949')
    df["위도"] = None
    df["경도"] = None

    for idx, row in df.iterrows():
        raw_addr = row["소재지도로명주소"]
        address = clean_address(raw_addr)

        lat, lon = get_coords(address)
        df.at[idx, "위도"] = lat
        df.at[idx, "경도"] = lon
        print(f"[{idx+1}] {address} → 위도: {lat}, 경도: {lon}")
        time.sleep(0.3)

    df.to_csv(output_path, index=False, encoding='utf-8-sig')
    print(f"\n저장 완료: {output_path}")

# 실행 예시
if __name__ == "__main__":
    geocode_csv("cheongju_center_2024.csv", "cheongju_center_with_coords.csv")
