package com.baek.diract.presentation.home.video.upload

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.baek.diract.R
import com.baek.diract.databinding.ItemGalleryVideoBinding
import com.baek.diract.presentation.common.Formatter.toTimeString

class GalleryVideoAdapter(
    private val onItemClick: (GalleryVideoItem) -> Unit
) : ListAdapter<GalleryVideoItem, GalleryVideoAdapter.GalleryVideoViewHolder>(
    GalleryVideoDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryVideoViewHolder {
        val binding = ItemGalleryVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GalleryVideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryVideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GalleryVideoViewHolder(
        private val binding: ItemGalleryVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GalleryVideoItem) {
            // 썸네일 로드
            Glide.with(binding.thumbnailImg)
                .load(item.uri)
                .placeholder(R.color.fill_normal)
                .centerCrop()
                .into(binding.thumbnailImg)

            // 영상 길이 표시
            binding.durationTxt.text = item.duration.toTimeString()
            binding.durationTxt.isSelected = item.isSelected
            // 클릭 리스너
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

    }

    private class GalleryVideoDiffCallback : DiffUtil.ItemCallback<GalleryVideoItem>() {
        override fun areItemsTheSame(
            oldItem: GalleryVideoItem,
            newItem: GalleryVideoItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: GalleryVideoItem,
            newItem: GalleryVideoItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
