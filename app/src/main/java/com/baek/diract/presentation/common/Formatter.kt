package com.baek.diract.presentation.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatter {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

    fun LocalDate.toUiString(): String {
        return this.format(DATE_FORMATTER)
    }

    fun Double.toTimeString(): String {
        val totalSeconds = this.toInt()
        val minutes = totalSeconds / 60
        val secs = totalSeconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    fun Long.toTimeString(): String {
        val totalSeconds = this / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
