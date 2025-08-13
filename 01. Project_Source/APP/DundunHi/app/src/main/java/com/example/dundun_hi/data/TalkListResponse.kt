package com.example.dundun_hi.data


import com.google.gson.annotations.SerializedName

data class TalkItemDto(
    @SerializedName("talk_id")     val talkId: Int,
    @SerializedName("sender_type") val senderType: Int,
    @SerializedName("sender_id")   val senderId: Int,
    @SerializedName("image_url")   val imageUrl: String,
    @SerializedName("send_at")     val sendAt: String,
    @SerializedName("is_read")     val isRead: Int,
    @SerializedName("read_at")     val readAt: String?
)

data class TalkListResponse(
    @SerializedName("rsCode")  val rsCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: List<TalkItemDto>
)
