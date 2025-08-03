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

import androidx.lifecycle.ViewModelProvider

class FamilyCertViewModel(private val apiService: MemberService) : ViewModel() {

    // --- 상태 관리를 위한 변수 선언 ---
    private val _uploadResult = MutableStateFlow<FileUploadResponse?>(null)
    val uploadResult = _uploadResult.asStateFlow()


    fun uploadCertificate(context: Context, uri: Uri, userNum: Int, seniorNum: Int) {
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