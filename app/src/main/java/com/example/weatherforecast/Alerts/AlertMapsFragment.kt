package com.example.weatherforecast.Alerts

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlertMapsFragment : Fragment() {

    private lateinit var mapView3: MapView
    private lateinit var txtCity3: TextView
    private lateinit var btnSelect3: Button
    private val requestPermissionsRequestCode = 1
    private val geocoder by lazy { Geocoder(requireContext(), Locale.getDefault()) }
    private var selectedDateTime: Calendar? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(0))

        val view = inflater.inflate(R.layout.fragment_alert_maps, container, false)
        mapView3 = view.findViewById(R.id.mapView3)
        txtCity3 = view.findViewById(R.id.txtCity3)
        btnSelect3 = view.findViewById(R.id.btnSelect3)

        mapView3.setMultiTouchControls(true)

        val middleEastGeoPoint = GeoPoint(25.0, 45.0)
        mapView3.controller.setZoom(5.0)
        mapView3.controller.setCenter(middleEastGeoPoint)

        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        mapView3.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val geoPoint = mapView3.projection.fromPixels(
                        event.x.toInt(),
                        event.y.toInt()
                    ) as GeoPoint?
                    geoPoint?.let {
                        addMarker(it.latitude, it.longitude)
                        updateCityName(it.latitude, it.longitude)
                    }
                    true
                }
                else -> false
            }
        }



        btnSelect3.setOnClickListener {
            val geoPoint = mapView3.overlays
                .filterIsInstance<Marker>()
                .firstOrNull()?.position
            geoPoint?.let {
                val cityName = txtCity3.text.toString()
                val dateTimeString = selectedDateTime?.let { calendar ->
                    val formatter = SimpleDateFormat("EEE, d MMM - hh : mm a", Locale.getDefault())
                    "Date : ${formatter.format(calendar.time)}"
                } ?: "No Date and Time Selected"

                val resultBundle = Bundle().apply {
                    putDouble("latitude", it.latitude)
                    putDouble("longitude", it.longitude)
                    putString("city_name", cityName)
                    putString("date_time", dateTimeString)
                }

                setFragmentResult("locationResult", resultBundle)
                findNavController().popBackStack()
            }
        }

        return view
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        mapView3.overlays.clear()
        val marker = Marker(mapView3)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Selected Location"
        mapView3.overlays.add(marker)
        mapView3.controller.animateTo(marker.position)
    }

    private fun updateCityName(latitude: Double, longitude: Double) {
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val cityName = addressList[0]?.locality ?: addressList[0]?.subAdminArea ?: "Unknown"
                txtCity3.text = cityName
            } else {
                txtCity3.text = "Unknown Location"
            }
        } catch (e: Exception) {
            txtCity3.text = "Error: ${e.message}"
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                requestPermissionsRequestCode
            )
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // Show TimePicker after date is picked
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        selectedDateTime = calendar
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }



    override fun onResume() {
        super.onResume()
        mapView3.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView3.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView3.onDetach()
    }
}
