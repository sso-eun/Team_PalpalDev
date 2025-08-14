package com.example.dundun_hi.data

import com.google.gson.annotations.SerializedName

data class UploadTalkResponse(
    @SerializedName("rsCode") val rsCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("path") val filePath: String  // "path" 필드를 filePath로 매핑

) 