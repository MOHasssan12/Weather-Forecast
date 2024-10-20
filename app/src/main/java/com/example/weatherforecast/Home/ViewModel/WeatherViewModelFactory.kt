package com.example.weatherforecast.Home.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.Model.Repo

class WeatherViewModelFactory(private val repo: Repo?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return repo?.let { WeatherViewModel(it) } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


