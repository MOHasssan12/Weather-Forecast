package com.example.weatherforecast.Favirotes

import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.IRepo
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRepo : IRepo {

    private val favoriteWeatherData = mutableListOf<WeatherInfo>()
    private val _favoriteWeatherFlow = MutableStateFlow<List<WeatherInfo>>(emptyList())

    override fun getWeather(lat: Double, lon: Double): Flow<APIState> {
        TODO("Not yet implemented")
    }

    override fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        TODO("Not yet implemented")
    }

    override fun getFavoriteWeather(): Flow<List<WeatherInfo>> = _favoriteWeatherFlow

    override suspend fun saveWeather(weather: WeatherInfo): Long {
        favoriteWeatherData.add(weather)
        _favoriteWeatherFlow.value = favoriteWeatherData.toList()
        return 1L
    }

    override suspend fun deleteWeather(weather: WeatherInfo): Int {
        favoriteWeatherData.remove(weather)
        _favoriteWeatherFlow.value = favoriteWeatherData.toList()
        return 1
    }


    override fun getAlertsWeather(): Flow<List<Alert>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveAlert(alert: Alert): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlert(alert: Alert): Int {
        TODO("Not yet implemented")
    }

    override fun saveLanguage(language: String) {
        TODO("Not yet implemented")
    }

    override fun getLanguage(): String {
        TODO("Not yet implemented")
    }

    override fun saveTemperatureUnit(unit: String) {
        TODO("Not yet implemented")
    }

    override fun getTemperatureUnit(): String {
        TODO("Not yet implemented")
    }

    override fun saveWindSpeedUnit(unit: String) {
        TODO("Not yet implemented")
    }

    override fun getWindSpeedUnit(): String {
        TODO("Not yet implemented")
    }

    override fun saveLocationSource(source: String) {
        TODO("Not yet implemented")
    }

    override fun savelat(lat: Double) {
        TODO("Not yet implemented")
    }

    override fun saveLong(long: Double) {
        TODO("Not yet implemented")
    }

    override fun getlat(): Double {
        TODO("Not yet implemented")
    }

    override fun getLong(): Double {
        TODO("Not yet implemented")
    }

    override fun getLocationSource(): String {
        TODO("Not yet implemented")
    }
}