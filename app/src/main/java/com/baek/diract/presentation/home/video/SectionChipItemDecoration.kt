package com.baek.diract.presentation.home.video

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SectionChipItemDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position < state.itemCount - 1) {
            outRect.right = spacing
        }
    }
}