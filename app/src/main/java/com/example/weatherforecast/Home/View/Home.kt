package com.example.weatherforecast.Home.View

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
import com.example.weatherforecast.LocationViewModel
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.Model.WeatherData
import com.example.weatherforecast.R
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
    lateinit var txtPressure : TextView
    lateinit var weatherImage: ImageView
    lateinit var viewModel: WeatherViewModel
    lateinit var weatherViewModelFactory: WeatherViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)

        weatherViewModelFactory = WeatherViewModelFactory(Repo.getInstance(remoteSource))
        viewModel =
            ViewModelProvider(
                requireActivity(),
                weatherViewModelFactory
            ).get(WeatherViewModel::class.java)
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

        mAdapterForecast = NextFiveDaysAdapter() // i will later pass it from local data base
        mLayoutManagerForecast = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerViewForecast.apply {
            adapter = mAdapterForecast
            layoutManager = mLayoutManagerForecast
        }

        mAdapter = WeatherAdapter() // i will later pass it from local data base
        mLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is APIState.Loading -> {
                        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                    }

                    is APIState.Success -> {
                        val weatherInfo = state.data

                        val city = weatherInfo.name
                        val country = weatherInfo.sys.country


                        val unixTimestamp = weatherInfo.dt
                        val date = Date(unixTimestamp * 1000L)

                        val sdfDate = SimpleDateFormat("EEEE ,d MMM", Locale.getDefault())
                        sdfDate.timeZone = TimeZone.getDefault()
                        val formattedDate = sdfDate.format(date)

                        setBackgroundAccordingToTime(weatherInfo.dt)

                        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        sdfTime.timeZone = TimeZone.getDefault()
                        val formattedTime = sdfTime.format(date)

                        txtCurrentWeatherCity.text = "$country, $city"
                        txtCurrentWeatherDate.text = formattedDate


                        val tempceli: Int = (weatherInfo.main.temp - 273.15).roundToInt()
                        txtWeatherDesc.text = weatherInfo.weather[0].description
                        txtTemperature.text = " $tempceli °C"
                        txtWindSpeed.text = "${weatherInfo.wind.speed} m/s"
                        txtHumidaty.text = "${weatherInfo.main.humidity}%"
                        txtRain.text = "${weatherInfo.rain?.`1h` ?: 0} mm"
                        txtPressure.text = "${weatherInfo.main.pressure} hPa"
                        Glide.with(weatherImage.context)
                            .load("https://openweathermap.org/img/wn/${weatherInfo.weather[0].icon}@2x.png")
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(weatherImage)
                    }

                    is APIState.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${state.msg.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.forecastState.collect { state ->
                when (state) {
                    is APIState.SuccessForecast -> {
                        val forecastInfo = state.data
                        mAdapter.submitList(filterCurrentDayData(forecastInfo.list))
                        val NextDays = forecastInfo.list - filterCurrentDayData(forecastInfo.list)

                        val filteredList = mutableListOf<WeatherData>()

                        for (weatherData in NextDays) {
                            if (weatherData.dt_txt.contains("12:00:00")) {
                                filteredList.add(weatherData)
                            }
                        }

                        mAdapterForecast.submitList(filteredList)

                    }

                    is APIState.Failure -> {
                        Log.e("WeatherForecast", "Error fetching forecast: ${state.msg.message}")
                    }

                    else -> {}
                }
            }
        }
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        Log.d("HomeFragment", "Latitude: $latitude, Longitude: $longitude, City: $cityName")


        if (latitude != null) {
            if (longitude != null) {
                viewModel.getWeather(latitude, longitude)
            }
        }
        if (latitude != null) {
            if (longitude != null) {
                viewModel.getWeatherForecast(latitude, longitude)
            }
        }

    }

    private fun setBackgroundAccordingToTime(unixTimestamp: Long) {
        val date = Date(unixTimestamp * 1000L) // Convert seconds to milliseconds
        val calendar = Calendar.getInstance().apply {
            time = date
        }
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        val backgroundDrawable: Drawable? = when {
            hourOfDay in 6..18 -> {
                resources.getDrawable(R.drawable.sunny_bg) // Daytime background
            }
            else -> {
                resources.getDrawable(R.drawable.night_bg) // Nighttime background
            }
        }

        view?.setBackground(backgroundDrawable) // Set the background
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun filterCurrentDayData(listWeather: List<WeatherData>): List<WeatherData> {
        val currentTimeMillis = System.currentTimeMillis()
        val currentDate = Date(currentTimeMillis)

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayString = sdf.format(currentDate)

        val currentDayWeatherData = listWeather.filter {
            val weatherDate = Date(it.dt * 1000L)
            val weatherDateString = sdf.format(weatherDate)

            weatherDateString == todayString
        }
        return currentDayWeatherData
    }
}

