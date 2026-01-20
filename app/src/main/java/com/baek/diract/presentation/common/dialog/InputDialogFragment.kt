package com.baek.diract.presentation.common.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.view.isVisible
import com.baek.diract.R
import com.baek.diract.databinding.FragmentInputDialogBinding
import com.baek.diract.presentation.common.LoadingOverlay
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 입력 다이얼로그 컴포넌트
 *
 * 사용 예시:
 * ```
 * InputDialogFragment.newInstance(
 *     title = "새 프로젝트 만들기",
 *     description = "프로젝트 이름을 입력하세요.",
 *     hint = "(예시) 대동제",
 *     buttonText = "새 프로젝트 만들기",
 *     maxLength = 20
 * ).apply {
 *     onConfirm = { inputText ->
 *         // 확인 버튼 클릭 시 처리
 *     }
 * }.show(parentFragmentManager, InputDialogFragment.TAG)
 * ```
 */
class InputDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentInputDialogBinding? = null
    private val binding get() = _binding!!
    private val loadingOverlay by lazy { LoadingOverlay(this) }
    var onConfirm: ((String) -> Unit)? = null

    private val maxLength: Int
        get() = arguments?.getInt(ARG_MAX_LENGTH, DEFAULT_MAX_LENGTH) ?: DEFAULT_MAX_LENGTH

    private var isError = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                Log.d("InputDialogFragment", "backPress됨")
                handleBackPress()
                true
            } else {
                false
            }
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullScreen()
        setupViews()
        setupTextWatcher()
    }

    private fun setupFullScreen() {
        (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->
            bottomSheetDialog.setOnShowListener {
                val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
                bottomSheet?.let { sheet ->
                    val behavior = BottomSheetBehavior.from(sheet)
                    sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                    behavior.isDraggable = false
                }
            }
        }
    }


    private fun setupViews() {
        val title = arguments?.getString(ARG_TITLE) ?: ""
        val description = arguments?.getString(ARG_DESCRIPTION) ?: ""
        val initialText = arguments?.getString(ARG_INITIAL_TEXT) ?: ""
        val hint = arguments?.getString(ARG_HINT)
        val buttonText = arguments?.getString(ARG_BUTTON_TEXT) ?: getString(R.string.dialog_confirm)

        binding.titleTxt.text = title
        binding.descriptionTxt.text = description
        binding.confirmBtn.text = buttonText

        hint?.let { binding.inputTxt.hint = it }
        binding.inputTxt.filters = arrayOf(InputFilter.LengthFilter(maxLength))
        binding.inputTxt.setText(initialText)

        // 입력 필드에 포커스 및 커서를 끝으로 이동
        binding.inputTxt.requestFocus()
        binding.inputTxt.setSelection(binding.inputTxt.text?.length ?: 0)

        // 초기 상태 설정
        updateButtonState(initialText)
        updateClearButtonVisibility(initialText)
        updateCounter(initialText.length)

        binding.closeBtn.setOnClickListener {
            handleBackPress()
        }

        binding.clearBtn.setOnClickListener {
            binding.inputTxt.text?.clear()
        }

        binding.confirmBtn.setOnClickListener {
            val imm = view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

            val inputText = binding.inputTxt.text?.toString()?.trim() ?: ""
            if (inputText.isNotEmpty() && inputText.length <= maxLength) {
                onConfirm?.invoke(inputText)
                loadingOverlay.show()
            }
        }


        // EditText 포커스 변경에 따른 배경 처리
        binding.inputTxt.setOnFocusChangeListener { _, hasFocus ->
            updateInputBackground(hasFocus)
        }

        // 키보드가 내려가면 포커스 해제
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible) {
                binding.inputTxt.clearFocus()
            }
            insets
        }
    }

    private fun setupTextWatcher() {
        binding.inputTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                updateButtonState(text)
                updateClearButtonVisibility(text)
                updateCounter(text.length)
            }
        })
    }

    private fun updateButtonState(text: String) {
        binding.confirmBtn.isEnabled = text.isNotEmpty() && text.length <= maxLength
    }

    private fun updateClearButtonVisibility(text: String) {
        binding.clearBtn.isVisible = text.isNotEmpty()
    }

    private fun updateCounter(length: Int) {
        if (length > 0) {
            binding.counterTxt.isVisible = true
            if (length >= maxLength) {
                isError = true
                binding.counterTxt.text =
                    getString(R.string.input_dialog_error_max_length, maxLength)
                binding.counterTxt.setTextColor(requireContext().getColor(R.color.accent_red_normal))
            } else {
                isError = false
                binding.counterTxt.text =
                    getString(R.string.input_dialog_counter, length, maxLength)
                binding.counterTxt.setTextColor(requireContext().getColor(R.color.secondary_normal))
            }
            updateInputBackground(binding.inputTxt.hasFocus())
        } else {
            isError = false
            binding.counterTxt.isVisible = false
            updateInputBackground(binding.inputTxt.hasFocus())
        }
    }

    private fun updateInputBackground(hasFocus: Boolean) {
        val backgroundRes = when {
            isError -> R.drawable.bg_input_error
            hasFocus -> R.drawable.bg_input_focus
            else -> R.drawable.bg_input_default
        }
        binding.inputContainer.setBackgroundResource(backgroundRes)
    }

    override fun onStart() {
        super.onStart()
    }


    private fun handleBackPress() {
        BasicDialog.destructive(
            context = requireContext(),
            title = getString(R.string.dialog_discard_title),
            message = getString(R.string.dialog_discard_message),
            positiveText = getString(R.string.dialog_exit),
            onPositive = {
                dismiss()
            }
        ).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "InputDialogFragment"
        private const val DEFAULT_MAX_LENGTH = 20

        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_INITIAL_TEXT = "arg_initial_text"
        private const val ARG_HINT = "arg_hint"
        private const val ARG_BUTTON_TEXT = "arg_button_text"
        private const val ARG_MAX_LENGTH = "arg_max_length"

        fun newInstance(
            title: String,
            description: String,
            hint: String? = null,
            initialText: String = "",
            buttonText: String? = null,
            maxLength: Int = DEFAULT_MAX_LENGTH
        ): InputDialogFragment {
            return InputDialogFragment().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_DESCRIPTION to description,
                    ARG_INITIAL_TEXT to initialText,
                    ARG_HINT to hint,
                    ARG_BUTTON_TEXT to buttonText,
                    ARG_MAX_LENGTH to maxLength
                )
            }
        }
    }
}
