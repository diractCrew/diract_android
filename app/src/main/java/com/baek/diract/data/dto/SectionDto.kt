package com.baek.diract.data.dto

import com.baek.diract.domain.model.Section
import com.google.firebase.Timestamp

data class SectionDto(
    val section_id: String = "",
    val section_title: String = "",
    val created_at: Timestamp? = null,
    val updated_at: Timestamp? = null
) {
    fun toDomain(): Section = Section(
        id = section_id,
        title = section_title
    )
}
