package com.baek.diract.presentation.home.video.section

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.R
import com.baek.diract.databinding.ItemSectionBinding

class SectionItemAdapter(
    private val onItemLongClick: (SectionItem, View) -> Unit,
    private val onEditDone: () -> Unit,
    private val onTextLengthChanged: (Int) -> Unit
) : ListAdapter<SectionItem, SectionItemAdapter.SectionViewHolder>(SectionDiffCallback()) {

    companion object {
        internal const val PAYLOAD_EDITING_CHANGED = "payload_editing_changed"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = ItemSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: SectionViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_EDITING_CHANGED)) {
            // isEditing 상태만 변경된 경우
            holder.bindEditingState(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class SectionViewHolder(
        private val binding: ItemSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: SectionItem? = null
        private var textWatcher: TextWatcher? = null

        fun bind(item: SectionItem) {
            currentItem = item

            // 기본 상태 vs 수정 상태
            binding.partNameTxt.visibility = if (item.isEditing) View.GONE else View.VISIBLE
            binding.editView.visibility = if (item.isEditing) View.VISIBLE else View.GONE

            binding.partNameTxt.text = item.name

            if (item.isEditing) {
                setupEditMode(item)
            }

            // 아이템 클릭 -> 수정 모드 진입
            binding.root.setOnLongClickListener { view ->
                if (item.isEditing) return@setOnLongClickListener true

                onItemLongClick(item, view)
                true
            }
        }

        // isEditing 상태 변경 시 호출 (payload 사용)
        fun bindEditingState(item: SectionItem) {
            currentItem = item

            binding.partNameTxt.visibility = if (item.isEditing) View.GONE else View.VISIBLE
            binding.editView.visibility = if (item.isEditing) View.VISIBLE else View.GONE

            if (item.isEditing) {
                setupEditMode(item)
                // 강제로 키보드 올리기
                binding.partEditTxt.postDelayed({
                    binding.partEditTxt.requestFocus()
                    val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.partEditTxt, InputMethodManager.SHOW_IMPLICIT)
                }, 100)
            }
        }

        private fun setupEditMode(item: SectionItem) {
            // 기존 TextWatcher 제거
            textWatcher?.let { binding.partEditTxt.removeTextChangedListener(it) }

            binding.partEditTxt.setText(item.name)
            binding.partEditTxt.setSelection(item.name.length)

            // 키보드 올리기 (레이아웃 완료 후 실행)
            binding.partEditTxt.post {
                binding.partEditTxt.requestFocus()
                val imm =
                    binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.partEditTxt, InputMethodManager.SHOW_IMPLICIT)
            }

            updateCharCount(item.name.length)

            // 새 TextWatcher 설정
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val length = s?.length ?: 0
                    updateCharCount(length)
                }
            }
            binding.partEditTxt.addTextChangedListener(textWatcher)

            // 키보드 완료 버튼 클릭
            binding.partEditTxt.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onEditDone()
                    true
                } else {
                    false
                }
            }

            // 텍스트 클리어 버튼 클릭
            binding.clearBtn.setOnClickListener {
                binding.partEditTxt.text?.clear()
            }
        }

        private fun updateCharCount(length: Int) {
            binding.countTxt.text = "$length/10"
            val colorRes = when {
                length >= 10 -> R.color.accent_red_normal
                length == 0 -> R.color.label_assistive
                else -> R.color.secondary_normal
            }
            binding.countTxt.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))
            binding.viewUnderline.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    colorRes
                )
            )
            onTextLengthChanged(length)
        }
    }

    class SectionDiffCallback : DiffUtil.ItemCallback<SectionItem>() {
        override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: SectionItem, newItem: SectionItem): Any? {
            // isEditing 상태만 변경된 경우 payload 반환
            if (oldItem.name == newItem.name && oldItem.isEditing != newItem.isEditing) {
                return PAYLOAD_EDITING_CHANGED
            }
            return null
        }
    }
}
