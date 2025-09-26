package com.example.ap2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ReminderItem(
    val id: Long,
    val title: String,
    val schedule: String,
    val repeat: String
)

class ReminderAdapter(
    private var items: List<ReminderItem>,
    private val onEdit: (ReminderItem) -> Unit,
    private val onDelete: (ReminderItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    fun setItems(newItems: List<ReminderItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSchedule: TextView = itemView.findViewById(R.id.tvSchedule)
        private val tvRepeat: TextView = itemView.findViewById(R.id.tvRepeat)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: ReminderItem) {
            tvTitle.text = item.title
            tvSchedule.text = item.schedule
            tvRepeat.text = item.repeat
        }
    }
}


