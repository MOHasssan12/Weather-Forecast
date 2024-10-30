package com.example.weatherforecast.Favirotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmproducts.Network.APIState
import com.example.weatherforecast.R
import com.example.weatherforecast.Model.Repo
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.Model.LatLong
import com.example.weatherforecast.Model.WeatherInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.util.GeoPoint

class Favirotes : Fragment() {

    private lateinit var addFavorite: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavAdapter
    private lateinit var viewModel: FavirotesViewModel
    private lateinit var viewModelFactory: FavirotesViewModelFactory
    private val listOfLocations: MutableList<LatLong> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
        viewModelFactory = FavirotesViewModelFactory(context?.let {
            Repo.getInstance(remoteSource,
                it
            )
        })
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(FavirotesViewModel::class.java)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<GeoPoint>("location")
            ?.observe(this) { geoPoint ->
                listOfLocations.add(LatLong(geoPoint.latitude, geoPoint.longitude))
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

        addFavorite = view.findViewById(R.id.FAB_Add)
        recyclerView = view.findViewById(R.id.rv_favirotes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext() , RecyclerView.VERTICAL, false)
        adapter = FavAdapter()
        recyclerView.adapter = adapter

        addFavorite.setOnClickListener {
            findNavController().navigate(R.id.action_favirotes_to_mapsFragment)
        }

        lifecycleScope.launchWhenStarted {
            val weatherDataList = mutableListOf<WeatherInfo>()
            for (location in listOfLocations) {
                viewModel.getWeather(location.latitude, location.longitude)
                viewModel.favoriteLocations.collect { state ->
                    if (state is APIState.Success) {
                        weatherDataList.add(state.data)
                        adapter.submitList(weatherDataList)
                    } else if (state is APIState.Failure) {
                    }
                }
            }
        }
    }

}
