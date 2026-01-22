package com.baek.diract.presentation.home.video.section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.baek.diract.R
import com.baek.diract.databinding.FragmentSectionSettingBinding
import com.baek.diract.presentation.common.CustomToast
import com.baek.diract.presentation.common.LoadingOverlay
import com.baek.diract.presentation.common.UiState
import com.baek.diract.presentation.common.dialog.BasicDialog
import com.baek.diract.presentation.common.recyclerview.SpacingItemDecoration
import com.baek.diract.presentation.common.option.OptionItem
import com.baek.diract.presentation.common.option.OptionPopup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SectionSettingFragment : Fragment() {
    private var _binding: FragmentSectionSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SectionSettingViewModel by viewModels()

    private val loadingOverlay by lazy { LoadingOverlay(this) }

    private lateinit var sectionItemAdapter: SectionItemAdapter
    private var hasShownMaxLengthToast = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSectionSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapter()
        observeViewModel()
        setupBackPressHandler()
        setupInsets()
    }

    private fun initView() {
        binding.toolbar.subtitle = viewModel.trackTitle ?: ""
        binding.toolbar.setNavigationOnClickListener {
            handleOnClickBackBtn()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    viewModel.startAddSection()
                    true
                }

                else -> false
            }
        }

        binding.addPartBtn.setOnClickListener {
            viewModel.startAddSection()
        }

        binding.confirmBtn.setOnClickListener {
            val newName = getCurrentEditingName()
            if (newName.isNotBlank()) {
                viewModel.completeEditSection(newName)
                hideKeyboard()
            }
        }
    }

    private fun setupInsets() {
        val defaultPadding = (16 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 키보드가 올라오면 키보드 높이, 아니면 기본 높이
            val bottomPadding = maxOf(imeInsets.bottom, systemBarInsets.bottom)

            binding.root.updatePadding(bottom = bottomPadding)
            insets
        }
        binding.actionContainer.doOnLayout {
            binding.rvPartList.updatePadding(
                bottom = defaultPadding + it.height
            )
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    // 현재 편집 중인 EditText의 텍스트 가져오기
    private fun getCurrentEditingName(): String {
        val editingPosition = viewModel.getEditingPosition()
        if (editingPosition == -1) return ""

        val viewHolder = binding.rvPartList.findViewHolderForAdapterPosition(editingPosition)
        if (viewHolder != null) {
            val itemView = viewHolder.itemView
            val editText = itemView.findViewById<EditText>(R.id.partEditTxt)
            return editText?.text?.toString()?.trim() ?: ""
        }
        return ""
    }

    private fun initAdapter() {
        sectionItemAdapter = SectionItemAdapter(
            onItemLongClick = ::onItemLongClick,
            onEditDone = ::onEditDone,
            onTextLengthChanged = ::onTextLengthChanged
        )

        binding.rvPartList.apply {
            adapter = sectionItemAdapter
            addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.section_item_spacing)))
        }
    }

    private fun onTextLengthChanged(length: Int) {
        binding.confirmBtn.isEnabled = length > 0

        // 10자 이상일 때 토스트 한 번만 표시
        if (length >= 10 && !hasShownMaxLengthToast) {
            hasShownMaxLengthToast = true
            CustomToast.showPositive(
                requireContext(),
                getString(R.string.toast_section_name_max_length, 10),
                Toast.LENGTH_LONG
            )
        } else if (length < 10) {
            hasShownMaxLengthToast = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sectionItems.collect { items ->
                        updateAdapter(items)
                    }
                }
                launch {
                    viewModel.uiState.collect { state ->
                        loadingOverlay.setVisible(state is UiState.Loading)
                    }
                }
                launch {
                    viewModel.editingItem.collect { item ->
                        binding.toolbar.menu.findItem(R.id.action_add).isEnabled = item == null
                        binding.actionContainer.visibility =
                            if (item == null) View.GONE else View.VISIBLE
                    }
                }
                viewModel.toastMessage.collect { message ->
                    CustomToast.showNegative(requireContext(), message, Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun updateAdapter(items: List<SectionItem>) {
        binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.rvPartList.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        sectionItemAdapter.submitList(items)
    }

    private fun onItemLongClick(item: SectionItem, view: View) {
        // 수정 중인 아이템이 있으면 다른 액션 차단
        if (viewModel.editingItem.value != null) return

        OptionPopup
            .basicOptions(requireContext(), onOptionSelected = { option ->
                if (option.id == OptionItem.ID_EDIT_NAME) {
                    viewModel.startEditSection(item)
                }
                if (option.id == OptionItem.ID_DELETE) {
                    BasicDialog.destructive(
                        requireContext(),
                        getString(R.string.dialog_delete_part_title, item.name),
                        getString(R.string.dialog_cannot_recover),
                        getString(R.string.dialog_delete),
                        onPositive = { viewModel.deleteSection(item) }
                    )
                        .show()
                }
            })
            .show(view)
    }


    // 키보드 완료 버튼 클릭 시 호출
    private fun onEditDone() {
        hideKeyboard()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        )
    }

    private fun handleBackPress() {
        if (viewModel.editingItem.value != null) {
            BasicDialog.destructive(
                context = requireContext(),
                title = getString(R.string.dialog_discard_title),
                message = getString(R.string.dialog_discard_message),
                positiveText = getString(R.string.dialog_discard),
                onPositive = {
                    viewModel.cancelEditing()
                }
            ).show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun handleOnClickBackBtn() {
        if (viewModel.editingItem.value != null) {
            BasicDialog.destructive(
                context = requireContext(),
                title = getString(R.string.dialog_discard_title),
                message = getString(R.string.dialog_discard_message),
                positiveText = getString(R.string.dialog_discard),
                onPositive = {
                    viewModel.cancelEditing()
                    findNavController().navigateUp()
                }
            ).show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
