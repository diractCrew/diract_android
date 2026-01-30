package com.baek.diract.presentation.home

import android.os.Bundle
import com.baek.diract.presentation.common.option.OptionPopup
import com.baek.diract.presentation.common.option.OptionItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.R
import com.baek.diract.databinding.FragmentHomeBinding
import com.baek.diract.domain.model.ProjectSummary
import com.baek.diract.domain.model.SongListSummary
import com.baek.diract.presentation.common.CustomToast
import com.baek.diract.presentation.common.dialog.BasicDialog
import com.baek.diract.presentation.common.dialog.InputDialogFragment
import com.baek.diract.presentation.common.recyclerview.SpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // =========================
    // ViewBinding
    // =========================
    // Fragment 뷰 생명주기 동안만 binding을 유지하기 위해 nullable로 두고,
    // onDestroyView에서 null 처리해서 메모리 누수 방지
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // =========================
    // 테스트/더미 데이터
    // =========================
    // 곡 리스트 기능 UI 테스트용 더미 데이터
    private var testSongLists = mutableListOf(
        SongListSummary("song_1", "첫 번째 곡", 1),
        SongListSummary("song_2", "두 번째 곡", 2)
    )

    // ✅ 현재 어떤 편집 모드인지 추적
    private enum class EditMode { NONE, PROJECT, SONG }
    private var currentEditMode = EditMode.NONE

    // ✅ 곡 리스트 수정 시 정보를 저장할 변수 추가
    private var editingSongListId: String? = null

    // =========================
    // ViewModel (Hilt + viewModels)
    // =========================
    // 화면 이벤트(생성/삭제/이름변경 등)를 ViewModel로 위임하기 위한 주입
    private val viewModel: HomeViewModel by viewModels()
    private val songListsByProject = mutableMapOf<String, MutableList<SongListSummary>>()
    // =========================
    // 프로젝트 이름 편집 상태
    // =========================
    // 현재 “프로젝트 이름 편집중인” 프로젝트 id (없으면 null)
    private var editingProjectId: String? = null

    // EditText에서 사용자가 입력 중인 프로젝트 이름 draft
    private var editingDraft: String = ""

    // =========================
    // 곡 리스트 팝업 상태 (PopupWindow)
    // =========================
    // 프로젝트 카드 아래에 뜨는 곡 리스트 팝업을 참조해서 닫기/교체에 사용
    private var songPopup: android.widget.PopupWindow? = null

    // 현재 열려있는 팝업이 어떤 프로젝트의 팝업인지 추적
    private var openedProjectId: String? = null

    // =========================
    // 1) 상단바 모드 전환 로직 (정상 vs 편집)
    // =========================
    // isEditing = true면 스샷처럼 "프로젝트 목록 + 체크버튼" 상단바로 전환
    // isEditing = false면 원래 "팀스페이스명 + 설정/추가" 상단바로 복구
    private fun setTopBarEditMode(isEditing: Boolean) = with(binding) {
        if (isEditing) {
            // 편집 모드: 타이틀을 "프로젝트 목록"으로 변경
            tvCreateProjectTitle.text = getString(R.string.project_list_title) // "프로젝트 목록"

            // 편집 모드에서는 기존 상단 요소들 숨기고, 편집 전용 상단바(체크) 표시
            homeCreateProjectBar.visibility = View.GONE   // (팀명/설정 아이콘 영역) 숨김
            homeProjectToolbar.visibility = View.GONE     // (+ 추가 등) 툴바 숨김
            homeEditProjectBar.visibility = View.VISIBLE  // (체크 버튼 있는 편집 상단바) 표시
        } else {
            // 평상시 모드: 타이틀을 원래 팀 이름(리소스)으로 복구
            tvCreateProjectTitle.text = getString(R.string.home_teamspace_current_name)

            // 평상시에는 원래 상단바 보여주고 편집 상단바는 숨김
            homeCreateProjectBar.visibility = View.VISIBLE
            homeProjectToolbar.visibility = View.VISIBLE
            homeEditProjectBar.visibility = View.GONE
        }
    }

    // =========================
    // 2) "프로젝트 추가" 버튼 보임/숨김 제어
    // =========================
    // show=true면 편집중이니 +추가 버튼 숨김 (겹치거나 UX 혼란 방지)
    // show=false면 평상시니 +추가 버튼 표시
    private fun showProjectEditDone(show: Boolean) = with(binding) {
        btnAddProject.visibility = if (show) View.GONE else View.VISIBLE
    }

    // =========================
    // 3) 체크(완료) 버튼 눌렀을 때 처리
    // =========================
    // - 현재 편집중인 projectId가 있어야 함
    // - draft가 유효하면 ViewModel에 rename 요청 후 편집모드 종료
    private fun onClickEditDone() {
        val name = editingDraft.trim()
        if (!isValidProjectName(name)) return

        when (currentEditMode) {
            EditMode.PROJECT -> {
                val id = editingProjectId ?: return
                viewModel.renameProject(id, name)
            }
            EditMode.SONG -> {
                // ✅ 곡 이름 수정 완료 로직 추가
                val pId = editingProjectId ?: return
                val sId = editingSongListId ?: return

                // 테스트용 더미 리스트 갱신
                val index = testSongLists.indexOfFirst { it.id == sId }
                if (index != -1) {
                    testSongLists[index] = testSongLists[index].copy(title = name)
                    // 어댑터에 변경된 리스트 주입
                    projectAdapter.setSongLists(pId, testSongLists.toList())
                }
            }
            else -> return
        }

        // 모든 작업 완료 후 편집 모드 종료
        exitProjectEditMode()
    }
    // =========================
    // 4) 프로젝트 이름 편집모드 진입
    // =========================
    // - editingProjectId/draft 세팅
    // - Adapter를 편집모드로 전환(해당 item이 EditText로 바뀌게)
    // - +추가 버튼 숨김
    private fun enterProjectEditMode(projectId: String, currentName: String) {
        currentEditMode = EditMode.PROJECT
        editingProjectId = projectId
        editingDraft = currentName

        projectAdapter.setEditMode(projectId, currentName)
        showProjectEditDone(true)

        // ✅ 상단바를 "프로젝트 목록 + 체크버튼" 모드로 변경
        setTopBarEditMode(true)

        // 진입 시 초기 이름에 따라 체크 버튼 활성화 여부 결정
        binding.confirmBtn.isEnabled = isValidProjectName(currentName)
    }

    // (현재 코드에서는 미사용) "이름 수정"이 눌렸을 때 편집모드 진입시키는 래퍼 함수
    private fun onProjectRenameClicked(project: ProjectSummary) {
        enterProjectEditMode(project.id, project.name)
    }

    // =========================
    // 5) 프로젝트 이름 편집모드 종료
    // =========================
    // - 상태 변수 초기화
    // - Adapter 편집모드 해제
    // - +추가 버튼 다시 표시
    private fun exitProjectEditMode() {
        // ✅ 안전 장치: 이미 수정 중인 ID가 없으면 중단 (무한 루프 방지)
        if (editingProjectId == null && currentEditMode == EditMode.NONE) return

        // 1. 상태 변수 초기화
        editingProjectId = null
        editingSongListId = null
        editingDraft = ""
        currentEditMode = EditMode.NONE

        // 2. 어댑터 모드 해제 (ProjectAdapter.kt 내 notifyDataSetChanged()가 있어야 함)
        projectAdapter.clearEditMode()

        // 3. 상단바 복구
        setTopBarEditMode(false)
        showProjectEditDone(false)
    }
    // =========================
    // 6) 프로젝트 이름 유효성 검사
    // =========================
    // - 공백 제거 후 비어있지 않아야 하고
    // - 길이가 20자 이하여야 함
    private fun isValidProjectName(name: String): Boolean {
        return name.trim().isNotEmpty() && name.length <= 20
    }

    // =========================
    // RecyclerView 어댑터 (lateinit)
    // =========================
    // setupProjectRecycler()에서 초기화되므로 lateinit 사용
    private lateinit var projectAdapter: ProjectAdapter

    // =========================
    // Fragment View 생성
    // =========================
    // binding inflate 후 root 반환
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // =========================
    // View 생성 후 초기화
    // =========================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 프로젝트 리스트 RecyclerView/Adapter 세팅
        setupProjectRecycler()

        // 2) 프로젝트 더미 데이터 구성 (UI 테스트용)
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


        // 5) 팀스페이스 유무 UI 테스트 (true = 팀스페이스 있음)
        renderHasTeamspace(true)
        // 3) 화면 렌더링(테스트)
        renderProjects(emptyList())

        // 4) 특정 프로젝트(1번)에 곡 리스트 더미 주입(테스트)
        projectAdapter.setSongLists("1", testSongLists)

