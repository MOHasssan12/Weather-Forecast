package com.example.weatherforecast.Home.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.Model.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repo: Repo) : ViewModel() {


    private val _weatherState = MutableStateFlow<APIState>(APIState.Loading)
    val weatherState: MutableStateFlow<APIState> = _weatherState

    private val _forecastState = MutableStateFlow<APIState>(APIState.Loading)
    val forecastState: MutableStateFlow<APIState> = _forecastState


    fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            repo.getWeather(lat, lon).collect { state ->
                _weatherState.value = state
            }
        }
    }

    fun getLat(): Double {
        return repo.getlat()
    }

    fun getLong(): Double {
        return repo.getLong()
    }



    fun getWeatherForecast(lat: Double, lon: Double) {
        viewModelScope.launch {

            repo.getWeatherForecast(lat, lon).collect { state ->
                _forecastState.value = state


            }
        }
    }
}