package com.baek.diract.data.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.model.GalleryVideo
import com.baek.diract.domain.repository.GalleryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//갤러리 접근 repository
class GalleryRepositoryImpl @Inject constructor(
    @field:ApplicationContext private val context: Context
) : GalleryRepository {

    override suspend fun getGalleryVideos(): DataResult<List<GalleryVideo>> =
        withContext(Dispatchers.IO) {
            try {
                val result = queryGalleryVideos()
                DataResult.Success(result)
            } catch (e: Exception) {
                Log.e(TAG, "갤러리에서 영상 불러오기 실패", e)
                DataResult.Error(e)
            }
        }

    private fun queryGalleryVideos(): List<GalleryVideo> {
        val videos = mutableListOf<GalleryVideo>()

        // API 29 이상에서는 VOLUME_EXTERNAL 사용
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        // 조회할 컬럼 정의
        val projection = mutableListOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )

        // API 30 이상에서만 IS_FAVORITE 지원
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection.add(MediaStore.Video.Media.IS_FAVORITE)
        }

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        // MediaStore 쿼리 실행
        context.contentResolver.query(
            collection,
            projection.toTypedArray(),
            null,
            null,
            sortOrder
        )?.use { cursor ->
            // 컬럼 인덱스 조회
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val favoriteColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.Video.Media.IS_FAVORITE)
            } else {
                -1
            }

            // 결과를 GalleryVideo로 변환
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn) ?: ""
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val isFavorite = if (favoriteColumn != -1) {
                    cursor.getInt(favoriteColumn) == 1
                } else {
                    false
                }

                // content:// URI 생성
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                videos.add(
                    GalleryVideo(
                        id = id,
                        uri = contentUri,
                        displayName = displayName,
                        duration = duration,
                        size = size,
                        dateAdded = dateAdded,
                        isFavorite = isFavorite
                    )
                )
            }
        }

        return videos
    }

    companion object {
        const val TAG = "GalleryRepositoryImpl"
    }
}
