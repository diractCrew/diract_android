package com.baek.diract.presentation.common

import android.text.InputFilter
import android.text.Spanned

/**
 * maxLength 초과 입력 시도 시 콜백을 호출하는 InputFilter
 *
 * 사용 예시:
 * ```
 * editText.filters = arrayOf(MaxLengthInputFilter(20) { showError() })
 *
 * private fun showError() {
 *         isError = true
 *         binding.counterTxt.isVisible = true
 *         binding.counterTxt.text = getString(R.string.input_dialog_error_max_length, maxLength)
 *         binding.counterTxt.setTextColor(requireContext().getColor(R.color.accent_red_normal))
 *     }
 * ```
 */
class MaxLengthInputFilter(
    private val maxLength: Int,
    private val onExceed: (() -> Unit)? = null
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val sourceLength = source?.length ?: 0
        val destLength = dest?.length ?: 0
        val keep = maxLength - (destLength - (dend - dstart))

        return when {
            keep <= 0 -> {
                if (sourceLength > 0) onExceed?.invoke()
                ""
            }
            keep >= end - start -> {
                null
            }
            else -> {
                onExceed?.invoke()
                source?.subSequence(start, start + keep)
            }
        }
    }
}
