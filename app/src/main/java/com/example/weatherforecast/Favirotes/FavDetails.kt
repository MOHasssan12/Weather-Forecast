package com.example.weatherforecast.Favirotes

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.LocalDataSource
import com.example.weatherforecast.DB.WeatherDataBase
import com.example.weatherforecast.Home.View.NextFiveDaysAdapter
import com.example.weatherforecast.Home.View.WeatherAdapter
import com.example.weatherforecast.Home.ViewModel.WeatherViewModel
import com.example.weatherforecast.Home.ViewModel.WeatherViewModelFactory
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.R
import com.example.weatherforecast.Settings.SettingsViewModel
import com.example.weatherforecast.Settings.SettingsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
class FavDetails : AppCompatActivity() {

    lateinit var txtCurrentWeatherCity: TextView
    lateinit var txtCurrentWeatherDate: TextView
    lateinit var txtWeatherDesc: TextView
    lateinit var txtTemperature: TextView
    lateinit var txtWindSpeed: TextView
    lateinit var txtHumidaty: TextView
    lateinit var txtRain: TextView
    lateinit var txtPressure: TextView
    lateinit var weatherImage: ImageView
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var txtCloud: TextView
    lateinit var txtCurrentWeatherTime: TextView
    lateinit var txtCurrentWeatherSunsetRise: TextView

    private var temperatureUnit: String = "Kelvin"
    private var windSpeedUnit: String = "km/h"

    private lateinit var viewModel: WeatherViewModel
    private lateinit var viewModelFactory: WeatherViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fav_details)

        txtCurrentWeatherDate = findViewById(R.id.txtCurrentWeatherDate2)
        txtWeatherDesc = findViewById(R.id.txtWeatherDesc2)
        txtTemperature = findViewById(R.id.txtTemperature2)
        txtWindSpeed = findViewById(R.id.txtWindSpeed2)
        txtHumidaty = findViewById(R.id.txtHumidaty2)
        txtRain = findViewById(R.id.txtRain2)
        weatherImage = findViewById(R.id.Weather_image2)
        txtCurrentWeatherCity = findViewById(R.id.txtCurrentWeatherCity2)
        txtPressure = findViewById(R.id.txtPressure2)
        txtCurrentWeatherTime = findViewById(R.id.txtCurrentWeatherTime2)
        txtCurrentWeatherSunsetRise = findViewById(R.id.txtCurrentWeatherSunsetRise2)
        txtCloud = findViewById(R.id.txtCloud2)

        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
        val weatherDAO = WeatherDataBase.getInstance(this).getWeatherDAO()
        val alertDao = WeatherDataBase.getInstance(this).getAlertDAO()
        val localeDataSource = LocalDataSource(weatherDAO, alertDao)

        val settingsViewModelFactory = Repo.getInstance(remoteSource, this, localeDataSource)?.let {
            SettingsViewModelFactory(
                it
            )
        }
        settingsViewModel = settingsViewModelFactory?.let {
            ViewModelProvider(this,
                it
            ).get(SettingsViewModel::class.java)
        }!!

        viewModelFactory = WeatherViewModelFactory(
            Repo.getInstance(remoteSource, this, localeDataSource)
        )
        viewModel = ViewModelProvider(this, viewModelFactory).get(WeatherViewModel::class.java)

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        viewModel.getWeather(latitude, longitude)
        observeSettingsChanges()

        lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is APIState.Loading -> {
                    }
                    is APIState.Success -> {
                        val weatherInfo = state.data
                        updateWeatherInfo(weatherInfo)
                    }
                    is APIState.Failure -> {
                        Toast.makeText(
                            this@FavDetails,
                            "Error: ${state.msg.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeSettingsChanges() {
        lifecycleScope.launchWhenStarted {
            settingsViewModel.temperatureUnit.collect { unit ->
                temperatureUnit = unit
                updateUIWithNewUnits()
            }
        }
        lifecycleScope.launchWhenStarted {
            settingsViewModel.windSpeedUnit.collect { unit ->
                windSpeedUnit = unit
                updateUIWithNewUnits()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeatherInfo(weatherInfo: WeatherInfo) {
        val city = weatherInfo.name
        val country = weatherInfo.sys.country
        val unixTimestamp = weatherInfo.dt
        val date = Date(unixTimestamp * 1000L)

        val sdfDate = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
        sdfDate.timeZone = TimeZone.getDefault()
        txtCurrentWeatherCity.text = "$country, $city"
        txtCurrentWeatherDate.text = sdfDate.format(date)
        txtCurrentWeatherTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)

        val sunriseTimestamp = weatherInfo.sys.sunrise.toLong()
        val sunsetTimestamp = weatherInfo.sys.sunset.toLong()
        txtCurrentWeatherSunsetRise.text = "Sunset : ${
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
                Date(sunsetTimestamp * 1000L)
            )
        } - Sunrise : ${
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(
                Date(sunriseTimestamp * 1000L)
            )
        }"
        updateTemperatureAndWindSpeed(weatherInfo)
        txtWeatherDesc.text = weatherInfo.weather[0].description

        Glide.with(weatherImage.context)
            .load("https://openweathermap.org/img/wn/${weatherInfo.weather[0].icon}@2x.png")
            .placeholder(R.drawable.ic_launcher_background)
            .into(weatherImage)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTemperatureAndWindSpeed(weatherInfo: WeatherInfo) {
        val temp = when (temperatureUnit) {
            "Celsius" -> (weatherInfo.main.temp - 273.15).roundToInt()
            "Kelvin" -> weatherInfo.main.temp.roundToInt()
            else -> weatherInfo.main.temp.roundToInt()
        }
        val windSpeed = when (windSpeedUnit) {
            "km/h" -> (weatherInfo.wind.speed * 3.6).roundToInt()
            "mile/h" -> (weatherInfo.wind.speed * 2.23694).roundToInt()
            else -> weatherInfo.wind.speed.roundToInt()
        }

        txtTemperature.text = "$temp °${if (temperatureUnit == "Celsius") "C" else "K"}"
        txtWindSpeed.text = "$windSpeed $windSpeedUnit"
        txtHumidaty.text = "${weatherInfo.main.humidity}%"
        txtRain.text = "${weatherInfo.rain?.`1h` ?: 0} mm"
        txtPressure.text = "${weatherInfo.main.pressure} hPa"
        txtCloud.text = "${weatherInfo.clouds.all}%"
    }

    private fun updateUIWithNewUnits() {
        lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collectLatest { state ->
                if (state is APIState.Success) {
                    updateTemperatureAndWindSpeed(state.data)
                }
            }
        }
    }
}