package com.baek.diract.data.remote.dto

import java.sql.Timestamp

data class UserTeamspaceDto(
    val teamspace_id: String = "",
    val joined_at: Timestamp? = null
)
