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
import java.util.*
import java.util.concurrent.TimeUnit


class UpdateWeatherService : Service(), WeatherDetailGetterThread.ThreadReport {
    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {

    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        Log.d("Service", "Weather status: " + myWeatherDetailObject.weather)
        //if(myWeatherDetailObject.weather == "Rain"){
            createNotification(myWeatherDetailObject.icon, "Watch out, it is rainy in " + myWeatherDetailObject.cityName, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
        //}
    }

    val appId = "7ac8041476369264714a77f37e2f4141"
    private lateinit var notificationManager: NotificationManager

    var context: Context = this
    var handler: Handler? = null
    var runnable: Runnable? = null

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location

    override fun onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)




        val lat = SimpleWeatherWidget.getSharedPref("last_location_lat", this)
        val lon = SimpleWeatherWidget.getSharedPref("last_location_lon", this)


        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
/*
        handler = Handler()
        runnable = Runnable {
            //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show()

            //val queryString = "https://api.openweathermap.org/data/2.5/weather?q=" + "Brahin" + "&appid=" + appId
            val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appId
            val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
            weatherDetailGetterThread.call()


            notificationManager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager


            handler!!.postDelayed(runnable, /*10000*/ TimeUnit.MINUTES.toMillis(60))
        }

        handler!!.postDelayed(runnable, TimeUnit.MINUTES.toMillis(60))*/

        getData()
    }



    private fun getData(){
        val t = Timer()
        val tt = object : TimerTask() {

            override fun run() {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location ->
                        lastLocation = location
                    }

                fusedLocationClient.lastLocation
                    .addOnCompleteListener{
                        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lastLocation.latitude + "&lon=" + lastLocation.longitude + "&appid=" + appId
                        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, this@UpdateWeatherService)
                        weatherDetailGetterThread.call()
                    }
            }

        }
        t.schedule(tt, /*10 * 1000*/ TimeUnit.MINUTES.toMillis(1)) //Schedule to run tt (TimerTask) again after 10 seconds
    }



    override fun onBind(intent: Intent): IBinder? {

/*
        val lat = SimpleWeatherWidget.getSharedPref("last_location_lat", this)
        val lon = SimpleWeatherWidget.getSharedPref("last_location_lon", this)

        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()


        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager*/

        return null
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent, startid: Int) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }


    fun createNotification(icon: Int, title: String, message: String){

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

/* //Modern way Android O ++
    inner class MyJobCreator : JobCreator {

        @Nullable
        fun create(tag: String): Job? {
            when (tag) {
                MySyncJob.TAG -> return MySyncJob()
                else -> return null
            }
        }
    }

    inner class MySyncJob : Job() {

        protected fun onRunJob(params: Params): Result {
            //
            // run your job here
            //
            //
            return Result.SUCCESS
        }

        companion object {

            val TAG = "my_job_tag"

            fun scheduleJob() {
                JobRequest.Builder(MySyncJob.TAG)
                    .setExecutionWindow(30_000L, 40_000L) //Every 30 seconds for 40 seconds
                    .build()
                    .schedule()
            }
        }
    }*/

}
