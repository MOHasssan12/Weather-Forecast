package com.example.weatherforecast.Alerts

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.LocalDataSource
import com.example.weatherforecast.DB.WeatherDataBase
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.R
import java.util.*

class AlertDialogFragment : DialogFragment() {

    private var lastSelectedLocation: String? = null
    private var listener: OnAlertSetListener? = null
    private var shouldNavigateToMap: Boolean = false // Flag to track navigation

    lateinit var radio_current_location: RadioButton
    lateinit var radio_map: RadioButton

    interface OnAlertSetListener {
        fun onAlertSet(timeInMillis: Long)
    }

    fun setOnAlertSetListener(listener: OnAlertSetListener) {
        this.listener = listener
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = Dialog(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_alert_dialog, null)
        builder.setContentView(view)

        val timePicker: TimePicker = view.findViewById(R.id.timePicker)
        val dateButton: Button = view.findViewById(R.id.btn_set_date)
        val setButton: Button = view.findViewById(R.id.btn_set_alert)
        radio_current_location = view.findViewById(R.id.radio_current_location)
        radio_map = view.findViewById(R.id.radio_map)

        radio_current_location.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lastSelectedLocation = "current_location"
                radio_map.isChecked = false  // Ensure only one is checked
            }
        }

        radio_map.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lastSelectedLocation = "Map"
                radio_current_location.isChecked = false  // Ensure only one is checked
                shouldNavigateToMap = true // Set the flag for navigation
            }
        }

        // Initialize calendar for date and time selection
        val calendar = Calendar.getInstance()

        // Date picker dialog
        dateButton.setOnClickListener {
            DatePickerDialog(requireContext(), { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Set the time and date when user clicks "Set Alert"
        setButton.setOnClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            listener?.onAlertSet(calendar.timeInMillis)
            dismiss()
        }

        return builder
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check the flag and navigate if necessary
        if (shouldNavigateToMap) {
            findNavController().navigate(R.id.action_alertDialogFragment_to_alertMapsFragment)
        }

        setFragmentResultListener("locationResult") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            val cityName = bundle.getString("city_name", "Unknown")
            val dateTimeString = bundle.getString("date_time", "No Date and Time Selected")

            val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
            val weatherdao = WeatherDataBase.getInstance(requireContext()).getWeatherDAO()
            val alertDao = WeatherDataBase.getInstance(requireContext()).getAlertDAO()
            val localDataSource = LocalDataSource(weatherdao, alertDao)

            val viewModelFactory = AlertsViewModelFactory(context?.let {
                Repo.getInstance(remoteSource, it, localDataSource)
            }, requireContext())
            val viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(AlertsViewModel::class.java)
            viewModel.setLocationData(latitude, longitude, cityName, dateTimeString)
        }
    }
}