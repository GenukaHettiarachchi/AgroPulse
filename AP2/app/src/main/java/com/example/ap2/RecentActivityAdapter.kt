package com.example.ap2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class RecentActivityItem(
    val iconResId: Int,
    val title: String,
    val subtitle: String,
    val timeAgo: String
)

class RecentActivityAdapter(
    private var items: List<RecentActivityItem>
) : RecyclerView.Adapter<RecentActivityAdapter.RecentViewHolder>() {

    fun submit(newItems: List<RecentActivityItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return RecentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivType: ImageView = itemView.findViewById(R.id.ivType)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)

        fun bind(item: RecentActivityItem) {
            ivType.setImageResource(item.iconResId)
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvTimeAgo.text = item.timeAgo
        }
    }
}


