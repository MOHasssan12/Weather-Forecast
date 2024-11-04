package com.example.weatherforecast.DB

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.weatherforecast.Model.Clouds
import com.example.weatherforecast.Model.Coord
import com.example.weatherforecast.Model.Main
import com.example.weatherforecast.Model.Sys
import com.example.weatherforecast.Model.Weather
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.Model.Wind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@SmallTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WeatherDAOTest {

    private lateinit var database: WeatherDataBase
    private lateinit var weatherDao: WeatherDAO

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDataBase::class.java
        )   .allowMainThreadQueries()
            .build()

        weatherDao = database.getWeatherDAO()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertWeather_insertsWeatherSuccessfully() = runTest {
        // Given
        val weatherInfo = WeatherInfo(
            coord = Coord(139.0, 35.0),
            weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
            base = "stations",
            main = Main(294.5, 293.0, 293.0, 294.5, 1013, 53, null, null),
            visibility = 10000,
            wind = Wind(1.5, 350, null),
            rain = null,
            clouds = Clouds(1),
            dt = 1625045664,
            sys = Sys(1, 1, "JP", 1625005664, 1625055664),
            timezone = 32400,
            id = 1851632,
            name = "Shuzenji",
            cod = 200
        )

        // When
        val id = weatherDao.insert(weatherInfo)
        val retrievedWeather = weatherDao.getFAvWeather().first()

        // Then
        assertThat(retrievedWeather.size, `is`(1))
        assertThat(retrievedWeather[0], `is`(weatherInfo))
    }

    @Test
    fun deleteWeather_deletesWeatherSuccessfully() = runTest {
        // Given
        val weatherInfo = WeatherInfo(
            coord = Coord(139.0, 35.0),
            weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
            base = "stations",
            main = Main(294.5, 293.0, 293.0, 294.5, 1013, 53, null, null),
            visibility = 10000,
            wind = Wind(1.5, 350, null),
            rain = null,
            clouds = Clouds(1),
            dt = 1625045664,
            sys = Sys(1, 1, "JP", 1625005664, 1625055664),
            timezone = 32400,
            id = 1851632,
            name = "Shuzenji",
            cod = 200
        )

        weatherDao.insert(weatherInfo)

        // When
        val deleteCount = weatherDao.delete(weatherInfo)
        val retrievedWeather = weatherDao.getFAvWeather().first()

        // Then
        assertThat(deleteCount, `is`(1))
        assertThat(retrievedWeather.size, `is`(0))
    }
}