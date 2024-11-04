package com.example.weatherforecast.Model

import android.content.Context
import android.content.SharedPreferences
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.LocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Repo ( private val remoteSource: WeatherRemoteDataSource , context: Context ,    private val localDataSource: LocalDataSource,
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: Repo? = null

        fun getInstance( remoteDataSource: WeatherRemoteDataSource ,  context: Context , localDataSource: LocalDataSource): Repo? {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Repo(remoteDataSource,context , localDataSource)
                INSTANCE
            }
        }
    }

    // retrofit functions
    fun getWeather(lat : Double,  lon : Double): Flow<APIState> {
        return remoteSource.getCurrentWeather(lat, lon)
    }

    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        return remoteSource.getWeatherForecast(lat, lon)
    }

// database functions for favorites
    fun getFavoriteWeather(): Flow<List<WeatherInfo>> = localDataSource.getFavWeather()

    suspend fun saveWeather(weather: WeatherInfo) = localDataSource.insert(weather)

    suspend fun deleteWeather(weather: WeatherInfo) = localDataSource.delete(weather)


    // database function for Alerts
    fun getAlertsWeather(): Flow<List<Alert>> = localDataSource.getAllAlerts()

    suspend fun saveAlert(alert: Alert) = localDataSource.insertAlert(alert)

    suspend fun deleteAlert(alert: Alert) = localDataSource.deleteAlert(alert)


    // SharedPreferences functions for settings
    fun saveLanguage(language: String) {
        sharedPreferences.edit().putString("language", language).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("language", "English") ?: "English"
    }

    fun saveTemperatureUnit(unit: String) {
        sharedPreferences.edit().putString("temperature_unit", unit).apply()
    }

    fun getTemperatureUnit(): String {
        return sharedPreferences.getString("temperature_unit", "Kelvin") ?: "Kelvin"
    }

    fun saveWindSpeedUnit(unit: String) {
        sharedPreferences.edit().putString("wind_speed_unit", unit).apply()
    }

    fun getWindSpeedUnit(): String {
        return sharedPreferences.getString("wind_speed_unit", "km/h") ?: "km/h"
    }

    fun saveLocationSource(source: String) {
        sharedPreferences.edit().putString("location_source", source).apply()
    }

    fun savelat(lat: Double) {
        sharedPreferences.edit().putString("lat", lat.toString()).apply()
    }
    fun saveLong(long:Double) {
        sharedPreferences.edit().putString("long", long.toString()).apply()
    }

    fun getlat(): Double {
        return sharedPreferences.getString("lat", "0.0")?.toDouble() ?: 0.0
    }

    fun getLong(): Double {
        return sharedPreferences.getString("long", "0.0")?.toDouble() ?: 0.0
    }


    fun getLocationSource(): String {
        return sharedPreferences.getString("location_source", "GPS") ?: "GPS"
    }


}