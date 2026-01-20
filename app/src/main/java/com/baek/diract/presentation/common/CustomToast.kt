package com.baek.diract.presentation.common

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.baek.diract.databinding.LayoutCustomToastBinding

/**
 * 커스텀 토스트 메시지
 *
 * 사용 예시:
 * ```
 * CustomToast.show(context, "곡 이름은 20자 이하로 입력해 주세요.")
 * CustomToast.show(context, R.string.error_message)
 * ```
 */
object CustomToast {

    // checkIcon이 있는 토스트
    fun showPositive(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
        val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(context))

        binding.positiveIcon.visibility = View.VISIBLE
        binding.negativeIcon.visibility = View.GONE
        binding.toastMessage.text = message

        Toast(context).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            this.duration = duration
            view = binding.root
            show()
        }
    }

    //errorIcon이 있는 토스트
    fun showPositive(context: Context, messageResId: Int, duration: Int = Toast.LENGTH_LONG) {
        showPositive(context, context.getString(messageResId), duration)
    }

    fun showNegative(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
        val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(context))

        binding.positiveIcon.visibility = View.GONE
        binding.negativeIcon.visibility = View.VISIBLE
        binding.toastMessage.text = message

        Toast(context).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            this.duration = duration
            view = binding.root
            show()
        }
    }

    fun showNegative(context: Context, messageResId: Int, duration: Int = Toast.LENGTH_LONG) {
        showNegative(context, context.getString(messageResId), duration)
    }

    //아이콘 없는 토스트
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
        val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(context))

        binding.positiveIcon.visibility = View.GONE
        binding.negativeIcon.visibility = View.GONE
        binding.toastMessage.text = message

        Toast(context).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            this.duration = duration
            view = binding.root
            show()
        }
    }

    fun show(context: Context, messageResId: Int, duration: Int = Toast.LENGTH_LONG) {
        show(context, context.getString(messageResId), duration)
    }

}
