package com.example.weatherforecast.DB

import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.Flow

class LocalDataSource (private val weatherDAO: WeatherDAO , private val alertDAO: AlertDAO)  {

    fun getFavWeather(): Flow<List<WeatherInfo>> {
        return weatherDAO.getFAvWeather()
    }

    suspend fun insert(weather: WeatherInfo): Long {
        return weatherDAO.insert(weather)
    }

    suspend fun delete(weather: WeatherInfo): Int {
        return weatherDAO.delete(weather)
    }


    fun getAllAlerts(): Flow<List<Alert>> = alertDAO.getAllAlerts()
    suspend fun insertAlert(alert: Alert): Long = alertDAO.insertAlert(alert)
    suspend fun deleteAlert(alert: Alert): Int = alertDAO.delete(alert)


}