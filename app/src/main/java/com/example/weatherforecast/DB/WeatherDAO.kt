package com.example.weatherforecast.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.Model.WeatherInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDAO {

    @Query("SELECT * FROM Weather")
    fun getFAvWeather(): Flow<List<WeatherInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather:WeatherInfo) : Long

    @Delete
    suspend fun delete(weather: WeatherInfo): Int

}