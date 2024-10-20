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


    fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            repo.getWeather(lat, lon).collect { state ->
                _weatherState.value = state
            }
        }
    }
}