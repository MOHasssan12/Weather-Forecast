package com.example.weatherforecast.Favirotes

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherforecast.Model.WeatherInfo

class FavDiffutil : DiffUtil.ItemCallback<WeatherInfo>() {
    override fun areItemsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
    return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeatherInfo, newItem: WeatherInfo): Boolean {
        return oldItem == newItem
    }
}