package com.example.mvvmproducts.Network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRemoteDataSource(private val apiService: APIservice) {

    private val apiKey = "39db03c72297ad6c8b1d7f4980b9a8c6"

    fun getCurrentWeather(lat : Double,  lon : Double): Flow<APIState> = flow {
        emit(APIState.Loading)
        try {
            val response = apiService.getCurrentWeather(lat, lon, apiKey)
            emit(APIState.Success(response))
        } catch (e: Exception) {
            emit(APIState.Failure(e))
        }
    }

    fun getWeatherForecast(lat: Double, lon: Double): Flow<APIState> = flow {
        emit(APIState.Loading)
        try {
            val response = apiService.getWeatherForecast(lat, lon, apiKey)
            emit(APIState.SuccessForecast(response))
        } catch (e: Exception) {
            emit(APIState.Failure(e))
        }
    }
    }


