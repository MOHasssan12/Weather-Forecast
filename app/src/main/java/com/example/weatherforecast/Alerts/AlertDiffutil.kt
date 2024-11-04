package com.example.weatherforecast.Alerts

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherforecast.Model.Alert

class AlertDiffutil : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem == newItem
    }
}