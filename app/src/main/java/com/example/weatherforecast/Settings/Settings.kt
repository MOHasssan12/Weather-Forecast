package com.example.weatherforecast.Settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.Model.LatLong
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.R
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class Settings : Fragment() {

    private val viewModel: SettingsViewModel by activityViewModels {
        context?.let {
            Repo.getInstance(
                WeatherRemoteDataSource(RetrofitHelper.service), it
            )
        }?.let {
            SettingsViewModelFactory(
                it
            )
        }!!
    }

    private lateinit var radioEnglish: RadioButton
    private lateinit var radioArabic: RadioButton
    private lateinit var radioKelvin: RadioButton
    private lateinit var radioCelsius: RadioButton
    private lateinit var radioKmh: RadioButton
    private lateinit var radioMph: RadioButton
    private lateinit var radioGps: RadioButton
    private lateinit var radioMap: RadioButton

    private var lastSelectedLocation: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize RadioButtons
        radioEnglish = view.findViewById(R.id.radio_english)
        radioArabic = view.findViewById(R.id.radio_arabic)
        radioKelvin = view.findViewById(R.id.radio_kelvin)
        radioCelsius = view.findViewById(R.id.radio_celsius)
        radioKmh = view.findViewById(R.id.radio_kmh)
        radioMph = view.findViewById(R.id.radio_mph)
        radioGps = view.findViewById(R.id.radio_gps)
        radioMap = view.findViewById(R.id.radio_map)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GeoPoint>("location")
            ?.observe(viewLifecycleOwner) { geoPoint ->
                viewModel.updateLat(geoPoint.latitude)
                viewModel.updateLong(geoPoint.longitude)
            }


        setListeners()
        observeSettings()

        return view
    }

    private fun setListeners() {
        radioEnglish.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateLanguage("English")
        }

        radioArabic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateLanguage("Arabic")
        }

        radioKelvin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateTemperatureUnit("Kelvin")
        }

        radioCelsius.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateTemperatureUnit("Celsius")
        }

        radioKmh.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateWindSpeedUnit("km/h")
        }

        radioMph.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.updateWindSpeedUnit("mile/h")
        }
        radioGps.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.updateLocation("Gps")
                lastSelectedLocation = "Gps" // Update last selected location
            }
        }

        radioMap.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (lastSelectedLocation == "Gps") {
                    findNavController().navigate(R.id.action_settings_to_settingsMapFragment)
                }
                viewModel.updateLocation("Map")
                lastSelectedLocation = "Map" // Update last selected location
            }
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            viewModel.language.collect { language ->
                radioEnglish.isChecked = (language == "English")
                radioArabic.isChecked = (language == "Arabic")
            }
        }

        lifecycleScope.launch {
            viewModel.temperatureUnit.collect { unit ->
                radioKelvin.isChecked = (unit == "Kelvin")
                radioCelsius.isChecked = (unit == "Celsius")
            }
        }

        lifecycleScope.launch {
            viewModel.windSpeedUnit.collect { unit ->
                radioKmh.isChecked = (unit == "km/h")
                radioMph.isChecked = (unit == "mile/h")
            }
        }
        lifecycleScope.launch {
            viewModel.location.collect { location ->
                radioGps.isChecked = (location == "Gps")
                radioMap.isChecked = (location == "Map")
            }
        }
    }
}