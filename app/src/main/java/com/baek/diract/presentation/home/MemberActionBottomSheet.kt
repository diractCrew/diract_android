package com.baek.diract.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.baek.diract.R
import com.baek.diract.databinding.BottomsheetMemberActionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class MemberActionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetMemberActionsBinding? = null
    private val binding get() = _binding!!

    private val memberId by lazy { requireArguments().getString(ARG_ID).orEmpty() }
    private val memberName by lazy { requireArguments().getString(ARG_NAME).orEmpty() }

    private enum class LeaderState { IDLE, LOADING, SUCCESS }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetMemberActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        tvMemberName.text = memberName

        ivClose.setOnClickListener { dismiss() }

        renderLeaderState(LeaderState.IDLE)

        btnGiveLeader.setOnClickListener {
            // 1) 로딩 표시
            renderLeaderState(LeaderState.LOADING)

            // TODO: 실제 API 호출로 교체
            // 지금은 더미로 성공/실패를 번갈아 테스트 가능
            view.postDelayed({
                val success = true // 실패 테스트: false
                if (success) {
                    renderLeaderState(LeaderState.SUCCESS)
                } else {
                    // 실패: 스낵바 띄우고 원상복구
                    Snackbar.make(requireView(), "팀장 권한 주기를 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                    renderLeaderState(LeaderState.IDLE)
                }
            }, 800)
        }

        tvKick.setOnClickListener {
            // TODO: 내보내기 처리 (확인 다이얼로그 띄우는 게 보통)
            // 지금은 동작만 막고 싶으면 주석 처리해도 됨.
        }
    }

    private fun renderLeaderState(state: LeaderState) = with(binding) {
        val loading = state == LeaderState.LOADING
        val success = state == LeaderState.SUCCESS

        pbLoading.isVisible = loading
        ivSuccess.isVisible = success

        // 스샷처럼 로딩/성공일 때 다른 액션 비활성
        btnGiveLeader.isEnabled = !loading && !success
        tvKick.isEnabled = !loading && !success
        tvKick.alpha = if (!loading && !success) 1f else 0.35f
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    override fun getTheme(): Int = R.style.ThemeOverlay_Diract_BottomSheetDialog
    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"

        fun newInstance(id: String, name: String) = MemberActionBottomSheet().apply {
            arguments = bundleOf(ARG_ID to id, ARG_NAME to name)
        }
    }
}
