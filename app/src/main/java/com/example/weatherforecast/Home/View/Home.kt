package com.example.weatherforecast.Home.View

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.example.weatherforecast.DB.WeatherDAO
import com.example.weatherforecast.DB.WeatherDataBase
import com.example.weatherforecast.Home.ViewModel.WeatherViewModel
import com.example.weatherforecast.Home.ViewModel.WeatherViewModelFactory
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.Model.WeatherData
import com.example.weatherforecast.Model.WeatherInfo
import com.example.weatherforecast.R
import com.example.weatherforecast.Settings.SettingsViewModelFactory
import com.example.weatherforecast.Settings.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class Home : Fragment() {



    lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: WeatherAdapter
    lateinit var mLayoutManager: LinearLayoutManager
    lateinit var recyclerViewForecast: RecyclerView
    lateinit var mAdapterForecast: NextFiveDaysAdapter
    lateinit var mLayoutManagerForecast: LinearLayoutManager
    lateinit var txtCurrentWeatherCity: TextView
    lateinit var txtCurrentWeatherDate: TextView
    lateinit var txtWeatherDesc: TextView
    lateinit var txtTemperature: TextView
    lateinit var txtWindSpeed: TextView
    lateinit var txtHumidaty: TextView
    lateinit var txtRain: TextView
    lateinit var txtPressure: TextView
    lateinit var weatherImage: ImageView
    lateinit var viewModel: WeatherViewModel
    lateinit var weatherViewModelFactory: WeatherViewModelFactory
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var txtCloud : TextView
    lateinit var txtCurrentWeatherTime : TextView
    lateinit var txtCurrentWeatherSunsetRise : TextView
    lateinit var progressBar: ProgressBar



    private var temperatureUnit: String = "Kelvin"
    private var windSpeedUnit: String = "km/h"
    private var locationSource: String = "Gps"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
        val WeatherDAO = WeatherDataBase.getInstance(requireContext()).getWeatherDAO()
        val alertDao = WeatherDataBase.getInstance(requireContext()).getAlertDAO()
        val localeDataSource = LocalDataSource(WeatherDAO,alertDao)

        settingsViewModel = activityViewModels<SettingsViewModel> {
            Repo.getInstance(WeatherRemoteDataSource(RetrofitHelper.service), requireContext(),localeDataSource)
                ?.let { SettingsViewModelFactory(it) }!!
        }.value

        weatherViewModelFactory = WeatherViewModelFactory(context?.let {
            Repo.getInstance(remoteSource, it , localeDataSource)
        })
        viewModel = ViewModelProvider(requireActivity(), weatherViewModelFactory).get(WeatherViewModel::class.java)


    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rec_hourly_details)
        recyclerViewForecast = view.findViewById(R.id.rec_next_days)
        txtCurrentWeatherDate = view.findViewById(R.id.txtCurrentWeatherDate)
        txtWeatherDesc = view.findViewById(R.id.txtWeatherDesc)
        txtTemperature = view.findViewById(R.id.txtTemperature)
        txtWindSpeed = view.findViewById(R.id.txtWindSpeed)
        txtHumidaty = view.findViewById(R.id.txtHumidaty)
        txtRain = view.findViewById(R.id.txtRain)
        weatherImage = view.findViewById(R.id.Weather_image)
        txtCurrentWeatherCity = view.findViewById(R.id.txtCurrentWeatherCity)
        txtPressure = view.findViewById(R.id.txtPressure)
        txtCurrentWeatherTime = view.findViewById(R.id.txtCurrentWeatherTime)
        txtCurrentWeatherSunsetRise = view.findViewById(R.id.txtCurrentWeatherSunsetRise)
        txtCloud = view.findViewById(R.id.txtCloud)
        progressBar = view.findViewById(R.id.progressBar)


        val cityName = arguments?.getString("city_name")

        observeSettingsChanges()

        getWeatherData()



        mAdapterForecast = NextFiveDaysAdapter()
        mLayoutManagerForecast = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerViewForecast.apply {
            adapter = mAdapterForecast
            layoutManager = mLayoutManagerForecast
        }

        mAdapter = WeatherAdapter()
        mLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is APIState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    is APIState.Success -> {
                        progressBar.visibility = View.GONE
                        val weatherInfo = state.data
                        updateWeatherInfo(weatherInfo)
                    }
                    is APIState.Failure -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${state.msg.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is APIState.SuccessForecast -> {
                        progressBar.visibility = View.GONE
                        val forecastInfo = state.data
                        mAdapter.submitList(filterCurrentDayData(forecastInfo.list))
                        mAdapterForecast.submitList(getNextDaysForecast(forecastInfo.list))
                    }
                    is APIState.Failure -> {
                        progressBar.visibility = View.GONE
                        Log.e("WeatherForecast", "Error fetching forecast: ${state.msg.message}")
                    }
                    APIState.Loading -> {
                        progressBar.visibility = View.VISIBLE

                    }
                    else -> {}
                }
            }
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

        lifecycleScope.launchWhenStarted {
            settingsViewModel.location.collect { source ->
                locationSource = source
                Log.d("HomeFragment", "Location source updated: $locationSource")
                getWeatherData()
            }
        }

    }

    private fun getWeatherData() {
        Log.d("HomeFragment", "Current location source: $locationSource")
        val latitude: Double
        val longitude: Double

        if (locationSource.equals("Gps", ignoreCase = true)) {
            // Retrieve GPS coordinates from arguments
            latitude = arguments?.getDouble("latitude") ?: 0.0
            longitude = arguments?.getDouble("longitude") ?: 0.0
            Log.d("HomeFragment", "Using GPS: Latitude: $latitude, Longitude: $longitude")
        } else {
            latitude = viewModel.getLat()
            longitude = viewModel.getLong()
            Log.d("HomeFragment", "Using SharedPreferences: Latitude: $latitude, Longitude: $longitude")
        }

        if (latitude != 0.0 && longitude != 0.0) {
            viewModel.getWeather(latitude, longitude)
            viewModel.getWeatherForecast(latitude, longitude)
        } else {
            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
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
        txtCurrentWeatherSunsetRise.text = "Sunset : ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sunsetTimestamp * 1000L))} - Sunrise : ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sunriseTimestamp * 1000L))}"
        updateTemperatureAndWindSpeed(weatherInfo)
        txtWeatherDesc.text = weatherInfo.weather[0].description

        //setBackgroundAccordingToTime(unixTimestamp)

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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collectLatest { state ->
                if (state is APIState.Success) {
                    updateTemperatureAndWindSpeed(state.data)

                    val forecastInfo = viewModel.forecastState.value
                    if (forecastInfo is APIState.SuccessForecast) {
                        mAdapterForecast.submitList(getNextDaysForecast(forecastInfo.data.list))
                    }
                    mAdapterForecast.setTemperatureUnit(temperatureUnit)
                    mAdapter.setTemperatureUnit(temperatureUnit)
                }
            }
        }
    }

    private fun getNextDaysForecast(listWeather: List<WeatherData>): List<WeatherData> {
        val filteredList = mutableListOf<WeatherData>()
        for (weatherData in listWeather) {
            if (weatherData.dt_txt.contains("12:00:00")) {
                filteredList.add(weatherData)
            }
        }
        return filteredList
    }

    private fun filterCurrentDayData(listWeather: List<WeatherData>): List<WeatherData> {
        val filteredList = mutableListOf<WeatherData>()
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        for (weatherData in listWeather) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weatherData.dt_txt.substring(0, 10))
            val day = Calendar.getInstance().apply { time = date!! }.get(Calendar.DAY_OF_MONTH)
            if (day == currentDay) {
                filteredList.add(weatherData)
            }
        }
        return filteredList
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setBackgroundAccordingToTime(unixTimestamp: Long) {
        val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(unixTimestamp * 1000L)).toInt()
        val backgroundDrawable: Drawable? = if (hour in 6..18) {
            ContextCompat.getDrawable(requireContext(), R.drawable.sunny_bg)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.night_bg)
        }
        view?.background = backgroundDrawable
    }
}