// ✅ 바깥(스크림) 탭하면 닫기
        binding.teamspaceTipScrim.setOnClickListener {
            showTeamspaceTipOverlay(false)
        }

// ✅ 카드 안쪽 탭은 닫히지 않게(클릭 소비)
        binding.cardTeamspaceTip.setOnClickListener {
            // 아무 것도 안 해도 됨 (소비용)
        }


// 바깥 터치 = 닫기(= 다음 단계로)
        binding.tipScrim.setOnClickListener {
            val step = viewModel.homeTipStep.value ?: 2
            advanceTipStep(step)
        }

        // 카드 클릭은 소비만(닫히지 않게)
        binding.cardProjectTip.setOnClickListener { }
        binding.cardManageTeamspaceTip.setOnClickListener { }

        // step 관찰
        viewModel.homeTipStep.observe(viewLifecycleOwner) { step ->
            renderTip(step)
        }
        // 6) 프로젝트 유무 UI 테스트


        // 7) 상단 체크버튼(완료) 클릭 -> 이름 변경 완료 처리
        binding.confirmBtn.setOnClickListener {
            // 프로젝트 편집 중이면 기존 로직
            if (editingProjectId != null) {
                onClickEditDone()
                return@setOnClickListener
            }

            // 곡 리스트 편집 중이면: 저장+종료
            val ok = projectAdapter.commitActiveSongEditAndExit()
            if (ok) {
                setTopBarEditMode(false)
            }
        }


        // 9) 프로젝트 empty 화면 클릭하면 프로젝트 생성 바텀시트 띄움
        binding.ivEmptyProject.setOnClickListener {
            showCreateProjectSheet()
        }

        // 10) +추가 버튼 클릭 -> 프로젝트 생성 바텀시트
        binding.btnAddProject.setOnClickListener {
            showCreateProjectSheet()
        }

        // 11) 상단 "팀 스페이스를 생성하세요" 영역 클릭 -> 팀스페이스 생성 바텀시트
        binding.CreateTeamspaceBar.setOnClickListener {
            showCreateTeamspaceSheet()
        }

        // 12) 팀스페이스 관리 화면으로 이동
        binding.manageTeamspace.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_manageTeamspaceFragment)
        }

        // TODO: 실제론 viewModel flow/livedata 수집해서 submitList 해야 함
    }

    // =========================
    // 곡 리스트 PopupWindow 표시
    // =========================
    // - 같은 프로젝트에서 다시 누르면 닫기(toggle)
    // - 다른 프로젝트를 누르면 기존 팝업 닫고 새 팝업 열기
    // - anchor.width로 팝업 폭을 카드 폭과 맞춤
    private fun showSongPopup(anchor: View, project: ProjectSummary) {
        // 같은 프로젝트 팝업이 이미 떠있으면 닫기
        if (songPopup?.isShowing == true && openedProjectId == project.id) {
            songPopup?.dismiss()
            return
        }

        // 기존 팝업 닫고 새로 열 준비
        songPopup?.dismiss()
        openedProjectId = project.id

        // 팝업 레이아웃 inflate
        val content = layoutInflater.inflate(R.layout.popup_song_list, null)
        val btnAdd = content.findViewById<View>(R.id.btnAddSong)

        // TODO: rv.adapter 연결 / 데이터 유무에 따라 empty 처리

        btnAdd.setOnClickListener {
            // TODO: 곡 추가 플로우
        }

        // PopupWindow 생성
        val popup = android.widget.PopupWindow(
            content,
            anchor.width,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = resources.getDimension(R.dimen.section_chip_spacing)
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setOnDismissListener { openedProjectId = null }
        }

        songPopup = popup

        // 카드 아래로 8dp 내려서 표시
        val yOff = (8 * resources.displayMetrics.density).toInt()
        popup.showAsDropDown(anchor, 0, yOff)
    }


    private fun renderTip(step: Int) = with(binding) {
        tipScrim.visibility = if (step in 0..1) View.VISIBLE else View.GONE
        cardProjectTip.visibility = if (step == 0) View.VISIBLE else View.GONE
        cardManageTeamspaceTip.visibility = if (step == 1) View.VISIBLE else View.GONE
    }

    private fun advanceTipStep(current: Int) {
        val next = (current + 1).coerceAtMost(2)
        viewModel.setHomeTipStep(next)   // 저장
        renderTip(next)
    }

    // =========================
    // 프로젝트 RecyclerView + Adapter 세팅
    // =========================
    private fun setupProjectRecycler() {
        projectAdapter = ProjectAdapter(
            // 롱클릭 -> 옵션 팝업(이름수정/삭제)
            onLongClick = { v, p -> showProjectActions(anchor = v, project = p) },

            // 일반 클릭 (필요시 확장)
            onClick = { p -> /* 필요 시 구현 */ },

            // 프로젝트 이름 EditText 변경 콜백
            // - draft를 Fragment에 저장(완료 버튼 누를 때 사용)
            onEditTextChanged = { draft: String ->
                editingDraft = draft

                // 1. 유효성 검사 (공백 제외 및 20자 이하 여부)
                val isValid = isValidProjectName(draft)

                // 2. 상단 체크 버튼(confirmBtn) 상태 제어
                binding.confirmBtn.isEnabled = isValid
                binding.confirmBtn.alpha = if (isValid) 1.0f else 0.4f

                // 3. 20자 초과 시 경고 처리 (SongListAdapter와 동일한 경험 제공)
                if (draft.length > 20) {
                    CustomToast.showNegative(
                        requireContext(),
                        getString(R.string.song_list_name_max_warning)
                    )
                }
            },

            // 곡 리스트 열기 버튼(또는 영역) 클릭 -> 팝업 표시
            onOpenSongs = { anchor, project ->
                showSongPopup(anchor, project)
            },

            // 곡 리스트 아이템 클릭 (현재는 비워둠)
            onSongListClick = { project, song -> },

            // 곡 리스트 삭제 -> 확인 다이얼로그 -> 더미 리스트에서 제거 후 갱신
            onSongListDelete = { project, song ->
                BasicDialog.destructive(
                    context = requireContext(),
                    title = getString(R.string.dialog_song_delete_title, song.title),
                    message = getString(R.string.dialog_song_cannot_recover),
                    positiveText = getString(R.string.dialog_delete),
                    onPositive = {
                        testSongLists.removeAll { it.id == song.id }
                        projectAdapter.setSongLists(project.id, testSongLists.toList())
                    }
                ).show()
            },

            // 곡 리스트 이름 수정 -> 더미 리스트 업데이트 후 갱신
            onSongListRename = { project, song, newName ->
                val index = testSongLists.indexOfFirst { it.id == song.id }
                if (index != -1) {
                    testSongLists[index] = song.copy(title = newName)
                    projectAdapter.setSongLists(project.id, testSongLists.toList())
                }
            },
            onAddSongList = { project ->
                showAddSongListSheet(project)
            },

            // 곡 리스트가 "편집 상태"로 들어가거나 나올 때 상단바 모드 전환
            onSongListEditStateChanged = { isEditing ->
                currentEditMode = if (isEditing) EditMode.SONG else EditMode.NONE
                setTopBarEditMode(isEditing)
            }
        )

        // RecyclerView 기본 설정
        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter

            // 아이템 간격 데코레이션(중복 추가 방지)
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

    // =========================
    // 팀스페이스 유무에 따른 첫 화면 토글
    // =========================
    // hasTeamspace=true면 프로젝트 화면 영역 보여주고,
    // false면 팀스페이스 생성 유도 화면 보여줌
    private fun showTeamspaceTipOverlay(show: Boolean) = with(binding) {
        teamspaceTipScrim.visibility = if (show) View.VISIBLE else View.GONE
        cardTeamspaceTip.visibility = if (show) View.VISIBLE else View.GONE
    }
    private fun renderHasTeamspace(hasTeamspace: Boolean) {
        binding.CreateTeamspaceFirstLayout.visibility = if (hasTeamspace) View.GONE else View.VISIBLE
        binding.CreateProjectFirstLayout.visibility = if (hasTeamspace) View.VISIBLE else View.GONE
        showTeamspaceTipOverlay(!hasTeamspace)
    }


    private fun showSecondTip() = with(binding) {
        cardProjectTip.visibility = View.GONE
        cardManageTeamspaceTip.visibility = View.VISIBLE
    }


    // =========================
    // 프로젝트 리스트 렌더링
    // =========================
    // projects 비었으면 empty/tip 보여주고 리스트/툴바 숨김
    // projects 있으면 리스트/툴바 보여주고 어댑터에 submit
    private fun renderProjects(projects: List<ProjectSummary>) {
        val isEmpty = projects.isEmpty()

        binding.homeProjectToolbar.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.rvProjects.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyProject.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.cardProjectTip.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (!isEmpty) projectAdapter.submitList(projects)
    }

    // =========================
    // 팀스페이스 생성 바텀시트(입력 다이얼로그)
    // =========================
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
    private fun showAddSongListSheet(project: ProjectSummary) {
        InputDialogFragment.newInstance(
            title = getString(R.string.song_add_title),              // 예: "곡 추가하기"
            description = getString(R.string.song_add_prompt),       // 예: "추가할 곡의 이름을 입력하세요"
            hint = getString(R.string.song_name_hint),               // 예: "(예시) SEVENTEEN - ..."
            buttonText = getString(R.string.song_add_cta),           // 예: "곡 추가하기"
            maxLength = 20
        ).apply {
            onConfirm = { name ->
                viewModel.createSong(name)

            }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }
    // =========================
    // 프로젝트 생성 바텀시트(입력 다이얼로그)
    // =========================
    private fun showCreateProjectSheet() {
        InputDialogFragment.newInstance(
            title = getString(R.string.project_create_title),
            description = getString(R.string.project_create_prompt),
            hint = getString(R.string.project_name_hint),
            buttonText = getString(R.string.project_create_cta),
            maxLength = 20
        ).apply {
            onConfirm = { name ->
                viewModel.createProject(name)
            }
        }.show(parentFragmentManager, InputDialogFragment.TAG)
    }

    // =========================
    // 프로젝트 옵션 팝업(이름수정/삭제) 표시
    // =========================
    private fun showProjectActions(anchor: View, project: ProjectSummary) {
        OptionPopup
            .basicOptions(
                context = requireContext(),
                onOptionSelected = { option ->
                    when (option.id) {
                        // 이름 수정 -> EditText 편집모드 진입
                        OptionItem.ID_EDIT_NAME -> enterProjectEditMode(
                            projectId = project.id,
                            currentName = project.name
                        )
                        // 삭제 -> 삭제 다이얼로그
                        OptionItem.ID_DELETE -> showDeleteDialog(project)
                    }
                }
            )
            .show(anchor)
    }


    // =========================
    // 삭제 확인 다이얼로그
    // =========================
    private fun showDeleteDialog(project: ProjectSummary) {
        BasicDialog.destructive(
            context = requireContext(),
            title = "${project.name} 프로젝트를 삭제하시겠어요?",
            message = "프로젝트 모든 내용이 삭제됩니다.",
            positiveText = "삭제",
            onPositive = { viewModel.deleteProject(project.id) }
        ).show()
    }

    // =========================
    // (안 쓰는 버전) Android 기본 PopupMenu 방식 옵션 메뉴
    // =========================
    // 현재는 OptionPopup을 사용 중이라 이 함수는 정리 대상
    private fun showProjectActionsMenu(anchor: View, project: ProjectSummary) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_project_actions, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> {
                    true
                }
                R.id.action_delete -> {
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // =========================
    // View 파괴 시 binding 해제
    // =========================
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
