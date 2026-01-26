package com.baek.diract.presentation.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.R
import com.baek.diract.databinding.FragmentManageTeamspaceBinding

import com.baek.diract.presentation.common.dialog.InputDialogFragment
import com.baek.diract.presentation.common.option.OptionItem
import com.baek.diract.presentation.common.option.OptionPopup
import com.baek.diract.presentation.common.option.TeamspaceSwitcherPopup
import com.baek.diract.presentation.common.option.TeamspaceUi
import com.baek.diract.presentation.common.recyclerview.SpacingItemDecoration
import com.google.android.material.divider.MaterialDividerItemDecoration
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
        TeamMemberAdapter { anchor, member ->
            MemberActionBottomSheet
                .newInstance(member.id, member.name)
                .show(parentFragmentManager, "member_actions")
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
    private fun renderMembers(list: List<TeamMemberUi>) {
        val sorted = list.sortedByDescending { it.isLeader }
        memberAdapter.submitList(sorted)

        val showEmpty = (sorted.size == 1 && sorted.first().isLeader)

        binding.emptyMember.isVisible = showEmpty
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
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
    private var dividerAdded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageTeamspaceBinding.bind(view)
        binding.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMembers.adapter = memberAdapter

        if (!dividerAdded) {
            val divider = MaterialDividerItemDecoration(requireContext(), RecyclerView.VERTICAL).apply {
                setDividerColor(ContextCompat.getColor(requireContext(), R.color.stroke_strong))
                setDividerThickness(dp(1))

                // ✅ 피그마처럼 좌우 여백 주기 (값은 너 리스트 좌우 padding과 맞춰)
                setDividerInsetStart(dp(24))
                setDividerInsetEnd(dp(24))

                isLastItemDecorated = false
            }
            binding.rvMembers.addItemDecoration(divider)
            dividerAdded = true
        }


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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()   // 또는 popBackStack()
        }
        // 1) RecyclerView 세팅
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memberAdapter
        }

        // 2) 당겨서 새로고침 테스트(동작만)
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }

        // 3) 더미 10개 넣기
        val fake = listOf(
            TeamMemberUi("1", "카단", isLeader = true),
            TeamMemberUi("2", "줄리엔", isLeader = false),
            TeamMemberUi("3", "리비", isLeader = false),
            TeamMemberUi("4", "제이콥", isLeader = false),
            TeamMemberUi("5", "파이디온", isLeader = false),
            TeamMemberUi("6", "벨코", isLeader = false),
            TeamMemberUi("7", "멤버7", isLeader = false),
            TeamMemberUi("8", "멤버8", isLeader = false),
            TeamMemberUi("9", "멤버9", isLeader = false),
            TeamMemberUi("10", "멤버10", isLeader = false),
        )

// 팀장 맨 위 고정해서 넣기
        memberAdapter.submitList(fake.sortedByDescending { it.isLeader })
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
