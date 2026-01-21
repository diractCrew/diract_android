package com.baek.diract.presentation.home.video.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baek.diract.domain.common.DataResult
import com.baek.diract.domain.model.GalleryVideo
import com.baek.diract.domain.repository.GalleryRepository
import com.baek.diract.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    // 갤러리 로딩 상태
    private val _galleryState = MutableStateFlow<UiState<Unit>>(UiState.None)
    val galleryState: StateFlow<UiState<Unit>> = _galleryState.asStateFlow()

    // 전체 갤러리 비디오 목록 (단일 소스)
    private val _allVideos = MutableStateFlow<List<GalleryVideo>>(emptyList())

    // 현재 필터 (전체/즐겨찾기)
    private val _currentFilter = MutableStateFlow(VideoFilter.ALL)
    val currentFilter: StateFlow<VideoFilter> = _currentFilter.asStateFlow()

    // 선택된 비디오
    private val _selectedVideoId = MutableStateFlow<Long?>(null)
    val selectedVideoId: StateFlow<Long?> = _selectedVideoId.asStateFlow()

    // 필터링된 비디오 목록
    val filteredVideos: StateFlow<List<GalleryVideoItem>> = combine(
        _allVideos,
        _currentFilter,
        _selectedVideoId
    ) { videos, filter,selectedId  ->
        val filtered = when (filter) {
            VideoFilter.ALL -> videos
            VideoFilter.FAVORITE -> videos.filter { it.isFavorite }
        }
        filtered.map { it.toUiItem(it.id == selectedId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    init {
        loadGalleryVideos()
    }

    // 갤러리 비디오 로드
    fun loadGalleryVideos() {
        viewModelScope.launch {
            _galleryState.value = UiState.Loading
            when (val result = galleryRepository.getGalleryVideos()) {
                is DataResult.Success -> {
                    _allVideos.value = result.data
                    _galleryState.value = UiState.Success(Unit)
                }

                is DataResult.Error -> {
                    _galleryState.value = UiState.Error(
                        message = result.throwable.message,
                        throwable = result.throwable
                    )
                }
            }
        }
    }

    // 필터 변경
    fun setFilter(filter: VideoFilter) {
        _currentFilter.value = filter
    }

    // 비디오 선택
    fun selectVideo(video: GalleryVideo) {
        _selectedVideoId.value = video.id
    }

    // 업로드 가능 여부
    fun canUpload(title: String): Boolean {
        return _selectedVideoId.value != null && title.isNotBlank()
    }
}

