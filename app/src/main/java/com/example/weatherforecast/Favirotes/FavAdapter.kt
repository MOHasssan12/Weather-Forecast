package com.example.weatherforecast.Favirotes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class FavAdapter :
    ListAdapter<WeatherInfo, FavAdapter.ViewHolder>(FavDiffutil()) {

    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val txtTime1: TextView = item.findViewById(R.id.txtCurrentWeatherDate)
        val city: TextView = item.findViewById(R.id.txtFavCity)
        val weatherIcon: ImageView = item.findViewById(R.id.Weather_image)
        val desc: TextView = item.findViewById(R.id.txtWeatherDesc)
        val temp: TextView = item.findViewById(R.id.txtTemperature)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.favirote, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentWeather = getItem(position)

        val unixTimestamp = currentWeather.dt
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("E, d MMM yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val formattedDate = sdf.format(date)
        holder.txtTime1.text = formattedDate

        holder.city.text = "${currentWeather.name}"
        holder.desc.text = currentWeather.weather[0].description
        holder.temp.text = "${(currentWeather.main.temp - 273.15).roundToInt()}°C"

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${currentWeather.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.weatherIcon)
    }
}
