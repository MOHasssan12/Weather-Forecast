package com.example.weatherforecast.Favirotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.Model.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class FavirotesViewModel (private val repo: Repo) : ViewModel() {

    private val _favoriteLocations = MutableStateFlow<APIState>(APIState.Loading)
    val favoriteLocations: StateFlow<APIState> get() = _favoriteLocations

    fun addFavoriteLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            repo.getWeather(lat, lon).collect { state ->
                _favoriteLocations.value = state
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


