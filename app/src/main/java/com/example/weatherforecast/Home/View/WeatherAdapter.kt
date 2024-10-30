package com.example.weatherforecast.Home.View

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecast.Model.WeatherData

import com.example.weatherforecast.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class WeatherAdapter() :
    ListAdapter<WeatherData, WeatherAdapter.ViewHolder>(WeatherDiffUtil()) {

    private var temperatureUnit: String = "Kelvin"

    @SuppressLint("NotifyDataSetChanged")
    fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
        notifyDataSetChanged() // Notify adapter to refresh the displayed data
    }

    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val txtTime1: TextView = item.findViewById(R.id.txtTime1)
        val weatherIcon: ImageView = item.findViewById(R.id.NextImage)
        val desc: TextView = item.findViewById(R.id.txtNextHourlyDesc)
        val temp: TextView = item.findViewById(R.id.txtNextHourlyTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.hourly_details, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentHourlyWeather = getItem(position)

        val unixTimestamp = currentHourlyWeather.dt
        val date = Date(unixTimestamp * 1000L)

        val sdf = SimpleDateFormat("E d MMM HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val formattedDateTime = sdf.format(date)

        holder.txtTime1.text = formattedDateTime
        holder.desc.text = currentHourlyWeather.weather[0].description
        val temp = when (temperatureUnit) {
            "Celsius" -> (currentHourlyWeather.main.temp - 273.15).roundToInt()
            "Kelvin" -> currentHourlyWeather.main.temp.roundToInt()
            else -> currentHourlyWeather.main.temp.roundToInt()
        }
        holder.temp.text = "$temp °${if (temperatureUnit == "Celsius") "C" else "K"}"

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${currentHourlyWeather.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.weatherIcon)
    }


}
