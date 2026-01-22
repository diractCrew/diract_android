package com.baek.diract.presentation.home

import androidx.recyclerview.widget.RecyclerView
import com.baek.diract.databinding.ItemProjectBinding
import com.baek.diract.domain.model.ProjectSummary
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ProjectAdapter(
    private val onLongClick: (view: View, project: ProjectSummary) -> Unit,
    private val onClick: (project: ProjectSummary) -> Unit
) : ListAdapter<ProjectSummary, ProjectAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProjectSummary) = with(binding) {
            tvProjectName.text = item.name
//            tvProjectCount.text = item.count.toString()

            root.setOnClickListener { onClick(item) }
            root.setOnLongClickListener {
                onLongClick(it, item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = getItem(position)

        holder.itemView.setOnClickListener {
            onClick(project)
        }

        holder.itemView.setOnLongClickListener { v ->
            onLongClick(v, project)
            true // ✅ 롱클릭 이벤트 소비
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProjectSummary>() {
            override fun areItemsTheSame(oldItem: ProjectSummary, newItem: ProjectSummary): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProjectSummary, newItem: ProjectSummary): Boolean =
                oldItem == newItem
        }
    }
}