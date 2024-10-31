package com.example.weatherforecast.Model

import android.content.Context
import android.content.SharedPreferences
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Repo ( private val remoteSource: WeatherRemoteDataSource , context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: Repo? = null

        fun getInstance( remoteDataSource: WeatherRemoteDataSource ,  context: Context): Repo? {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Repo(remoteDataSource,context)
                INSTANCE
            }
        }
    }

    fun getWeather(lat : Double,  lon : Double): Flow<APIState> {
        return remoteSource.getCurrentWeather(lat, lon)
    }

    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        return remoteSource.getWeatherForecast(lat, lon)
    }

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