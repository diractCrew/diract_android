package com.baek.diract.domain.model

import android.net.Uri
import com.baek.diract.presentation.home.video.upload.GalleryVideoItem

/**
 * 갤러리에서 가져온 비디오 정보
 */
data class GalleryVideo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val duration: Long,     // 밀리초 단위
    val size: Long,         // 바이트 단위
    val dateAdded: Long,    // Unix timestamp
    val isFavorite: Boolean // 즐겨찾기 여부
) {
    fun toUiItem(selected: Boolean = false): GalleryVideoItem {
        return GalleryVideoItem(
            id = id,
            uri = uri,
            duration = duration,
            isFavorite = isFavorite,
            isSelected = selected
        )
    }
}
