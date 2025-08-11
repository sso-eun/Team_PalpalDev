// FamilyCertViewModel.kt
// 25-08-02 은재 추가
package com.example.dundun_hi.ui.signup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dundun_hi.data.FileUploadResponse
import com.example.dundun_hi.network.MemberService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.dundun_hi.data.SearchedMember
import androidx.lifecycle.ViewModelProvider
import com.example.dundun_hi.data.FindIdRequest


// ViewModel 외부에서 검색 상태를 표현하기 위한 sealed class
sealed class SearchState {
    object Idle : SearchState()         // 초기 상태
    object Loading : SearchState()      // 검색 중
    data class Success(val senior: SearchedMember) : SearchState()  // 성공
    object NotFound : SearchState()     // 결과 없음
    data class Error(val message: String) : SearchState()           // 에러
}
// -------------------------


class FamilyCertViewModel(private val apiService: MemberService) : ViewModel() {

    // --- 상태 관리를 위한 변수 선언 ---
    private val _uploadResult = MutableStateFlow<FileUploadResponse?>(null)
    val uploadResult = _uploadResult.asStateFlow()

    // --- 시니어 검색 관련 로직 추가 ---
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState = _searchState.asStateFlow()

    val foundSenior: SearchedMember?
        get() = (searchState.value as? SearchState.Success)?.senior

    // 회원 검증: onSuccess, onError 콜백을 제거하고 상태만 업데이트하도록 수정
    fun verifySenior(name: String, phone: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val response = apiService.findId(FindIdRequest(phone))
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.userId == name) {
                        // 이름과 전화번호 모두 일치: 상태를 Success로 변경
                        val seniorNum = result.userNum.toInt()
                        val senior = SearchedMember(seniorNum, name, phone)
                        _searchState.value = SearchState.Success(senior)
                    } else {
                        // 이름 불일치: 상태를 NotFound로 변경
                        _searchState.value = SearchState.NotFound
                    }
                } else {
                    // API 응답 실패: 상태를 NotFound로 변경
                    _searchState.value = SearchState.NotFound
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "네트워크 오류")
            }
        }
    }
    fun uploadCertificate(context: Context, uri: Uri, userNum: Int) {

        // 검색된 시니어의 userNum을 사용하도록 수정
        val seniorNum = foundSenior?.userNum ?: -1
        if (seniorNum == -1) {
            _uploadResult.value = FileUploadResponse(-1, "업로드 전 시니어를 먼저 검색해주세요.", "")
            return
        }
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes != null) {
                    val requestFile = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", "certificate.jpg", requestFile)

                    val response = apiService.uploadCertificate(userNum, seniorNum, body)

                    if (response.isSuccessful) {
                        _uploadResult.value = response.body()
                    } else {
                        _uploadResult.value = FileUploadResponse(-1, "Upload failed", "")
                    }
                }
            } catch (e: Exception) {
                _uploadResult.value = FileUploadResponse(-1, e.message ?: "Unknown error", "")
            }
        }
    }
}

class FamilyCertViewModelFactory(private val apiService: MemberService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyCertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FamilyCertViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}