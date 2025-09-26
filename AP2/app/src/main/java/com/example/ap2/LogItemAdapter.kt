package com.example.ap2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class LogItem(
    val iconResId: Int,
    val title: String,
    val description: String,
    val date: String,
    val savedAt: Long,
    val srcDateKey: String,
    val timeLabel: String
)

class LogItemAdapter(
    private var items: List<LogItem>,
    private val onDelete: (LogItem) -> Unit
) : RecyclerView.Adapter<LogItemAdapter.LogViewHolder>() {

    private var filteredItems: List<LogItem> = items

    fun setItems(newItems: List<LogItem>) {
        items = newItems
        filteredItems = newItems
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items
        } else {
            items.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(filteredItems[position], onDelete)
    }

    override fun getItemCount(): Int = filteredItems.size

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnDelete: View? = itemView.findViewById(R.id.btnDelete)

        fun bind(item: LogItem, onDelete: (LogItem) -> Unit) {
            ivIcon.setImageResource(item.iconResId)
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvDate.text = item.date
            btnDelete?.setOnClickListener { onDelete(item) }
        }
    }
}
