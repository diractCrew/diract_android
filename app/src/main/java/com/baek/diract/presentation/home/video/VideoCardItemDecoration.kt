package com.baek.diract.presentation.home.video

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoCardItemDecoration(
    private val spanCount: Int,
    private val horizontalSpacing: Int,
    private val verticalSpacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        // 가로 간격: 각 아이템이 균등한 간격을 갖도록 계산
        outRect.left = horizontalSpacing * column / spanCount
        outRect.right = horizontalSpacing * (spanCount - 1 - column) / spanCount

        // 세로 간격: 첫 번째 행 제외하고 상단에 간격 추가
        if (position >= spanCount) {
            outRect.top = verticalSpacing
        }
    }
}
