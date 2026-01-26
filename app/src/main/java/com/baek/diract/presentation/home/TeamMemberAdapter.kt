package com.baek.diract.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.databinding.ItemTeammemberBinding


data class TeamMemberUi(
    val id: String,
    val name: String,
    val isLeader: Boolean
)

class TeamMemberAdapter(
    private val onMoreClick: (anchor: View, member: TeamMemberUi) -> Unit
) : RecyclerView.Adapter<TeamMemberAdapter.VH>() {

    private val items = mutableListOf<TeamMemberUi>()

    fun submitList(list: List<TeamMemberUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTeammemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding, onMoreClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class VH(
        private val binding: ItemTeammemberBinding,
        private val onMoreClick: (View, TeamMemberUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeamMemberUi) = with(binding) {
            tvName.text = item.name
            ivLeader.isVisible = item.isLeader

            // row는 나열만
            root.setOnClickListener(null)
            root.setOnLongClickListener(null)

            // 점3개 클릭 -> anchor(점3개 뷰) + 멤버 전달
            ivMore.setOnClickListener { onMoreClick(ivMore, item) }
        }
    }
}
