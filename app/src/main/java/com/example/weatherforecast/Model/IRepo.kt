package com.example.weatherforecast.Model

import com.example.mvvmproducts.Network.APIState
import kotlinx.coroutines.flow.Flow

interface IRepo {
    // retrofit functions
    fun getWeather(lat : Double, lon : Double): Flow<APIState>
    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState>

    // database functions for favorites
    fun getFavoriteWeather(): Flow<List<WeatherInfo>>

    suspend fun saveWeather(weather: WeatherInfo): Long

    suspend fun deleteWeather(weather: WeatherInfo): Int

    // database function for Alerts
    fun getAlertsWeather(): Flow<List<Alert>>

    suspend fun saveAlert(alert: Alert): Long

    suspend fun deleteAlert(alert: Alert): Int

    // SharedPreferences functions for settings
    fun saveLanguage(language: String)
    fun getLanguage(): String
    fun saveTemperatureUnit(unit: String)
    fun getTemperatureUnit(): String
    fun saveWindSpeedUnit(unit: String)
    fun getWindSpeedUnit(): String
    fun saveLocationSource(source: String)
    fun savelat(lat: Double)
    fun saveLong(long: Double)
    fun getlat(): Double
    fun getLong(): Double
    fun getLocationSource(): String
}