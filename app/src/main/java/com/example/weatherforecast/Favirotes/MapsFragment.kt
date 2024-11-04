package com.example.weatherforecast.Favirotes

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.location.Geocoder
import java.util.Locale

class MapsFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var txtCity: TextView
    private lateinit var btnSelect: Button
    private val requestPermissionsRequestCode = 1

    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))

        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        mapView = view.findViewById(R.id.mapView)
        txtCity = view.findViewById(R.id.txtCity)
        btnSelect = view.findViewById(R.id.btnSelect)

        mapView.setMultiTouchControls(true)

        val middleEastGeoPoint = GeoPoint(25.0, 45.0)  // Roughly Middle East
        mapView.controller.setZoom(5.0)  // Adjust zoom level as needed
        mapView.controller.setCenter(middleEastGeoPoint)

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val geoPoint = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint?
                    geoPoint?.let {
                        addMarker(it.latitude, it.longitude)
                        updateCityName(it.latitude, it.longitude)
                    }
                    true
                }
                else -> false
            }
        }

        btnSelect.setOnClickListener {
            val geoPoint = mapView.overlays
                .filterIsInstance<Marker>()
                .firstOrNull()?.position
            geoPoint?.let {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("location", it)
                findNavController().popBackStack()
            }
        }

        return view
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView.overlays.add(marker)
        mapView.controller.animateTo(marker.position)
    }

    private fun updateCityName(latitude: Double, longitude: Double) {
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null) {
                if (addressList.isNotEmpty()) {
                    val cityName = addressList[0]?.locality ?: addressList[0]?.subAdminArea ?: "Unknown"
                    txtCity.text = cityName
                } else {
                    txtCity.text = "Unknown Location"
                }
            }
        } catch (e: Exception) {
            txtCity.text = "Error: ${e.message}"
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                requestPermissionsRequestCode
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}
