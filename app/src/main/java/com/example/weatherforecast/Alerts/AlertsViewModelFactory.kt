package com.example.weatherforecast.Alerts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.Model.Repo

class AlertsViewModelFactory (private val repo: Repo?, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            return repo?.let { AlertsViewModel(repo,context) } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}