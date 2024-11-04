package com.example.weatherforecast.Alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(private val repo: Repo, private val context: Context) : ViewModel() {

    // Location data is now a regular variable
    private var locationData: AlertLocationData? = null

    // Backing property for alerts list from the database
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    init {
        // Start collecting alerts from the repository
        viewModelScope.launch {
            repo.getAlertsWeather().collect { alertList ->
                _alerts.value = alertList
            }
        }
    }

    // Function to set the location data from AlertMapsFragment
    fun setLocationData(latitude: Double, longitude: Double, cityName: String, dateTimeString: String) {
        locationData = AlertLocationData(latitude, longitude, cityName, dateTimeString)
    }

    fun addAlert(alert: Alert) {
        viewModelScope.launch {
            repo.saveAlert(alert)
        }
    }

    // Save an alert based on the current location data
    fun saveAlert() {
        locationData?.let { data ->
            val alert = Alert(
                id = 0, // This will be auto-generated
                latitude = data.latitude,
                longitude = data.longitude,
                cityName = data.cityName,
                dateTime = data.dateTimeString
            )
            viewModelScope.launch {
                repo.saveAlert(alert)
                // Set the alarm after saving the alert if needed
                setAlarm(alert) // Assuming you want to set the alarm here
            }
        }
    }

    // Delete an alert and cancel its corresponding alarm
    fun deleteAlert(alert: Alert) {
        viewModelScope.launch {
            // Cancel the alarm using AlarmManager
            cancelAlarm(alert)

            // Delete the alert from the repository
            repo.deleteAlert(alert)
        }
    }

    // Method to set the alarm (you can adjust this according to your implementation)
    private fun setAlarm(alert: Alert) {
        // Implementation for setting the alarm based on the alert details
        // This is where you would add the code to set the alarm
    }

    // Method to cancel the alarm
    private fun cancelAlarm(alert: Alert) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java) // Use your AlarmReceiver class
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alert.id, // Use the alert ID as request code to cancel
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Check if the pending intent exists
        )

        // Cancel the alarm if it exists
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}

// Data class to store location details before saving as an alert
data class AlertLocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val dateTimeString: String
)