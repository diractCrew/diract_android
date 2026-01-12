package com.baek.diract.data.remote.dto

import com.google.firebase.Timestamp

data class SectionDto(
    val section_id: String = "",
    val section_title: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
)
