package com.example.weatherforecast.Home.View

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherforecast.Model.ForecastInfo
import com.example.weatherforecast.Model.WeatherData

class WeatherDiffUtil : DiffUtil.ItemCallback<WeatherData>() {
    override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
        return oldItem.pop == newItem.pop
    }

    override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
    return oldItem == newItem
    }

}