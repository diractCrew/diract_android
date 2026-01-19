package com.baek.diract.presentation.common.option

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.databinding.ItemOptionBinding

/**
 * 옵션 메뉴 어댑터
 *
 * @param options 표시할 옵션 목록
 * @param onOptionClick 옵션 클릭 콜백
 */
class OptionAdapter(
    private val options: List<OptionItem>,
    private val onOptionClick: (OptionItem) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size

    inner class OptionViewHolder(
        private val binding: ItemOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OptionItem) {
            binding.optionText.apply {
                text = itemView.context.getString(item.titleRes)
                setTextColor(ContextCompat.getColor(context, item.textColorRes))
                setOnClickListener { onOptionClick(item) }
            }
        }
    }
}
