package com.example.weatherforecast.Home.View

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentHourlyWeather = getItem(position)

        // Convert Unix timestamp to a human-readable date and time format
        val unixTimestamp = currentHourlyWeather.dt
        val date = Date(unixTimestamp * 1000L) // Convert seconds to milliseconds

        // Format to display day of the week, day of the month, and abbreviated month
        val sdf = SimpleDateFormat("E d MMM HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // Set timezone to device's default
        val formattedDateTime = sdf.format(date)

        // Set the time, description, and temperature to the views
        holder.txtTime1.text = formattedDateTime
        holder.desc.text = currentHourlyWeather.weather[0].description
        val tempCelsius = (currentHourlyWeather.main.temp - 273.15).roundToInt()
        holder.temp.text = "$tempCelsius°C"

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${currentHourlyWeather.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.weatherIcon)
    }


}
