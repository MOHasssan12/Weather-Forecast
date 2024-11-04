package com.example.weatherforecast.Favirotes

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
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class FavAdapter(
    private val onItemClickListener: (WeatherInfo) -> Unit
) : ListAdapter<WeatherInfo, FavAdapter.ViewHolder>(FavDiffutil()) {

    private var temperatureUnit: String = "Kelvin"
    private var weatherList = mutableListOf<WeatherInfo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
        notifyDataSetChanged()
    }

    override fun submitList(list: List<WeatherInfo>?) {
        weatherList = list?.toMutableList() ?: mutableListOf()
        super.submitList(list)
    }

    inner class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val city: TextView = item.findViewById(R.id.txtFavCity)
        val weatherIcon: ImageView = item.findViewById(R.id.Weather_image)
        val desc: TextView = item.findViewById(R.id.txtWeatherDesc)
        val temp: TextView = item.findViewById(R.id.txtTemperature)

        init {
            item.setOnClickListener {
                onItemClickListener.invoke(weatherList[adapterPosition])
            }
        }
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

        holder.city.text = "${currentWeather.name}"
        holder.desc.text = currentWeather.weather[0].description
        val temp = when (temperatureUnit) {
            "Celsius" -> (currentWeather.main.temp - 273.15).roundToInt()
            "Kelvin" -> currentWeather.main.temp.roundToInt()
            else -> currentWeather.main.temp.roundToInt()
        }
        holder.temp.text = "$temp °${if (temperatureUnit == "Celsius") "C" else "K"}"

        Glide.with(holder.weatherIcon.context)
            .load("https://openweathermap.org/img/wn/${currentWeather.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.weatherIcon)
    }

    fun removeItemAtPosition(position: Int): WeatherInfo? {
        return if (position in weatherList.indices) {
            val removedItem = weatherList.removeAt(position)
            submitList(weatherList.toList())
            removedItem
        } else null
    }

    fun addItemAtPosition(item: WeatherInfo, position: Int) {
        if (position <= weatherList.size) {
            weatherList.add(position, item)
            submitList(weatherList.toList())
        }
    }

    fun getWeatherAtPosition(position: Int): WeatherInfo? {
        return if (position in weatherList.indices) weatherList[position] else null
    }
}