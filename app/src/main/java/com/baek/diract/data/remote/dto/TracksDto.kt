package com.baek.diract.data.remote.dto

import com.google.firebase.Timestamp

data class TracksDto(
    val tracks_id: String = "",
    val project_id: String = "",
    val creator_id: String = "",
    val track_name: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)
