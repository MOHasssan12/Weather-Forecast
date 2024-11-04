package com.example.weatherforecast.Favirotes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmproducts.Network.APIState
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.LocalDataSource
import com.example.weatherforecast.DB.WeatherDataBase
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.R
import com.example.weatherforecast.Settings.SettingsViewModel
import com.example.weatherforecast.Settings.SettingsViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint


class Favirotes : Fragment() {

    private lateinit var addFavorite: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavAdapter
    private lateinit var viewModel: FavirotesViewModel
    private lateinit var viewModelFactory: FavirotesViewModelFactory

    private lateinit var settingsViewModel: SettingsViewModel

    private var temperatureUnit: String = "Kelvin"
    private var windSpeedUnit: String = "km/h"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
        val weatherdao = WeatherDataBase.getInstance(requireContext()).getWeatherDAO()
        val alertDao = WeatherDataBase.getInstance(requireContext()).getAlertDAO()

        val localDataSource = LocalDataSource(weatherdao,alertDao)
        viewModelFactory = FavirotesViewModelFactory(context?.let {
            Repo.getInstance(remoteSource, it , localDataSource)
        })
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FavirotesViewModel::class.java)


        settingsViewModel = activityViewModels<SettingsViewModel> {
            Repo.getInstance(WeatherRemoteDataSource(RetrofitHelper.service), requireContext(),localDataSource)
                ?.let { SettingsViewModelFactory(it) }!!
        }.value

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GeoPoint>("location")
            ?.observe(this) { geoPoint ->
                if (geoPoint != null) {
                    addFavoriteLocation(geoPoint.latitude, geoPoint.longitude)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favirotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavAdapter { weatherInfo ->
            val intent = Intent(requireContext(), FavDetails::class.java)
            intent.putExtra("latitude", weatherInfo.coord.lat)
            intent.putExtra("longitude", weatherInfo.coord.lon)
            startActivity(intent)
        }

        addFavorite = view.findViewById(R.id.FAB_Add)
        recyclerView = view.findViewById(R.id.rv_favirotes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter


        viewModel.loadFavoriteWeather()

        addFavorite.setOnClickListener {
            findNavController().navigate(R.id.action_favirotes_to_mapsFragment)
        }

        observeSettingsChanges()

        lifecycleScope.launchWhenStarted {
            viewModel.favoriteWeatherData.collect { favoriteWeatherList ->
                adapter.submitList(favoriteWeatherList)
            }
        }
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedWeather = adapter.getWeatherAtPosition(position)

                if (deletedWeather != null) {
                    adapter.removeItemAtPosition(position)

                    viewModel.deleteFavoriteLocation(deletedWeather)


                    Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            adapter.addItemAtPosition(deletedWeather, position)
                            viewModel.addFavoriteLocation(deletedWeather)
                        }.show()
                }else{
                    adapter.notifyDataSetChanged()
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun addFavoriteLocation(lat: Double, lon: Double) {
        lifecycleScope.launch {
            viewModel.getWeather(lat, lon)
            viewModel.favoriteLocations.collect { state ->
                if (state is APIState.Success) {
                    val weatherInfo = state.data
                    viewModel.addFavoriteLocation(weatherInfo)
                }
            }
        }
    }

    private fun observeSettingsChanges() {
        lifecycleScope.launchWhenStarted {
            settingsViewModel.temperatureUnit.collect { unit ->
                temperatureUnit = unit
                adapter.setTemperatureUnit(temperatureUnit)
            }
        }

        lifecycleScope.launchWhenStarted {
            settingsViewModel.windSpeedUnit.collect { unit ->
                windSpeedUnit = unit
                adapter.setTemperatureUnit(temperatureUnit)
            }
        }
    }


}
