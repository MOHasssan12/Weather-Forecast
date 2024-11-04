package com.example.weatherforecast.Alerts

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmproducts.Network.RetrofitHelper
import com.example.mvvmproducts.Network.WeatherRemoteDataSource
import com.example.weatherforecast.DB.LocalDataSource
import com.example.weatherforecast.DB.WeatherDataBase
import com.example.weatherforecast.Model.Alert
import com.example.weatherforecast.Model.Repo
import com.example.weatherforecast.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect

class Alerts : Fragment() {

    private lateinit var alertsViewModel: AlertsViewModel
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlertAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val remoteSource = WeatherRemoteDataSource(RetrofitHelper.service)
        val weatherdao = WeatherDataBase.getInstance(requireContext()).getWeatherDAO()
        val alertDao = WeatherDataBase.getInstance(requireContext()).getAlertDAO()

        val localDataSource = LocalDataSource(weatherdao,alertDao)
        // Pass this data to the alert fragment or ViewModel as needed
        val  viewModelFactory = AlertsViewModelFactory(context?.let {
            Repo.getInstance(remoteSource, it , localDataSource)
        },requireContext())
        alertsViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            AlertsViewModel::class.java)

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alerts, container, false)

        requestNotificationPermission()
        createNotificationChannel()


        val fab: FloatingActionButton = view.findViewById(R.id.Alert_Add)
        fab.setOnClickListener {
            showAlertDialog()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_Alerts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter = AlertAdapter()
        recyclerView.adapter = adapter


        lifecycleScope.launchWhenStarted {
            alertsViewModel.alerts.collect { alertsList ->
                adapter.submitList(alertsList)
            }
        }

        // Set up ItemTouchHelper for swipe-to-delete functionality
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
                    // Remove item from adapter and notify ViewModel to delete from database
                    adapter.removeItemAtPosition(position)

                    alertsViewModel.deleteAlert(deletedWeather)

                    // Show Snackbar for undo option
                    Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            adapter.addItemAtPosition(deletedWeather, position)
                            alertsViewModel.addAlert(deletedWeather)
                        }.show()
                }else{
                    adapter.notifyDataSetChanged()
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun showAlertDialog() {
        val dialog = AlertDialogFragment()
        dialog.setOnAlertSetListener(object : AlertDialogFragment.OnAlertSetListener {
            override fun onAlertSet(timeInMillis: Long) {
                setAlarm(timeInMillis)
            }
        })
        dialog.show(requireActivity().supportFragmentManager, "AlertDialog")
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("message", "Weather Alert!")
        }

        // Make the PendingIntent unique by using the current time
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeInMillis.toInt(), // Use time in milliseconds as a unique request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Update existing if it exists
        )

        // Set the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel_id"
            val channelName = "Alarm Notifications"
            val channelDescription = "Notifications for scheduled alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}