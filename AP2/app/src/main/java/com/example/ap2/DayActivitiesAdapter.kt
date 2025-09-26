package com.example.ap2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DayActivityItem(
    val iconResId: Int,
    val title: String,
    val subtitle: String,
    val time: String
)

class DayActivitiesAdapter(
    private var items: List<DayActivityItem>
) : RecyclerView.Adapter<DayActivitiesAdapter.DayViewHolder>() {

    fun submit(newItems: List<DayActivityItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivType: ImageView = itemView.findViewById(R.id.ivType)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(item: DayActivityItem) {
            ivType.setImageResource(item.iconResId)
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvTime.text = item.time
        }
    }
}


