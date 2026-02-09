package com.v2ray.ang.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.api.SubscriptionOrderDto
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersAdapter : ListAdapter<SubscriptionOrderDto, OrdersAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val plan: TextView = itemView.findViewById(R.id.item_order_plan)
        private val status: TextView = itemView.findViewById(R.id.item_order_status)
        private val tracking: TextView = itemView.findViewById(R.id.item_order_tracking)
        private val date: TextView = itemView.findViewById(R.id.item_order_date)

        fun bind(order: SubscriptionOrderDto) {
            plan.text = order.planTypeDisplay
            status.text = order.statusDisplay
            if (!order.paymentTrackingCode.isNullOrBlank()) {
                tracking.visibility = View.VISIBLE
                tracking.text = itemView.context.getString(R.string.order_tracking_code) + ": " + order.paymentTrackingCode
            } else {
                tracking.visibility = View.GONE
            }
            date.text = formatDate(order.createdAt)
        }

        private fun formatDate(isoDate: String): String {
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                val parsed = input.parse(isoDate)
                if (parsed != null) output.format(parsed) else isoDate
            } catch (e: Exception) {
                isoDate
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SubscriptionOrderDto>() {
        override fun areItemsTheSame(old: SubscriptionOrderDto, new: SubscriptionOrderDto) = old.id == new.id
        override fun areContentsTheSame(old: SubscriptionOrderDto, new: SubscriptionOrderDto) = old == new
    }
}
