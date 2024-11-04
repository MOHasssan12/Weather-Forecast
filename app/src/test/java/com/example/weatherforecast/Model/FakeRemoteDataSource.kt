package com.example.weatherforecast.Model

import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.IWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class FakeRemoteDataSource : IWeatherRemoteDataSource,
    com.example.weatherforecast.Newtwork.IWeatherRemoteDataSource {
    override fun getCurrentWeather(lat: Double, lon: Double): Flow<APIState> {
        TODO("Not yet implemented")
    }

    override fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        TODO("Not yet implemented")
    }

}