package com.example.weatherforecast.Home.View

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.Home.ViewModel.WeatherViewModel
import com.example.weatherforecast.Home.ViewModel.WeatherViewModelFactory
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.R


class Home : Fragment() {

    private lateinit var weatherDescription: TextView
    private lateinit var temperature: TextView
    private lateinit var windSpeed: TextView

    lateinit var viewModel: WeatherViewModel
    lateinit var weatherViewModelFactory: WeatherViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)

        weatherViewModelFactory = WeatherViewModelFactory(Repo.getInstance(remoteSource))
        viewModel =
            ViewModelProvider(this, weatherViewModelFactory).get(WeatherViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weatherDescription = view.findViewById(R.id.tv_weather_description)
        temperature = view.findViewById(R.id.tv_temperature)
        windSpeed = view.findViewById(R.id.tv_wind_speed)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is APIState.Loading -> {
                        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                    }

                    is APIState.Success -> {
                        val weatherInfo = state.data
                        weatherDescription.text = weatherInfo.weather[0].description
                        temperature.text = "Temperature: ${weatherInfo.main.temp} K"
                        windSpeed.text = "Wind Speed: ${weatherInfo.wind.speed} m/s"
                    }

                    is APIState.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${state.msg.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
       viewModel.getWeather(lat = 70.5, lon = 7.367)
    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {

                }
            }
    }
}