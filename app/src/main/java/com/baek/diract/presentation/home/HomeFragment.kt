package com.baek.diract.presentation.home

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.R
import com.baek.diract.databinding.FragmentHomeBinding
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.baek.diract.domain.model.ProjectSummary
import com.baek.diract.presentation.common.dialog.BasicDialog
import com.baek.diract.presentation.common.dialog.InputDialogFragment
import com.baek.diract.presentation.common.recyclerview.SpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProjectRecycler()
        val fakeProjects = listOf(
            ProjectSummary(
                id = "1",
                name = "테스트 프로젝트 1",
                createdAt = LocalDate.of(2026, 1, 22),
                creatorId = "tester",
                teamspaceId = "teamspace_1",
            ),
            ProjectSummary(
                id = "2",
                name = "테스트 프로젝트 2",
                createdAt = LocalDate.of(2026, 1, 22),
                creatorId = "tester",
                teamspaceId = "teamspace_1",
            )
        )
        renderHasTeamspace(true) //우선 팀스페이스가 없음으로 테스트용
        renderProjects(fakeProjects)//프로젝트 여부 테스트용



        binding.ivEmpty.setOnClickListener {
            showCreateTeamspaceSheet()
        }

        binding.ivEmptyProject.setOnClickListener {
            showCreateProjectSheet()
        }

        binding.btnAddProject.setOnClickListener {
            showCreateProjectSheet()
        }

        // 상단 “팀 스페이스를 생성하세요 >” 영역도 같이 열고 싶으면
        binding.CreateTeamspaceBar.setOnClickListener {
            showCreateTeamspaceSheet()
        }
        projectAdapter = ProjectAdapter(
            onLongClick = { anchor, project -> showProjectActions(anchor, project) },
            onClick = { project ->
                // TODO: 상세 이동
            }
        )



        // TODO: viewModel 프로젝트 리스트 수집해서 submitList
        // viewModel.projects.collect { projectAdapter.submitList(it) }
    }

    private fun setupProjectRecycler() {
        projectAdapter = ProjectAdapter(
            onLongClick = { anchor, project -> showProjectActions(anchor, project) },
            onClick = { project -> /* TODO */ }
        )

        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter
            // ✅ 아이템 사이 간격 (중복 추가 방지)
            if (itemDecorationCount == 0) {
                addItemDecoration(
                    SpacingItemDecoration(
                        spacing = resources.getDimensionPixelSize(R.dimen.spacing_12),
                        orientation = RecyclerView.VERTICAL,
                        includeEdge = false
                    )
                )
            }
        }
    }
    private fun renderHasTeamspace(hasTeamspace: Boolean) {
        binding.CreateTeamspaceFirstLayout.visibility = if (hasTeamspace) View.GONE else View.VISIBLE
        binding.CreateProjectFirstLayout.visibility = if (hasTeamspace) View.VISIBLE else View.GONE
    }
    private fun showFirstTip() = with(binding) {
        cardProjectTip.visibility = View.VISIBLE
        cardManageTeamspaceTip.visibility = View.GONE
    }

    private fun showSecondTip() = with(binding) {
        cardProjectTip.visibility = View.GONE
        cardManageTeamspaceTip.visibility = View.VISIBLE
    }

    private fun setupTipToggle() = with(binding) {
        // 1) 첫 안내 카드 자체를 누르거나
        cardProjectTip.setOnClickListener { showSecondTip() }

        // 2) 혹은 빈 영역(예: 프로젝트 없음 화면 전체)을 누르면 바뀌게
        //emptyStateRoot.setOnClickListener { showSecondTip() }*/***********
    }
    private fun renderProjects(projects: List<ProjectSummary>) {
        val isEmpty = projects.isEmpty()

        // 스샷처럼 "프로젝트 없음"이면 툴바/리스트 숨기고, empty + tip 보여주기
        binding.homeProjectToolbar.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.rvProjects.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyProject.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.cardProjectTip.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (!isEmpty) projectAdapter.submitList(projects)
    }
    private fun showCreateTeamspaceSheet() {
        InputDialogFragment.newInstance(
            title = getString(R.string.teamspace_create_title),          // "팀 스페이스 만들기"
            description = getString(R.string.teamspace_create_prompt),    // "팀 스페이스 이름을 입력하세요."
            hint = getString(R.string.teamspace_name_hint),           // "(예시) ..."
            buttonText = getString(R.string.teamspace_create_cta),   // "팀 스페이스 만들기"
            maxLength = 20
        ).apply {
            onConfirm = { name ->
                viewModel.createTeamspace(name)
            }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }
    private fun showCreateProjectSheet() {
        InputDialogFragment.newInstance(
            title = getString(R.string.project_create_title),
            description = getString(R.string.project_create_prompt),
            hint = getString(R.string.project_name_hint),
            buttonText = getString(R.string.project_create_cta),
            maxLength = 20
        ).apply {
            onConfirm = { name ->
                viewModel.createTeamspace(name)
            }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }
    private fun showProjectActions(anchor: View, project: ProjectSummary) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_project_actions, menu)
            menu.findItem(R.id.action_delete)?.let { item ->
                val s = SpannableString(item.title)
                s.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accent_red_normal)),
                    0, s.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                item.title = s
            }

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_rename -> { showRenameDialog(project); true }
                    R.id.action_delete -> { showDeleteDialog(project); true }
                    else -> false
                }
            }
        }.show()
    }


    private fun showRenameDialog(project: ProjectSummary) {
        InputDialogFragment.newInstance(
            title = "이름 수정",
            description = "프로젝트 이름을 입력하세요.",
            hint = project.name,
            buttonText = "저장",
            maxLength = 20
        ).apply {
            onConfirm = { newName -> viewModel.renameProject(project.id, newName) }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }

    private fun showDeleteDialog(project: ProjectSummary) {
        BasicDialog.destructive(
            context = requireContext(),
            title = "${project.name} 프로젝트를 삭제하시겠어요?",
            message = "프로젝트 모든 내용이 삭제됩니다.",
            positiveText = "삭제",
            onPositive = { viewModel.deleteProject(project.id) }
        ).show()
    }

    private fun showProjectActionsMenu(anchor: View, project: ProjectSummary) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_project_actions, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> {
                    // TODO: 이름 수정 처리
                    true
                }
                R.id.action_delete -> {
                    // TODO: 삭제 확인 다이얼로그 띄우기
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}