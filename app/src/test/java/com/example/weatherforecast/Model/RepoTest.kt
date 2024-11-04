package com.example.weatherforecast.Model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class RepoTest {

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

    private lateinit var localDataSource: FakeLocalDataSource
    private lateinit var remoteDataSource: FakeRemoteDataSource
    private lateinit var repo: Repo
    private lateinit var context: Context

    @Before
    fun setUp() {

        context = ApplicationProvider.getApplicationContext()

        localDataSource = FakeLocalDataSource()
        remoteDataSource = FakeRemoteDataSource()


        repo = Repo(
            remoteSource = remoteDataSource,
            context = context,
            localDataSource = localDataSource
        )
    }

    @Test
    fun saveWeather() = runTest {
        // Given
        val initialSize = localDataSource.getFavWeather().first().size

        // When
        repo.saveWeather(sampleWeatherInfo)

        // Then
        val updatedSize = localDataSource.getFavWeather().first().size
        assertEquals(initialSize + 1, updatedSize)
        assert(localDataSource.getFavWeather().first().contains(sampleWeatherInfo))
    }

    @Test
    fun deleteWeather() = runTest {
        // Given
        localDataSource.insert(sampleWeatherInfo)
        val initialSize = localDataSource.getFavWeather().first().size

        // When
        repo.deleteWeather(sampleWeatherInfo)

        // Then
        val updatedSize = localDataSource.getFavWeather().first().size
        assertEquals(initialSize - 1, updatedSize)
        assert(!localDataSource.getFavWeather().first().contains(sampleWeatherInfo))
    }
}
