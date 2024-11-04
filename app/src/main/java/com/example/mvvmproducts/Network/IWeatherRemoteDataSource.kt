package com.example.mvvmproducts.Network

import kotlinx.coroutines.flow.Flow

interface IWeatherRemoteDataSource {
    fun getCurrentWeather(lat: Double, lon: Double): Flow<APIState>
    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState>
}