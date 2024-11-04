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

    private var locationData: AlertLocationData? = null

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    init {
        viewModelScope.launch {
            repo.getAlertsWeather().collect { alertList ->
                _alerts.value = alertList
            }
        }
    }

    fun setLocationData(latitude: Double, longitude: Double, cityName: String, dateTimeString: String) {
        locationData = AlertLocationData(latitude, longitude, cityName, dateTimeString)
    }

    fun addAlert(alert: Alert) {
        viewModelScope.launch {
            repo.saveAlert(alert)
        }
    }

    fun saveAlert() {
        locationData?.let { data ->
            val alert = Alert(
                id = 0,
                latitude = data.latitude,
                longitude = data.longitude,
                cityName = data.cityName,
                dateTime = data.dateTimeString
            )
            viewModelScope.launch {
                repo.saveAlert(alert)
                setAlarm(alert)
            }
        }
    }

    fun deleteAlert(alert: Alert) {
        viewModelScope.launch {
            cancelAlarm(alert)

            repo.deleteAlert(alert)
        }
    }

    private fun setAlarm(alert: Alert) {
    }

    private fun cancelAlarm(alert: Alert) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alert.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )


        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}

data class AlertLocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val dateTimeString: String
)