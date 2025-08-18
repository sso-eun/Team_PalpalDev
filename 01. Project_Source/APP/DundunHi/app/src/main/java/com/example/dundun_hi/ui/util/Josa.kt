package com.example.dundun_hi.ui.util

// 간단한 조사 선택 유틸
fun josa(word: String, pair: String): String {
    val (front, back) = pair.split("/").let {
        if (it.size == 2) it[0] to it[1] else return ""
    }
    val last = word.trim().lastOrNull() ?: return back

    // 한글 여부 & 종성(받침) 계산
    val isHangul = last in '가'..'힣'
    val jong = if (isHangul) (last.code - 0xAC00) % 28 else 0
    val hasBatchim = isHangul && jong != 0

    // '으로/로' 특례: 받침이 없거나 'ㄹ(종성 8)'이면 '로'
    if (pair == "으로/로") {
        return if (!hasBatchim || jong == 8) "로" else "으로"
    }
    // 일반 규칙: 받침 있으면 앞, 없으면 뒤
    return if (hasBatchim) front else back
}
