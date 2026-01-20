package com.baek.diract.presentation.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.baek.diract.R

/**
 * 로딩 오버레이 컴포넌트
 *
 * 사용 예시:
 * ```
 * // Fragment에서 사용
 * private val loadingOverlay by lazy { LoadingOverlay(this) }
 *
 * // 로딩 표시
 * loadingOverlay.show()
 *
 * // 로딩 숨기기
 * loadingOverlay.hide()
 *
 * // UiState와 함께 사용
 * viewModel.uiState.collect { state ->
 *     loadingOverlay.setVisible(state is UiState.Loading)
 * }
 * ```
 */
class LoadingOverlay(private val fragment: Fragment) {

    private var overlayView: View? = null

    private fun getOverlayView(): View {
        if (overlayView == null) {
            val rootView = fragment.view as? ViewGroup ?: return View(fragment.requireContext())
            overlayView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.view_loading_overlay, rootView, false)
        }
        return overlayView!!
    }

    fun show() {
        val rootView = fragment.view as? ViewGroup ?: return
        val overlay = getOverlayView()

        if (overlay.parent == null) {
            rootView.addView(overlay)
        }
        overlay.visibility = View.VISIBLE
    }

    fun hide() {
        overlayView?.visibility = View.GONE
    }

    fun setVisible(visible: Boolean) {
        if (visible) show() else hide()
    }
}