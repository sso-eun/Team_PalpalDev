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

    private var foundSenior: SearchedMember? = null

    // API 명세에 따라 user_tel로 검색-----------------------------------
    fun searchSenior(phone: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                // API 호출: field는 "user_tel", keyword는 입력받은 phone 사용
                val response = apiService.searchMember("user_tel", phone)
                if (response.isSuccessful && response.body() != null) {
                    val searchResult = response.body()!!.results
                    if (searchResult.isNotEmpty()) {
                        // 검색 결과가 있으면 첫 번째 사용자를 저장
                        foundSenior = searchResult[0]
                        _searchState.value = SearchState.Success(foundSenior!!)
                    } else {
                        // 검색 결과가 없으면
                        _searchState.value = SearchState.NotFound
                    }
                } else {
                    _searchState.value = SearchState.Error("검색에 실패했습니다.")
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "네트워크 오류")
            }
        }
    }
    // --------------------------------------------------------------

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