package com.example.weatherforecast.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.Model.WeatherTypeConverters

@Database(entities = [WeatherInfo::class, Alert::class], version = 2)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDataBase  : RoomDatabase() {
    abstract fun getWeatherDAO(): WeatherDAO
    abstract fun getAlertDAO(): AlertDAO
    companion object {
        @Volatile
        private var INSTANCE: WeatherDataBase? = null
        fun getInstance(ctx: Context): WeatherDataBase {
            return INSTANCE ?: synchronized(this) {
                val temp = Room.databaseBuilder(
                    ctx.applicationContext,
                    WeatherDataBase::class.java, "weather_database"
                ) .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = temp
                temp
            }
        }
    }
}