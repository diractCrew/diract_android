package com.baek.diract.presentation.common.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.baek.diract.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val DIM_AMOUNT = 0.4f

/**
 * 공통 다이얼로그 빌더
 *
 * 사용 예시
 * // 확인 다이얼로그 (버튼 1개)
 *   BasicDialog.confirm(
 *       context = requireContext(),
 *       title = "존재하지 않는 동영상입니다",
 *       message = "삭제된 동영상입니다"
 *   ).show()
 *
 *   // 위험 액션 다이얼로그 (버튼 2개, 빨간 버튼)
 *   BasicDialog.destructive(
 *       context = requireContext(),
 *       title = "벨카젤줄리파 팀 스페이스를 삭제하시겠어요?",
 *       message = "팀 스페이스와 멤버 목록이 모두 초기화되며 되돌릴 수 없습니다.",
 *       positiveText = "삭제",
 *       onPositive = { /* 삭제 처리 */ }
 *   ).show()
 *
 */

object BasicDialog {

    //확인 버튼만 있는 다이얼로그
    fun confirm(
        context: Context,
        title: String,
        message: String? = null,
        confirmText: String = context.getString(R.string.dialog_confirm),
        onConfirm: (() -> Unit)? = null
    ): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context, R.style.Theme_Diract_Dialog_Confirm)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } }
            .setPositiveButton(confirmText) { d, _ ->
                onConfirm?.invoke()
                d.dismiss()
            }
            .create()
        return dialog
    }

    //버튼 두개짜리 다이얼로그
    fun destructive(
        context: Context,
        title: String,
        message: String? = null,
        negativeText: String = context.getString(R.string.dialog_cancel),
        positiveText: String = context.getString(R.string.dialog_delete),
        onNegative: (() -> Unit)? = null,
        onPositive: (() -> Unit)? = null
    ): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context, R.style.Theme_Diract_Dialog_Destructive)
            .setTitle(title)
            .apply { message?.let { setMessage(it) } }
            .setNegativeButton(negativeText) { d, _ ->
                onNegative?.invoke()
                d.dismiss()
            }
            .setPositiveButton(positiveText) { d, _ ->
                onPositive?.invoke()
                d.dismiss()
            }
            .create()
        return dialog
    }
}
