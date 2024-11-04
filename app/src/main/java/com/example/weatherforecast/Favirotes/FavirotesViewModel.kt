package com.example.weatherforecast.Favirotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.Model.IRepo
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class FavirotesViewModel (private val repo: IRepo) : ViewModel() {

    private val _favoriteLocations = MutableStateFlow<APIState>(APIState.Loading)
    val favoriteLocations: StateFlow<APIState> get() = _favoriteLocations

    private val _favoriteWeatherData = MutableStateFlow<List<WeatherInfo>>(emptyList())
    val favoriteWeatherData: StateFlow<List<WeatherInfo>> get() = _favoriteWeatherData

    fun addFavoriteLocation(weatherInfo: WeatherInfo) {
        viewModelScope.launch {
            repo.saveWeather(weatherInfo)
            loadFavoriteWeather()
        }
    }

    fun deleteFavoriteLocation(weatherInfo: WeatherInfo) {
        viewModelScope.launch {
            repo.deleteWeather(weatherInfo)
            loadFavoriteWeather()
        }
    }

    fun loadFavoriteWeather() {
        viewModelScope.launch {
            repo.getFavoriteWeather().collect { weatherList ->
                _favoriteWeatherData.value = weatherList
            }
        }
    }

        fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            repo.getWeather(lat, lon).collect { state ->
                _favoriteLocations.value = state
            }
        }
    }



}


