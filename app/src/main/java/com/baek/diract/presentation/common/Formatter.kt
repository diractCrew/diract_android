package com.baek.diract.presentation.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Formatter {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    fun LocalDate.toUiString(): String {
        return this.format(DATE_FORMATTER)
    }

    fun Double.toUiString(): String {
        val totalSeconds = this.toInt()
        val minutes = totalSeconds / 60
        val secs = totalSeconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}
