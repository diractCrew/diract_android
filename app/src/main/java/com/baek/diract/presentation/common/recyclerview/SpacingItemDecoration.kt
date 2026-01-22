package com.baek.diract.presentation.common.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * recyclerView 아이템 간격 조절
 *
 * 사용예시:
 *
 * binding.rvSections.apply {
 *      adapter = sectionChipAdapter
 *      addItemDecoration(
 *          SpacingItemDecoration(
 *              resources.getDimensionPixelSize(R.dimen.section_chip_spacing), //아이템 간격
 *              RecyclerView.HORIZONTAL, //기본값 : Vertical
 *              false //RecyclerView의 양 끝에도 간격을 줄지 말지, 기본값: false
 *          )
 *      )
 * }
 */
class SpacingItemDecoration(
    private val spacing: Int,
    private val orientation: Int = RecyclerView.VERTICAL,
    private val includeEdge: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val isLast = position == state.itemCount - 1

        when (orientation) {
            RecyclerView.HORIZONTAL -> {
                if (position > 0) {
                    outRect.left = spacing
                }
                if (includeEdge) {
                    if (position == 0) outRect.left = spacing
                    if (isLast) outRect.right = spacing
                }
            }

            RecyclerView.VERTICAL -> {
                // 첫 번째 아이템 제외하고 top spacing 적용
                if (position > 0) {
                    outRect.top = spacing
                }
                if (includeEdge) {
                    if (position == 0) outRect.top = spacing
                    if (isLast) outRect.bottom = spacing
                }
            }
        }
    }
}
