package com.baek.diract.data.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.baek.diract.domain.model.CompressionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class VideoCompressor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // 압축 진행률 콜백
    interface ProgressListener {
        fun onProgress(progress: Int)
    }
    // 비디오 압축
    suspend fun compress(
        inputUri: Uri,
        progressListener: ProgressListener? = null
    ): CompressionResult = suspendCancellableCoroutine { continuation ->
        val outputFile = createOutputFile()
        val outputPath = outputFile.absolutePath

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                    val durationSeconds = getVideoDuration(Uri.fromFile(outputFile))
                    continuation.resume(
                        CompressionResult(
                            compressedUri = Uri.fromFile(outputFile),
                            durationSeconds = durationSeconds
                        )
                    )
                }

                override fun onError(
                    composition: androidx.media3.transformer.Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    outputFile.delete()
                    continuation.resumeWithException(exportException)
                }
            })
            .build()

        val mediaItem = MediaItem.fromUri(inputUri)

        transformer.start(mediaItem, outputPath)

        // 진행률 업데이트를 위한 폴링
        if (progressListener != null) {
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val progressHolder = ProgressHolder()

            val progressRunnable = object : Runnable {
                override fun run() {
                    if (!continuation.isCompleted) {
                        val state = transformer.getProgress(progressHolder)
                        if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                            progressListener.onProgress(progressHolder.progress)
                        }
                        handler.postDelayed(this, 100)
                    }
                }
            }
            handler.post(progressRunnable)

            continuation.invokeOnCancellation {
                handler.removeCallbacks(progressRunnable)
                transformer.cancel()
                outputFile.delete()
            }
        } else {
            continuation.invokeOnCancellation {
                transformer.cancel()
                outputFile.delete()
            }
        }
    }

    // 비디오 길이 조회 (초 단위)
    fun getVideoDuration(uri: Uri): Double {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            durationMs / 1000.0
        } catch (e: Exception) {
            0.0
        } finally {
            retriever.release()
        }
    }

    private fun createOutputFile(): File {
        val cacheDir = File(context.cacheDir, "compressed_videos")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "compressed_${System.currentTimeMillis()}.mp4")
    }
}
