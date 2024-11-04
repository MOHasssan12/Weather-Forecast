package com.example.weatherforecast.Model

import com.example.weatherforecast.DB.ILocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class FakeLocalDataSource : ILocalDataSource {

    private val weatherDataList = mutableListOf<WeatherInfo>()

    override fun getFavWeather(): Flow<List<WeatherInfo>> = flow {
        emit(weatherDataList.toList())
    }

    override suspend fun insert(weather: WeatherInfo): Long {
        weatherDataList.add(weather)
        return 1L
    }

    override suspend fun delete(weather: WeatherInfo): Int {
        val removed = weatherDataList.remove(weather)
        return if (removed) 1 else 0
    }

    override fun getAllAlerts(): Flow<List<Alert>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlert(alert: Alert): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlert(alert: Alert): Int {
        TODO("Not yet implemented")
    }
}