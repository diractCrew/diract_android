package com.baek.diract.domain.model

import android.net.Uri

data class CompressionResult(
    val compressedUri: Uri,
    val durationSeconds: Double
)
