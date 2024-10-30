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
    private val settingsViewModel: SettingsViewModel by activityViewModels {
        Repo.getInstance(WeatherRemoteDataSource(RetrofitHelper.service), requireContext())
            ?.let { SettingsViewModelFactory(it) }!!
    }
    private var temperatureUnit: String = "Kelvin"
    private var windSpeedUnit: String = "km/h"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)

        weatherViewModelFactory = WeatherViewModelFactory(context?.let {
            Repo.getInstance(remoteSource, it)
        })
        viewModel = ViewModelProvider(requireActivity(), weatherViewModelFactory).get(WeatherViewModel::class.java)

//        // Set up ViewModel with Repository
//        val repo = Repo.getInstance(remoteSource, requireContext().applicationContext)
//        settingsViewModel = ViewModelProvider(
//            this,
//            SettingsViewModelFactory(repo)
//        ).get(SettingsViewModel::class.java)

        // Observe temperature and wind speed units
        observeSettingsChanges()
    }

    private fun observeSettingsChanges() {
        lifecycleScope.launchWhenStarted {
            // Observe temperature unit changes
            settingsViewModel.temperatureUnit.collect { unit ->
                temperatureUnit = unit
                updateUIWithNewUnits()
            }
        }

        lifecycleScope.launchWhenStarted {
            // Observe wind speed unit changes
            settingsViewModel.windSpeedUnit.collect { unit ->
                windSpeedUnit = unit
                updateUIWithNewUnits()
            }
        }
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
        val cityName = arguments?.getString("city_name")

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

        // Collect weather state
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is APIState.Loading -> {
                        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                    }
                    is APIState.Success -> {
                        val weatherInfo = state.data
                        updateWeatherInfo(weatherInfo)
                    }
                    is APIState.Failure -> {
                        Toast.makeText(requireContext(), "Error: ${state.msg.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        // Collect forecast state
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is APIState.SuccessForecast -> {
                        val forecastInfo = state.data
                        mAdapter.submitList(filterCurrentDayData(forecastInfo.list))
                        mAdapterForecast.submitList(getNextDaysForecast(forecastInfo.list))
                    }
                    is APIState.Failure -> {
                        Log.e("WeatherForecast", "Error fetching forecast: ${state.msg.message}")
                    }
                    APIState.Loading -> {
                        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        Log.d("HomeFragment", "Latitude: $latitude, Longitude: $longitude, City: $cityName")

        if (latitude != null && longitude != null) {
            viewModel.getWeather(latitude, longitude)
            viewModel.getWeatherForecast(latitude, longitude)
        }
    }

    private fun updateWeatherInfo(weatherInfo: WeatherInfo) {
        val city = weatherInfo.name
        val country = weatherInfo.sys.country
        val unixTimestamp = weatherInfo.dt
        val date = Date(unixTimestamp * 1000L)

        val sdfDate = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
        sdfDate.timeZone = TimeZone.getDefault()
        txtCurrentWeatherCity.text = "$country, $city"
        txtCurrentWeatherDate.text = sdfDate.format(date)

        updateTemperatureAndWindSpeed(weatherInfo)
        txtWeatherDesc.text = weatherInfo.weather[0].description

        setBackgroundAccordingToTime(unixTimestamp)

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
    }

    private fun updateUIWithNewUnits() {
        // The temperature unit changes can be observed using the existing state without collecting again
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collectLatest { state ->
                if (state is APIState.Success) {
                    updateTemperatureAndWindSpeed(state.data)

                    // Update the forecast UI if needed
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
        view?.background = backgroundDrawable // Use `background` instead of `setBackgroundDrawable`
    }
}
