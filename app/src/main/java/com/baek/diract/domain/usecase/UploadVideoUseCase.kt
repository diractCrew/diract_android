package com.baek.diract.domain.usecase

import android.net.Uri
import com.baek.diract.data.util.VideoCompressor
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.repository.VideoRepository
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    private val videoCompressor: VideoCompressor,
    private val videoRepository: VideoRepository
) {
    // 업로드 상태
    sealed interface UploadState {
        data class Compressing(val progress: Int) : UploadState
        data class Uploading(val progress: Int) : UploadState
        data object Completed : UploadState
        data class Failed(val phase: FailPhase, val message: String?) : UploadState
    }

    enum class FailPhase { COMPRESSION, UPLOAD }

    suspend operator fun invoke(
        videoUri: Uri,
        title: String,
        tracksId: String,
        sectionId: String,
        uploaderId: String,
        onStateChanged: (UploadState) -> Unit
    ): DataResult<Unit> {
        return try {
            // 1. 압축
            onStateChanged(UploadState.Compressing(0))

            val compressionResult = videoCompressor.compress(
                inputUri = videoUri,
                progressListener = object : VideoCompressor.ProgressListener {
                    override fun onProgress(progress: Int) {
                        onStateChanged(UploadState.Compressing(progress))
                    }
                }
            )

            // 2. 업로드
            onStateChanged(UploadState.Uploading(0))

            val result = videoRepository.addVideo(
                tracksId = tracksId,
                sectionId = sectionId,
                videoUri = compressionResult.compressedUri,
                title = title,
                duration = compressionResult.durationSeconds,
                uploaderId = uploaderId,
                onProgress = { progress ->
                    onStateChanged(UploadState.Uploading(progress))
                }
            )

            // 3. 결과 처리
            when (result) {
                is DataResult.Success -> {
                    onStateChanged(UploadState.Completed)
                    DataResult.Success(Unit)
                }
                is DataResult.Error -> {
                    onStateChanged(UploadState.Failed(FailPhase.UPLOAD, result.throwable.message))
                    result
                }
            }
        } catch (e: Exception) {
            onStateChanged(UploadState.Failed(FailPhase.COMPRESSION, e.message))
            DataResult.Error(e)
        }
    }
}