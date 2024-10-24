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

class NextFiveDaysAdapter() :
    ListAdapter<WeatherData, NextFiveDaysAdapter.ViewHolder>(WeatherDiffUtil()) {


    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val txtNextHourlyTemp: TextView = item.findViewById(R.id.txtNextHourlyTemp)
        val nextImage: ImageView = item.findViewById(R.id.NextImage)
        val txtNextHourlydesc: TextView = item.findViewById(R.id.txtNextHourlyDesc)
        val txtNextDateDetails: TextView = item.findViewById(R.id.txtNextDateDetails)
        val txtNextDay: TextView = item.findViewById(R.id.txtNextDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.next_5days, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentHourlyWeather = getItem(position)

        // Convert Unix timestamp to a human-readable date and time format
        val unixTimestamp = currentHourlyWeather.dt
        val date = Date(unixTimestamp * 1000L) // Convert seconds to milliseconds

        // Format to display day of the week, day of the month, and abbreviated month
        val sdfDate = SimpleDateFormat("d MMM", Locale.getDefault()) // Format like "23 Oct"
        val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault()) // Format like "Monday"
        sdfDate.timeZone = TimeZone.getDefault()
        sdfDay.timeZone = TimeZone.getDefault()

        val formattedDate = sdfDate.format(date)
        val formattedDay = sdfDay.format(date)

        // Set the formatted date, day, description, and temperature to the views
        holder.txtNextDateDetails.text = formattedDate
        holder.txtNextDay.text = formattedDay
        holder.txtNextHourlydesc.text = currentHourlyWeather.weather[0].description
        val tempCelsius = (currentHourlyWeather.main.temp - 273.15).roundToInt()
        holder.txtNextHourlyTemp.text = "$tempCelsius°C"

        // Load weather icon using Glide
        Glide.with(holder.nextImage.context)
            .load("https://openweathermap.org/img/wn/${currentHourlyWeather.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.nextImage)
    }


}
