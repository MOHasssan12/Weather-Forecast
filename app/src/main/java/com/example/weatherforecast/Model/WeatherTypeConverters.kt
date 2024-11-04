package com.example.weatherforecast.Model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromCoord(coord: Coord): String {
        return gson.toJson(coord)
    }

    @TypeConverter
    fun toCoord(data: String): Coord {
        return gson.fromJson(data, Coord::class.java)
    }

    @TypeConverter
    fun fromWeatherList(weather: List<Weather>): String {
        return gson.toJson(weather)
    }

    @TypeConverter
    fun toWeatherList(data: String): List<Weather> {
        val listType = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromMain(main: Main): String {
        return gson.toJson(main)
    }

    @TypeConverter
    fun toMain(data: String): Main {
        return gson.fromJson(data, Main::class.java)
    }

    @TypeConverter
    fun fromWind(wind: Wind): String {
        return gson.toJson(wind)
    }

    @TypeConverter
    fun toWind(data: String): Wind {
        return gson.fromJson(data, Wind::class.java)
    }

    @TypeConverter
    fun fromRain(rain: Rain?): String? {
        return rain?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toRain(data: String?): Rain? {
        return data?.let { gson.fromJson(it, Rain::class.java) }
    }

    @TypeConverter
    fun fromClouds(clouds: Clouds): String {
        return gson.toJson(clouds)
    }

    @TypeConverter
    fun toClouds(data: String): Clouds {
        return gson.fromJson(data, Clouds::class.java)
    }

    @TypeConverter
    fun fromSys(sys: Sys): String {
        return gson.toJson(sys)
    }

    @TypeConverter
    fun toSys(data: String): Sys {
        return gson.fromJson(data, Sys::class.java)
    }

}