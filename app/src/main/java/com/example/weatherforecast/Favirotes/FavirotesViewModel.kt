package com.example.weatherforecast.Favirotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class FavirotesViewModel (private val repo: Repo) : ViewModel() {

    private val _favoriteLocations = MutableStateFlow<APIState>(APIState.Loading)
    val favoriteLocations: StateFlow<APIState> get() = _favoriteLocations

    // New StateFlow for favorite weather data
    private val _favoriteWeatherData = MutableStateFlow<List<WeatherInfo>>(emptyList())
    val favoriteWeatherData: StateFlow<List<WeatherInfo>> get() = _favoriteWeatherData

    // Function to add weather information to favorites
    fun addFavoriteLocation(weatherInfo: WeatherInfo) {
        viewModelScope.launch {
            repo.saveWeather(weatherInfo)
            loadFavoriteWeather() // Reload favorites after adding a new location
        }
    }

    // Function to delete weather information from favorites
    fun deleteFavoriteLocation(weatherInfo: WeatherInfo) {
        viewModelScope.launch {
            repo.deleteWeather(weatherInfo)
            loadFavoriteWeather() // Reload favorites after deletion
        }
    }

    // Function to load all favorite weather data from the database
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


