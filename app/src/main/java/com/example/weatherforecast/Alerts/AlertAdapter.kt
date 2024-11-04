package com.example.weatherforecast.Alerts

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
import com.example.weatherforecast.Favirotes.FavAdapter
import com.example.weatherforecast.Favirotes.FavDiffutil
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class AlertAdapter : ListAdapter<Alert, AlertAdapter.ViewHolder>(AlertDiffutil()) {



    private var alertList = mutableListOf<Alert>()

    override fun submitList(list: List<Alert>?) {
        alertList = list?.toMutableList() ?: mutableListOf()
        super.submitList(list)
    }

    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {

        val city: TextView = item.findViewById(R.id.txtFavCity)
        val txtDateTime: TextView = item.findViewById(R.id.txtAlertDateTime)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentWeather = getItem(position)


        holder.city.text = "${currentWeather.cityName}"
        holder.txtDateTime.text = currentWeather.dateTime

    }

    fun removeItemAtPosition(position: Int): Alert? {
        return if (position in alertList.indices) {
            val removedItem = alertList.removeAt(position)
            submitList(alertList.toList())
            removedItem
        } else null
    }

    fun addItemAtPosition(item: Alert, position: Int) {
        if (position <= alertList.size) {
            alertList.add(position, item)
            submitList(alertList.toList())
        }
    }

    fun getWeatherAtPosition(position: Int): Alert? {
        return if (position in alertList.indices) alertList[position] else null
    }
}