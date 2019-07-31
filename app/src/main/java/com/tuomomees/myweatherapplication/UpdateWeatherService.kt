package com.tuomomees.myweatherapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class UpdateWeatherService : Service(), WeatherDetailGetterThread.ThreadReport {

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        val sdf = SimpleDateFormat("hh:mm")
        val currentTime = sdf.format(Date())

        Log.d("Service", "Weather status: " + myWeatherDetailObject.weather + " " +currentTime)
        if(myWeatherDetailObject.weather == "Rain" || myWeatherDetailObject.weather == "Rainy"){
            createNotification(myWeatherDetailObject.icon, "Watch out, it is rainy in " + myWeatherDetailObject.cityName, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
        }
    }

    private lateinit var notificationManager: NotificationManager
    var context: Context = this
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location
    val TAG = "Service"

    var startTime: Long = 0

    override fun onCreate() {/*
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        lastLocation = Location("")

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

        getData()*/
    }

    private fun getData(){

        val handler = Handler()
        val weatherDataUpdaterLoop = object : Runnable {
            override fun run() {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            lastLocation = location
                        }
                    }

                fusedLocationClient.lastLocation
                    .addOnCompleteListener{
                        Helper().sendQueryWithLocation(lastLocation, 0, context, this@UpdateWeatherService)
                    }
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(30)) //interval can be defined here
            }
        }

        handler.post(weatherDataUpdaterLoop)
    }

    override fun onBind(intent: Intent): IBinder? {

        startTime = System.currentTimeMillis()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        lastLocation = Location("")
        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

        getData()
        return null
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);

        val aliveTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Service aliveTime: " + aliveTime)
        //Toast.makeText(this, "Service aliveTime: " + aliveTime, Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent, startid: Int) {
        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }

    private fun createNotification(icon: Int, title: String, message: String){

        val channelID = "weather notifications"
        createNotificationChannel(channelID, "tämän tuubin kautta ammutaan notifikaatioita", channelID)

        val notification = Notification.Builder(applicationContext,
            channelID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setChannelId(channelID)
            .build()

        notificationManager.notify(0, notification)
    }

    //API 25 has to be supported in my case because of the old testing devices
    private fun createNotificationChannel(name: String, description: String, id: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
