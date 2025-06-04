package com.example.dundun_hi.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.CodeAuthRepository
import com.example.dundun_hi.data.CodeAuthSendResponse
import com.example.dundun_hi.data.CodeAuthVerifyResponse
import com.example.dundun_hi.data.SignupRepository
import com.example.dundun_hi.data.SignupRequest
import com.example.dundun_hi.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val repo: SignupRepository = SignupRepository()
) : ViewModel() {

    // ---------------- SMS 인증 ----------------
    private val codeAuthRepository = CodeAuthRepository(RetrofitClient.codeAuthService)

    // 잠시만 번호 지정 나중에 주석처리한거로 바꾸기!____---------------------------------------------------
//    var lastTelNum: String = "0102222"
//        private set

//    //1) 마지막에 보낸 전화번호 저장
    var lastTelNum: String = ""
        private set

    // 2) 인증번호 발송 결과
    private val _sendCodeResult = MutableStateFlow<CodeAuthSendResponse?>(null)
    val sendCodeResult: StateFlow<CodeAuthSendResponse?> = _sendCodeResult

    // 3) 인증번호 검증 결과
    private val _verifyCodeResult = MutableStateFlow<CodeAuthVerifyResponse?>(null)
    val verifyCodeResult: StateFlow<CodeAuthVerifyResponse?> = _verifyCodeResult

    // 발송 요청
    fun sendVerificationCode(telNum: String) {
        lastTelNum = telNum
        viewModelScope.launch {
            try {
                _sendCodeResult.value = codeAuthRepository.sendCode(telNum)
            } catch (e: Exception) {
                _sendCodeResult.value =
                    CodeAuthSendResponse(-1, e.localizedMessage ?: "네트워크 오류")
            }
        }
    }

    // 검증 요청
    fun verifyAuthCode(authCode: String) {
        viewModelScope.launch {
            try {
                _verifyCodeResult.value =
                    codeAuthRepository.verifyCode(lastTelNum, authCode)
            } catch (e: Exception) {
                _verifyCodeResult.value =
                    CodeAuthVerifyResponse(-1, e.localizedMessage ?: "네트워크 오류")
            }
        }
    }

    // ---------------- 회원가입 ----------------
    private val _state = MutableStateFlow<SignupResult>(SignupResult.Idle)
    val state: StateFlow<SignupResult> = _state

    // 성공 시 저장할 userId
    var lastUserId: String = ""
        private set


    // 회원가입 후 로딩-> 메인 ----------------------------------------------------------------
    //이거 해결한다고 2일은 걸렸다
    // lastUserId에 resp.userId가 아닌 req.user_id를 넣어야됨(statevalue에도)
    // 왜냐하면 resp에 userId가 정의되어있지만 서버에서 실제로 보내는 값은 userNum이랑 message밖에 없어서
    // 계속 비어있는 값을 지니고 있어서 안넘어갔던거임
    fun signup(req: SignupRequest) = viewModelScope.launch {
        try {
            val resp = repo.signup(req)
            if (resp.message == "회원가입 성공") {
                lastUserId = req.user_id
                _state.value = SignupResult.Success(lastUserId, resp.userNum)
            } else {
                _state.value = SignupResult.Error(resp.message)
            }
        } catch (e: Exception) {
            _state.value = SignupResult.Error(e.message ?: "네트워크 오류")
        }
    }
}

