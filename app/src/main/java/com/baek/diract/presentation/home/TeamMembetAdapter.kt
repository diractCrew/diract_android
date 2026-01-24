package com.baek.diract.presentation.home

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TeamMemberAdapter(
    private val onItemLongClick: (View) -> Unit
) : RecyclerView.Adapter<TeamMemberAdapter.VH>() {

    private val items = mutableListOf<String>()

    // submitList 에러 났던 거 막기용 (임시)
    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val p = (16 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        (holder.itemView as TextView).text = items[position]
        holder.itemView.setOnLongClickListener {
            onItemLongClick(it)
            true
        }
    }
    override fun getItemCount() = items.size
//    override fun getItemViewType(position: Int) = TYPE_ITEM


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)
}
