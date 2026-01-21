package com.baek.diract.presentation.home.video.upload

import android.net.Uri

data class GalleryVideoItem (
    val id: Long,
    val uri: Uri,
    val duration: Long,     // 밀리초 단위
    val isFavorite: Boolean, // 즐겨찾기 여부
    var isSelected: Boolean = false
)
