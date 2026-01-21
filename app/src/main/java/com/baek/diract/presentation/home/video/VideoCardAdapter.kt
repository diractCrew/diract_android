package com.baek.diract.presentation.home.video

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.baek.diract.R
import com.baek.diract.databinding.ItemVideoCardBinding
import com.baek.diract.domain.model.VideoSummary
import com.baek.diract.presentation.common.Formatter.toUiString
import com.baek.diract.presentation.common.Formatter.toTimeString

class VideoCardAdapter(
    private val onItemClick: (VideoSummary) -> Unit,
    private val onMoreClick: (VideoSummary) -> Unit,
    private val onCancelClick: (VideoCardItem.Failed) -> Unit = {},
    private val onRetryClick: (VideoCardItem.Failed) -> Unit = {}
) : ListAdapter<VideoCardItem, VideoCardAdapter.VideoCardViewHolder>(VideoCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoCardViewHolder {
        val binding = ItemVideoCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoCardViewHolder(
        private val binding: ItemVideoCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var shimmerAnimator: ObjectAnimator? = null

        fun bind(item: VideoCardItem) {
            when (item) {
                is VideoCardItem.Completed -> bindCompleted(item.data)
                is VideoCardItem.Compressing -> bindCompressing(item)
                is VideoCardItem.Uploading -> bindUploading(item)
                is VideoCardItem.Failed -> bindFailed(item)
            }
        }
//
//        /** emptyInfoContainer의 깜박이는 애니메이션 시작 */
//        private fun startShimmerAnimation() {
//            stopShimmerAnimation()
//            shimmerAnimator = ObjectAnimator.ofFloat(
//                binding.emptyInfoContainer,
//                View.ALPHA,
//                1f, 0.4f
//            ).apply {
//                duration = 800
//                repeatMode = ValueAnimator.REVERSE
//                repeatCount = ValueAnimator.INFINITE
//                start()
//            }
//        }
//
//        /** 깜박이는 애니메이션 중지 */
//        private fun stopShimmerAnimation() {
//            shimmerAnimator?.cancel()
//            shimmerAnimator = null
//            binding.emptyInfoContainer.alpha = 1f
//        }

        //기본 UI
        private fun bindCompleted(data: VideoSummary) {
            //stopShimmerAnimation()

            binding.thumbnailImg.visibility = View.VISIBLE
            binding.btnMore.visibility = View.VISIBLE
            binding.stateOverlay.visibility = View.GONE
            binding.emptyInfoContainer.visibility = View.GONE
            binding.infoContainer.visibility = View.VISIBLE
            binding.failedInfoContainer.visibility = View.GONE

            binding.videoTitleTxt.text = data.title
            binding.videoDurationTxt.text = data.duration.toTimeString()
            binding.dateTxt.text = data.createdAt.toUiString()

            Glide.with(binding.thumbnailImg)
                .load(data.thumbnailUrl)
                .into(binding.thumbnailImg)

            binding.root.setOnClickListener { onItemClick(data) }
            binding.btnMore.setOnClickListener { onMoreClick(data) }
        }

        private fun bindCompressing(item: VideoCardItem.Compressing) {
            // 오버레이 표시, placeholder 하단
            binding.thumbnailImg.visibility = View.GONE
            binding.btnMore.visibility = View.GONE
            binding.stateOverlay.visibility = View.VISIBLE
            binding.stateText.visibility = View.VISIBLE
            binding.emptyInfoContainer.visibility = View.VISIBLE
            binding.infoContainer.visibility = View.GONE
            binding.failedInfoContainer.visibility = View.GONE

            //진행률 표시
            binding.progressBar.isIndeterminate = false
            binding.progressBar.progress = item.progress

            binding.centerIcon.setImageResource(R.drawable.ic_video)
            binding.stateText.visibility = View.VISIBLE
            binding.stateText.text = itemView.context.getString(R.string.compressing)

            //startShimmerAnimation()

            binding.root.setOnClickListener(null)
            binding.root.isClickable = false
        }

        private fun bindUploading(item: VideoCardItem.Uploading) {
            // 오버레이 표시, placeholder 하단
            binding.thumbnailImg.visibility = View.GONE
            binding.btnMore.visibility = View.GONE
            binding.stateOverlay.visibility = View.VISIBLE
            binding.emptyInfoContainer.visibility = View.VISIBLE
            binding.infoContainer.visibility = View.GONE
            binding.failedInfoContainer.visibility = View.GONE

            // determinate 모드로 설정 (업로드는 진행률 표시)
            binding.progressBar.isIndeterminate = false
            binding.progressBar.progress = item.progress
            binding.centerIcon.setImageResource(R.drawable.ic_arrow_upward)
            binding.stateText.visibility = View.VISIBLE
            binding.stateText.text =
                itemView.context.getString(R.string.upload_progress, item.progress)

            //startShimmerAnimation()

            binding.root.setOnClickListener(null)
            binding.root.isClickable = false
        }

        private fun bindFailed(item: VideoCardItem.Failed) {
            //stopShimmerAnimation()

            // 오버레이 표시, 실패 하단
            binding.thumbnailImg.visibility = View.GONE
            binding.btnMore.visibility = View.GONE
            binding.stateOverlay.visibility = View.VISIBLE
            binding.emptyInfoContainer.visibility = View.GONE
            binding.infoContainer.visibility = View.GONE
            binding.failedInfoContainer.visibility = View.VISIBLE

            // 실패 타입에 따른 아이콘 및 타이틀 설정
            binding.progressBar.isIndeterminate = false
            binding.progressBar.progress = 0

            when (item.type) {
                FailType.COMPRESSION -> {
                    binding.centerIcon.setImageResource(R.drawable.ic_replay_20dp)
                    binding.failedTitleTxt.text =
                        itemView.context.getString(R.string.compression_failed)
                    binding.errorMessageTxt.text = item.message
                        ?: itemView.context.getString(R.string.compression_error_message)
                    binding.btnCancel.visibility = View.VISIBLE

                    binding.centerIcon.setOnClickListener { onRetryClick(item) }
                    binding.btnCancel.setOnClickListener { onCancelClick(item) }
                }

                FailType.UPLOAD -> {
                    binding.centerIcon.setImageResource(R.drawable.ic_replay_20dp)
                    binding.failedTitleTxt.text = itemView.context.getString(R.string.upload_failed)
                    binding.errorMessageTxt.text =
                        item.message ?: itemView.context.getString(R.string.network_error_message)
                    binding.btnCancel.visibility = View.VISIBLE

                    binding.centerIcon.setOnClickListener { onRetryClick(item) }
                    binding.btnCancel.setOnClickListener { onCancelClick(item) }
                }

                FailType.EXCEEDED -> {
                    binding.centerIcon.setImageResource(R.drawable.ic_close)
                    binding.failedTitleTxt.text = itemView.context.getString(R.string.size_exceeded)
                    binding.errorMessageTxt.text =
                        itemView.context.getString(R.string.size_exceeded_message)
                    binding.btnCancel.visibility = View.GONE

                    binding.centerIcon.setOnClickListener { onCancelClick(item) }
                }
            }

            binding.stateText.visibility = View.GONE
            binding.btnCancel.setOnClickListener { onCancelClick(item) }

            binding.root.setOnClickListener(null)
            binding.root.isClickable = false
        }
    }

    private class VideoCardDiffCallback : DiffUtil.ItemCallback<VideoCardItem>() {
        override fun areItemsTheSame(oldItem: VideoCardItem, newItem: VideoCardItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VideoCardItem, newItem: VideoCardItem): Boolean {
            return oldItem == newItem
        }
    }
}
