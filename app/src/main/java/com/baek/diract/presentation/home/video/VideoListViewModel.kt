package com.baek.diract.presentation.home.video

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.repository.AuthRepository
import com.baek.diract.domain.repository.VideoRepository
import com.baek.diract.domain.usecase.UploadVideoUseCase
import com.baek.diract.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val videoRepository: VideoRepository,
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    val trackId: String = checkNotNull(savedStateHandle[KEY_TRACK_ID]) {
        "trackId값 없이 VideoList 접근이 불가능합니다."
    }
    val trackTitle: String? = savedStateHandle[KEY_TRACK_TITLE]

    // 비디오 아이템
    private val _videoItems = MutableStateFlow<List<VideoCardItem>>(emptyList())
    val videoItems: StateFlow<List<VideoCardItem>> = _videoItems.asStateFlow()

    private val _sectionChipItems = MutableStateFlow<List<SectionChipItem>>(emptyList())
    val sectionChipItems: StateFlow<List<SectionChipItem>> = _sectionChipItems.asStateFlow()

    //로딩, 성공 실패 관리
    private val _uiState =
        MutableStateFlow<UiState<Long>>(UiState.None) //로딩 상태 변화 관측을 위한 System.currentTimeMillis() 값 넣음
    val uiState: StateFlow<UiState<Long>> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    // 현재 비디오 리스트 가져오기
    private fun getCurrentVideoList(): List<VideoCardItem> {
        return _videoItems.value
    }

    // 비디오 리스트 업데이트
    private fun updateVideoList(transform: (List<VideoCardItem>) -> List<VideoCardItem>) {
        val currentList = getCurrentVideoList()
        _videoItems.value = transform(currentList)
    }

    // 초기 데이터 로드 (섹션 -> 비디오)
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // 1. 섹션 로드
            when (val sectionsResult = videoRepository.getSections(trackId)) {
                is DataResult.Success -> {
                    val sections = sectionsResult.data

                    // "일반" 섹션 찾기, 없으면 첫 번째 섹션 선택
                    val defaultSection = sections.find { it.title == DEFAULT_SECTION_TITLE }
                        ?: sections.firstOrNull()

                    // SectionChipItem으로 변환
                    val sectionItems = buildList {
                        add(SectionChipItem.SetSection)
                        addAll(sections.map { section ->
                            SectionChipItem.SectionUi(
                                id = section.id,
                                name = section.title,
                                isSelected = section.id == defaultSection?.id
                            )
                        })
                    }
                    _sectionChipItems.value = sectionItems

                    // 2. 선택된 섹션의 비디오 로드
                    if (defaultSection != null) {
                        loadVideosForSection(defaultSection.id)
                    } else {
                        _videoItems.value = emptyList()
                        _uiState.value = UiState.Success(System.currentTimeMillis())
                    }
                }

                is DataResult.Error -> {
                    Log.e(TAG, "Failed to load sections", sectionsResult.throwable)
                    _uiState.value = UiState.Error(
                        message = sectionsResult.throwable.message,
                        throwable = sectionsResult.throwable
                    )
                }
            }
        }
    }

    // 특정 섹션의 비디오 로드
    private suspend fun loadVideosForSection(sectionId: String) {
        when (val videosResult = videoRepository.getVideos(trackId, sectionId)) {
            is DataResult.Success -> {
                val completedItems = videosResult.data.map { VideoCardItem.Completed(it) }
                // 진행 중인 아이템 유지
                val inProgressItems = getCurrentVideoList().filter { item ->
                    item is VideoCardItem.Compressing ||
                            item is VideoCardItem.Uploading ||
                            item is VideoCardItem.Failed
                }
                _videoItems.value = inProgressItems + completedItems
                _uiState.value = UiState.Success(System.currentTimeMillis())
            }

            is DataResult.Error -> {
                Log.e(TAG, "Failed to load videos", videosResult.throwable)
                _uiState.value = UiState.Error(
                    message = videosResult.throwable.message,
                    throwable = videosResult.throwable
                )
            }
        }
    }

    // 실패 아이템 취소 (리스트에서 제거)
    fun cancelFailedItem(item: VideoCardItem.Failed) {
        updateVideoList { list -> list.filter { it.id != item.id } }
    }

    // 실패 아이템 재시도
    fun retryFailedItem(item: VideoCardItem.Failed) {
        val retryInfo = item.retryInfo ?: return
        val selectedSectionId = getSelectedSectionId() ?: return
        val uploaderId = authRepository.getCurrentUser()?.uid ?: return
        val retryId = item.id

        when (item.type) {
            // 압축 실패: 처음부터 다시 시작
            FailType.COMPRESSION -> {
                changeToCompressingState(retryId)
                viewModelScope.launch {
                    uploadVideoUseCase(
                        videoUri = retryInfo.originalUri,
                        title = retryInfo.title,
                        tracksId = trackId,
                        sectionId = selectedSectionId,
                        uploaderId = uploaderId,
                        onStateChanged = { state ->
                            handleUploadState(retryId, state, retryInfo)
                        }
                    )
                }
            }
            // 업로드 실패: 업로드만 재시도
            FailType.UPLOAD -> {
                val compressedUri = retryInfo.compressedUri ?: return
                val duration = retryInfo.duration ?: return

                changeToUploadingState(retryId)
                viewModelScope.launch {
                    uploadVideoUseCase.uploadOnly(
                        compressedUri = compressedUri,
                        duration = duration,
                        title = retryInfo.title,
                        tracksId = trackId,
                        sectionId = selectedSectionId,
                        uploaderId = uploaderId,
                        onStateChanged = { state ->
                            handleUploadState(retryId, state, retryInfo)
                        }
                    )
                }
            }
            // EXCEEDED는 재시도 불가
            FailType.EXCEEDED -> return
        }
    }

    // 업로드 상태 변경 처리 (공통 로직)
    private fun handleUploadState(
        uploadId: String,
        state: UploadVideoUseCase.UploadState,
        retryInfo: RetryInfo
    ) {
        when (state) {
            is UploadVideoUseCase.UploadState.Compressing -> {
                updateCompressionProgress(uploadId, state.progress)
            }

            is UploadVideoUseCase.UploadState.Uploading -> {
                changeToUploadingState(uploadId)
                updateUploadProgress(uploadId, state.progress)
            }

            is UploadVideoUseCase.UploadState.Completed -> {
                removeItem(uploadId)
                refresh()
            }

            is UploadVideoUseCase.UploadState.Failed -> {
                val failType = when (state.phase) {
                    UploadVideoUseCase.FailPhase.COMPRESSION -> FailType.COMPRESSION
                    UploadVideoUseCase.FailPhase.UPLOAD -> FailType.UPLOAD
                }
                val updatedRetryInfo = retryInfo.copy(
                    compressedUri = state.compressedUri ?: retryInfo.compressedUri,
                    duration = state.duration ?: retryInfo.duration
                )
                changeToFailedState(uploadId, failType, state.message, updatedRetryInfo)
            }
        }
    }

    // 압축 상태로 변경
    private fun changeToCompressingState(id: String) {
        updateVideoList { list ->
            list.map { item ->
                if (item.id == id) {
                    VideoCardItem.Compressing(id = id, progress = 0)
                } else {
                    item
                }
            }
        }
    }

    // 섹션 선택
    fun selectSection(sectionId: String) {
        _sectionChipItems.value = _sectionChipItems.value.map { item ->
            when (item) {
                is SectionChipItem.SectionUi -> item.copy(isSelected = item.id == sectionId)
                else -> item
            }
        }

        // 선택된 섹션의 비디오 로드
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            loadVideosForSection(sectionId)
        }
    }

    // 비디오 업로드 (압축 -> 업로드)
    fun uploadVideo(videoUri: Uri, title: String) {
        val uploadId = UUID.randomUUID().toString()
        val selectedSectionId = getSelectedSectionId() ?: return
        val uploaderId = authRepository.getCurrentUser()?.uid ?: return

        // 압축 상태로 리스트에 추가
        addCompressingItem(uploadId)

        viewModelScope.launch {
            val result = uploadVideoUseCase(
                videoUri = videoUri,
                title = title,
                tracksId = trackId,
                sectionId = selectedSectionId,
                uploaderId = uploaderId,
                onStateChanged = { state ->
                    when (state) {
                        is UploadVideoUseCase.UploadState.Compressing -> {
                            updateCompressionProgress(uploadId, state.progress)
                        }

                        is UploadVideoUseCase.UploadState.Uploading -> {
                            changeToUploadingState(uploadId)
                            updateUploadProgress(uploadId, state.progress)
                        }

                        is UploadVideoUseCase.UploadState.Completed -> {
                            removeItem(uploadId)
                            refresh()
                        }

                        is UploadVideoUseCase.UploadState.Failed -> {
                            val failType = when (state.phase) {
                                UploadVideoUseCase.FailPhase.COMPRESSION -> FailType.COMPRESSION
                                UploadVideoUseCase.FailPhase.UPLOAD -> FailType.UPLOAD
                            }
                            val retryInfo = RetryInfo(
                                originalUri = videoUri,
                                title = title,
                                compressedUri = state.compressedUri,
                                duration = state.duration
                            )
                            changeToFailedState(uploadId, failType, state.message, retryInfo)
                        }
                    }
                }
            )

            if (result is DataResult.Error) {
                Log.e("VideoListViewModel", "Upload failed", result.throwable)
            }
        }
    }

    // 현재 선택된 섹션 ID 반환
    private fun getSelectedSectionId(): String? {
        return _sectionChipItems.value
            .filterIsInstance<SectionChipItem.SectionUi>()
            .find { it.isSelected }
            ?.id
    }

    // 압축 중 아이템 추가
    private fun addCompressingItem(id: String) {
        updateVideoList { list ->
            listOf(VideoCardItem.Compressing(id = id, progress = 0)) + list
        }
    }

    // 압축 진행률 업데이트
    private fun updateCompressionProgress(id: String, progress: Int) {
        updateVideoList { list ->
            list.map { item ->
                if (item.id == id && item is VideoCardItem.Compressing) {
                    item.copy(progress = progress)
                } else {
                    item
                }
            }
        }
    }

    // 업로드 상태로 변경
    private fun changeToUploadingState(id: String) {
        updateVideoList { list ->
            list.map { item ->
                if (item.id == id) {
                    VideoCardItem.Uploading(id = id, progress = 0)
                } else {
                    item
                }
            }
        }
    }

    // 실패 상태로 변경
    private fun changeToFailedState(
        id: String,
        type: FailType,
        message: String?,
        retryInfo: RetryInfo
    ) {
        updateVideoList { list ->
            list.map { item ->
                if (item.id == id) {
                    VideoCardItem.Failed(
                        id = id,
                        type = type,
                        message = message,
                        retryInfo = retryInfo
                    )
                } else {
                    item
                }
            }
        }
    }

    // 아이템 제거
    private fun removeItem(id: String) {
        updateVideoList { list -> list.filter { it.id != id } }
    }

    // 새로고침
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val selectedSectionId = getSelectedSectionId()
            Log.d(TAG, "선택된 섹션: $selectedSectionId")
            if (selectedSectionId != null) {
                loadVideosForSection(selectedSectionId)
                return@launch
            }
            _uiState.value = UiState.Success(System.currentTimeMillis())
        }
    }

    // 업로드 진행률 업데이트
    private fun updateUploadProgress(id: String, progress: Int) {
        updateVideoList { list ->
            list.map { item ->
                if (item.id == id && item is VideoCardItem.Uploading) {
                    item.copy(progress = progress)
                } else {
                    item
                }
            }
        }
    }

    companion object {
        private const val TAG = "VideoListViewModel"
        private const val KEY_TRACK_ID = "tracksId"
        private const val KEY_TRACK_TITLE = "tracksTitle"
        private const val DEFAULT_SECTION_TITLE = "일반"
    }
}
