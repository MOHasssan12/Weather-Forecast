package com.example.weatherforecast.Settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class SettingsMapFragment : Fragment() {

    private lateinit var mapView2: MapView
    private lateinit var txtCity2: TextView
    private lateinit var btnSelect2: Button
    private val requestPermissionsRequestCode = 1

    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))

        val view = inflater.inflate(R.layout.fragment_settings_map, container, false)
        mapView2 = view.findViewById(R.id.mapView2)
        txtCity2 = view.findViewById(R.id.txtCity2)
        btnSelect2 = view.findViewById(R.id.btnSelect2)

        mapView2.setMultiTouchControls(true)

        val middleEastGeoPoint = GeoPoint(25.0, 45.0)
        mapView2.controller.setZoom(5.0)
        mapView2.controller.setCenter(middleEastGeoPoint)

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )


        mapView2.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val geoPoint = mapView2.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint?
                    geoPoint?.let {
                        addMarker(it.latitude, it.longitude)
                        updateCityName(it.latitude, it.longitude)
                    }
                    true
                }
                else -> false
            }
        }


        btnSelect2.setOnClickListener {
            val geoPoint = mapView2.overlays
                .filterIsInstance<Marker>()
                .firstOrNull()?.position
            geoPoint?.let {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("location", it)
                findNavController().popBackStack(R.id.settings, false)
            }
        }

        return view
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        mapView2.overlays.clear()
        val marker = Marker(mapView2)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView2.overlays.add(marker)
        mapView2.controller.animateTo(marker.position)
    }

    private fun updateCityName(latitude: Double, longitude: Double) {
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null) {
                if (addressList.isNotEmpty()) {
                    val cityName = addressList[0]?.locality ?: addressList[0]?.subAdminArea ?: "Unknown"
                    txtCity2.text = cityName
                } else {
                    txtCity2.text = "Unknown Location"
                }
            }
        } catch (e: Exception) {
            txtCity2.text = "Error: ${e.message}"
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
        mapView2.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView2.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView2.onDetach()
    }
}