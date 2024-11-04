package com.example.weatherforecast.DB

import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {
    fun getFavWeather(): Flow<List<WeatherInfo>>

    suspend fun insert(weather: WeatherInfo): Long

    suspend fun delete(weather: WeatherInfo): Int
    fun getAllAlerts(): Flow<List<Alert>>

    suspend fun insertAlert(alert: Alert): Long

    suspend fun deleteAlert(alert: Alert): Int
}

