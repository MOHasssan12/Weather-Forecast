package com.example.weatherforecast.Favirotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.Home.ViewModel.WeatherViewModel
import com.example.weatherforecast.Model.Repo

class FavirotesViewModelFactory (private val repo: Repo?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavirotesViewModel::class.java)) {
            return repo?.let { FavirotesViewModel(it) } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}