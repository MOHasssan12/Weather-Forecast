package com.example.weatherforecast.Favirotes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherforecast.Model.Clouds
import com.example.weatherforecast.Model.Coord
import com.example.weatherforecast.Model.Main
import com.example.weatherforecast.Model.Sys
import com.example.weatherforecast.Model.Weather
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.Model.Wind
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FavirotesViewModelTest {

    private val sampleWeatherInfo = WeatherInfo(
        coord = Coord(35.0, 139.0),
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


    @Test
    fun loadFavoriteWeather() = runTest {

        //Given
        val fakeRepo = FakeRepo()
        fakeRepo.saveWeather(sampleWeatherInfo)
        val viewModel = FavirotesViewModel(fakeRepo)

        //When
        viewModel.loadFavoriteWeather()

        //Then
        val favoriteWeather = viewModel.favoriteWeatherData.value
        assertThat(favoriteWeather, not(nullValue()))
        assertThat(favoriteWeather.size, `is`(1))
        assertThat(favoriteWeather[0], `is`(sampleWeatherInfo))

    }


    @Test
    fun addFavoriteLocation() {

        //Given
        val fakeRepo = FakeRepo()
        val viewModel = FavirotesViewModel(fakeRepo)

        //When
        viewModel.addFavoriteLocation(sampleWeatherInfo)

        //Then
        val favoriteWeather = viewModel.favoriteWeatherData.value
        assertThat(favoriteWeather, not(nullValue()))
        assertThat(favoriteWeather.size, `is`(1))
        assertThat(favoriteWeather[0], `is`(sampleWeatherInfo))
    }
}