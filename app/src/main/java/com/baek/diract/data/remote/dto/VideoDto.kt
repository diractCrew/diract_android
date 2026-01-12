package com.baek.diract.data.remote.dto

import com.google.firebase.Timestamp

data class VideoDto(
    val video_id: String = "",
    val video_title: String = "",
    val video_duration: Int = 0,
    val video_url: String = "",
    val thumbnail_url: String = "",
    val uploader_id: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)
