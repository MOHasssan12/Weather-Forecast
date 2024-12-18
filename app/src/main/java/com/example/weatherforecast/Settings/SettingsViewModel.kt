package com.example.weatherforecast.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.Model.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: Repo) : ViewModel() {

    private val _language = MutableStateFlow(repo.getLanguage())
    val language: StateFlow<String> get() = _language

    private val _temperatureUnit = MutableStateFlow(repo.getTemperatureUnit())
    val temperatureUnit: StateFlow<String> get() = _temperatureUnit

    private val _windSpeedUnit = MutableStateFlow(repo.getWindSpeedUnit())
    val windSpeedUnit: StateFlow<String> get() = _windSpeedUnit

    private val _location = MutableStateFlow(repo.getLocationSource())
    val location: StateFlow<String> get() = _location

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _language.value = language
            repo.saveLanguage(language)
        }
    }
    fun updateTemperatureUnit(unit: String) {
        viewModelScope.launch {
            _temperatureUnit.value = unit
            repo.saveTemperatureUnit(unit)
        }
    }
    fun updateWindSpeedUnit(unit: String) {
        viewModelScope.launch {
            _windSpeedUnit.value = unit
            repo.saveWindSpeedUnit(unit)
        }
    }
    fun updateLocation(location: String) {
        viewModelScope.launch {
            _location.value = location
            repo.saveLocationSource(location)
        }
    }

    fun updateLat(lat: Double) {
        viewModelScope.launch {
            repo.savelat(lat)
        }
    }
    fun updateLong(long: Double) {
        viewModelScope.launch {
            repo.saveLong(long)
        }
    }

}