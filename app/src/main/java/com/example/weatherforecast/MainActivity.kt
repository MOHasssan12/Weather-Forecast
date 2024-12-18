package com.example.weatherforecast

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    val REQUEST_LOCATION_CODE = 5005
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationFetched = false
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.btm_nav)
        val navController = Navigation.findNavController(this, R.id.host_fragment)
        bottomNavigationView.setupWithNavController(navController)


        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        listenForNetworkChanges(navController)

        // Initial network check
        if (!isNetworkAvailable()) {
            navController.navigate(R.id.noNetwork)
        }
    }

    override fun onStart() {
        super.onStart()
        if (checkPermission()) {
            if (isLocationEnabled()) {
                getFreshLocation()
            } else {
                enableLocationServices()
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFreshLocation()
            }
        }
    }

    fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun listenForNetworkChanges(navController: NavController) {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (navController.currentDestination?.id == R.id.noNetwork) {
                        navController.popBackStack()
                    }
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    navController.navigate(R.id.noNetwork)
                }
            }
        })
    }

    fun getFreshLocation() {
        if (locationFetched) return

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    if (locationResult.locations.isNotEmpty()) {
                        val location = locationResult.lastLocation
                        val latitude = location?.latitude
                        val longitude = location?.longitude
                        Log.i(TAG, "Latitude: $latitude, Longitude: $longitude")

                        if (!locationFetched) {
                            locationFetched = true
                            if (latitude != null) {
                                if (longitude != null) {
                                    val cityName = getCityName(latitude, longitude)
                                    navigateToHomeFragment(latitude, longitude,cityName)
                                }
                            }
                            fusedLocationProviderClient.removeLocationUpdates(this)
                        }
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun getCityName(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                Log.i(TAG, "Full Address: ${address.getAddressLine(0)}")
                Log.i(TAG, "Locality: ${address.locality}")
                Log.i(TAG, "Country: ${address.countryName}")
                address.locality // Return the locality
            } else {
                Log.i(TAG, "No addresses found for the provided location.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoder service not available", e)
            null
        }
    }


    fun navigateToHomeFragment(latitude: Double, longitude: Double, cityName: String?) {
        val navController = findNavController(R.id.host_fragment)
        val bundle = Bundle().apply {
            putDouble("latitude", latitude)
            putDouble("longitude", longitude)
            putString("city_name", cityName)
        }
        navController.navigate(R.id.home2, bundle)
    }

    fun enableLocationServices() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
