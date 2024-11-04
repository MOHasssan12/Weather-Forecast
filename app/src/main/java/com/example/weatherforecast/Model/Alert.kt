package com.example.weatherforecast.Model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true)val id : Int,
    val latitude: Double,
    val longitude: Double,
    val cityName : String,
    val dateTime : String
    )
