package com.example.weatherforecast.Newtwork

import com.example.mvvmproducts.Network.APIState
import kotlinx.coroutines.flow.Flow

interface IWeatherRemoteDataSource {
    fun getCurrentWeather(lat: Double, lon: Double): Flow<APIState>
    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState>
}