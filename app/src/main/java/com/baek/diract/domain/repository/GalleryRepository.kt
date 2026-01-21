package com.baek.diract.domain.repository

import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.model.GalleryVideo

interface GalleryRepository {

    //모든 비디오 가져오기
    suspend fun getGalleryVideos(): DataResult<List<GalleryVideo>>
}
