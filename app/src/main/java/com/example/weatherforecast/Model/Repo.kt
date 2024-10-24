package com.example.weatherforecast.Model

import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Repo ( private val remoteSource: WeatherRemoteDataSource) {


    private val _productsState = MutableStateFlow<APIState>(APIState.Loading)
    val productsState: StateFlow<APIState> = _productsState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: Repo? = null

        fun getInstance( remoteDataSource: WeatherRemoteDataSource): Repo? {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Repo(remoteDataSource)
                INSTANCE
            }
        }
    }

    fun getWeather(lat : Double,  lon : Double): Flow<APIState> {
        return remoteSource.getCurrentWeather(lat, lon)
    }

    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> {
        return remoteSource.getWeatherForecast(lat, lon)
    }

}