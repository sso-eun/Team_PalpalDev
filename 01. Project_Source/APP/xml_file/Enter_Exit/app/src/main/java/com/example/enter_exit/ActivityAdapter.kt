package com.example.enter_exit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivityAdapter(private val list: List<ActivityLog>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeText: TextView = itemView.findViewById(R.id.typeText)
        val typeIcon: ImageView = itemView.findViewById(R.id.typeIcon)
        val dateTimeText: TextView = itemView.findViewById(R.id.dateTimeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val item = list[position]
        holder.typeText.text = item.type
        holder.dateTimeText.text = "${item.date} ${item.time}"
        holder.typeIcon.setImageResource(
            if (item.type == "귀가") R.drawable.ic_door_alt else R.drawable.ic_home_entry
        )
    }

    override fun getItemCount(): Int = list.size
}
