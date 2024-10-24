package com.example.mvvmproducts.Network

import com.example.weatherforecast.Model.ForecastInfo
import com.example.weatherforecast.Model.WeatherInfo

sealed class APIState {
    class Success(val data: WeatherInfo) : APIState()
    class SuccessForecast(val data: ForecastInfo) : APIState()
    class Failure(val msg: Throwable) : APIState()
    object Loading : APIState()
}
