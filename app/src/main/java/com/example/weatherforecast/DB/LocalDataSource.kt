package com.example.weatherforecast.DB

import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.Flow

class LocalDataSource (private val weatherDAO: WeatherDAO , private val alertDAO: AlertDAO) :
    ILocalDataSource {

    override fun getFavWeather(): Flow<List<WeatherInfo>> {
        return weatherDAO.getFAvWeather()
    }

    override suspend fun insert(weather: WeatherInfo): Long {
        return weatherDAO.insert(weather)
    }

    override suspend fun delete(weather: WeatherInfo): Int {
        return weatherDAO.delete(weather)
    }


    override fun getAllAlerts(): Flow<List<Alert>> = alertDAO.getAllAlerts()

    override suspend fun insertAlert(alert: Alert): Long = alertDAO.insertAlert(alert)

    override suspend fun deleteAlert(alert: Alert): Int = alertDAO.delete(alert)


}