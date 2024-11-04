package com.example.weatherforecast.Model

import android.content.Context
import android.content.SharedPreferences
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.IWeatherRemoteDataSource
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.ILocalDataSource
import com.example.weatherforecast.DB.LocalDataSource
import kotlinx.coroutines.flow.Flow


class Repo(private val remoteSource: IWeatherRemoteDataSource, context: Context, private val localDataSource: ILocalDataSource,
) : IRepo {

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
    override fun getWeather(lat : Double,  lon : Double): Flow<APIState> {
        return remoteSource.getCurrentWeather(lat, lon)
    }

    override fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        return remoteSource.getWeatherForecast(lat, lon)
    }

// database functions for favorites
override fun getFavoriteWeather(): Flow<List<WeatherInfo>> = localDataSource.getFavWeather()

    override suspend fun saveWeather(weather: WeatherInfo) = localDataSource.insert(weather)

    override suspend fun deleteWeather(weather: WeatherInfo) = localDataSource.delete(weather)


    // database function for Alerts
    override fun getAlertsWeather(): Flow<List<Alert>> = localDataSource.getAllAlerts()

    override suspend fun saveAlert(alert: Alert) = localDataSource.insertAlert(alert)

    override suspend fun deleteAlert(alert: Alert) = localDataSource.deleteAlert(alert)


    // SharedPreferences functions for settings
    override fun saveLanguage(language: String) {
        sharedPreferences.edit().putString("language", language).apply()
    }

    override fun getLanguage(): String {
        return sharedPreferences.getString("language", "English") ?: "English"
    }

    override fun saveTemperatureUnit(unit: String) {
        sharedPreferences.edit().putString("temperature_unit", unit).apply()
    }

    override fun getTemperatureUnit(): String {
        return sharedPreferences.getString("temperature_unit", "Kelvin") ?: "Kelvin"
    }

    override fun saveWindSpeedUnit(unit: String) {
        sharedPreferences.edit().putString("wind_speed_unit", unit).apply()
    }

    override fun getWindSpeedUnit(): String {
        return sharedPreferences.getString("wind_speed_unit", "km/h") ?: "km/h"
    }

    override fun saveLocationSource(source: String) {
        sharedPreferences.edit().putString("location_source", source).apply()
    }

    override fun savelat(lat: Double) {
        sharedPreferences.edit().putString("lat", lat.toString()).apply()
    }
    override fun saveLong(long:Double) {
        sharedPreferences.edit().putString("long", long.toString()).apply()
    }

    override fun getlat(): Double {
        return sharedPreferences.getString("lat", "0.0")?.toDouble() ?: 0.0
    }

    override fun getLong(): Double {
        return sharedPreferences.getString("long", "0.0")?.toDouble() ?: 0.0
    }


    override fun getLocationSource(): String {
        return sharedPreferences.getString("location_source", "GPS") ?: "GPS"
    }


}