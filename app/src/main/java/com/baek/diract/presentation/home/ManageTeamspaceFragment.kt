package com.baek.diract.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.R
import com.baek.diract.databinding.FragmentManageTeamspaceBinding
import com.baek.diract.presentation.TeamMemberUi
import com.baek.diract.presentation.common.dialog.InputDialogFragment
import com.baek.diract.presentation.common.option.OptionItem
import com.baek.diract.presentation.common.option.OptionPopup
import com.baek.diract.presentation.common.option.TeamspaceSwitcherPopup
import com.baek.diract.presentation.common.option.TeamspaceUi
import com.baek.diract.presentation.common.recyclerview.SpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageTeamspaceFragment : Fragment(R.layout.fragment_manage_teamspace) {

    private var selectedTeamspaceId: Long = 3L // 임시(처음 선택값)
    private val teamspaces = listOf(
        TeamspaceUi(1, "Diract Crew"),
        TeamspaceUi(2, "Developer Academy"),
        TeamspaceUi(3, "CTR D"),
    )
    private var _binding: FragmentManageTeamspaceBinding? = null
    private val viewModel: HomeViewModel by activityViewModels()
    private val binding get() = _binding!!
    private var switcherPopup: TeamspaceSwitcherPopup? = null
    private val memberAdapter by lazy {
        TeamMemberAdapter { anchor ->
            OptionPopup
                .basicOptions(
                    context = requireContext(),
                    onOptionSelected = { option ->
                        when (option.id) {
                            OptionItem.ID_EDIT_NAME -> {
                                // TODO 내일: 이름 수정 로직
                            }
                            OptionItem.ID_DELETE -> {
                                // TODO 내일: 삭제 로직
                            }
                        }
                    }
                )
                .show(anchor)
        }
    }
    private fun showCreateTeamspaceSheet() {
        InputDialogFragment.newInstance(
            title = getString(R.string.teamspace_create_title),
            description = getString(R.string.teamspace_create_prompt),
            hint = getString(R.string.teamspace_name_hint),
            buttonText = getString(R.string.teamspace_create_cta),
            maxLength = 20
        ).apply {
            onConfirm = { name ->
                viewModel.createTeamspace(name)
            }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }

    private fun showRenameTeamspaceDialog() {
        val currentName = binding.tvTeamspaceTitle.text?.toString().orEmpty()

        val dialog = InputDialogFragment.newInstance(
            title = getString(R.string.teamspace_rename_title),
            description = getString(R.string.teamspace_create_prompt),
            hint = null, // 현재 팀스페이스 이름이 들어가야함
            buttonText = getString(R.string.confirm),
            maxLength = 20,
            initialText = currentName
        ).apply {
            onConfirm = { newName ->
                // ✅ 일단 화면만 동작시키기(추후 ViewModel로 rename API 연결)
                binding.tvTeamspaceTitle.text = newName
                dismiss()
            }
        }

        dialog.show(parentFragmentManager, InputDialogFragment.TAG)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageTeamspaceBinding.bind(view)

        // ✅ 상단바 메뉴 클릭 처리 (여기에)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_more -> {
                    val anchor = binding.toolbar.findViewById<View>(R.id.action_more) ?: binding.toolbar

                    OptionPopup.builder(requireContext())
                        .addOptions(
                            OptionItem.renameTeamspace(),  // "팀 스페이스 이름 수정"
                            OptionItem.kickMember()        // "팀원 내보내기"(빨강)
                        )
                        .setOnOptionSelectedListener { option ->
                            when (option.id) {
                                OptionItem.ID_RENAME_TEAMSPACE -> {
                                    showRenameTeamspaceDialog()
                                }
                                OptionItem.ID_KICK_MEMBER -> {
                                    // 선택모드 진입
                                }
                            }
                        }
                        .show(anchor)

                    true
                }
                else -> false
            }
        }
        // 1) RecyclerView 세팅
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memberAdapter
        }
        memberAdapter.submitList(List(10) { "멤버 더미 ${it + 1}" })

        // 2) 당겨서 새로고침 테스트(동작만)
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }

        // 3) 더미 10개 넣기
        val fake = listOf(
            TeamMemberUi("1", "카단", isLeader = true),
            TeamMemberUi("2", "줄리엔"),
            TeamMemberUi("3", "리비"),
            TeamMemberUi("4", "제이콥"),
            TeamMemberUi("5", "파이디온"),
            TeamMemberUi("6", "벨코"),
            TeamMemberUi("7", "멤버7"),
            TeamMemberUi("8", "멤버8"),
            TeamMemberUi("9", "멤버9"),
            TeamMemberUi("10", "멤버10"),
        )
        binding.teamspaceTitleArea.setOnClickListener {
            TeamspaceSwitcherPopup(
                context = requireContext(),
                items = teamspaces,
                selectedId = selectedTeamspaceId,
                onSelect = { selected ->
                    selectedTeamspaceId = selected.id
                    binding.tvTeamspaceTitle.text = selected.name
                },
                onCreate = { showCreateTeamspaceSheet() }
            ).show(binding.teamspaceTitleArea)
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
