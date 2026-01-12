package com.baek.diract.data.remote.dto

import com.google.firebase.Timestamp

data class MembersDto(
    val user_id: String = "",
    val joined_at: Timestamp? = null
)
