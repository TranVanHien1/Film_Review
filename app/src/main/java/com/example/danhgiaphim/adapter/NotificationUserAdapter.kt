package com.example.danhgiaphim.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.danhgiaphim.R
import com.example.danhgiaphim.data.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationUserAdapter(
    private val context: Context,
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationUserAdapter.NotifViewHolder>() {

    inner class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(notification: Notification) {
            tvTitle.text = notification.title
            tvDate.text = formatTimestamp(notification.date)
        }

        private fun formatTimestamp(timeMillis: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timeMillis))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_notification_user, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)

        holder.itemView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(notification.title)
                .setMessage(notification.content)
                .setPositiveButton("Đóng", null)
                .show()
        }
    }

    override fun getItemCount(): Int = notifications.size
}
