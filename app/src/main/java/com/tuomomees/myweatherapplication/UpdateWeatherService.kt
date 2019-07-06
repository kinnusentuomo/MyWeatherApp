package com.tuomomees.myweatherapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import android.support.v4.os.HandlerCompat.postDelayed
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import android.support.annotation.NonNull




class UpdateWeatherService : Service(), WeatherDetailGetterThread.ThreadReport {
    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {

    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {

        Log.d("Service", "Weather status: " + myWeatherDetailObject.weather)
        if(myWeatherDetailObject.weather == "Rain"){
            createNotification(myWeatherDetailObject.icon, "Watch out, it is rainy in: " + myWeatherDetailObject.cityName, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
        }
    }



    val appId = "7ac8041476369264714a77f37e2f4141"
    private lateinit var notificationManager: NotificationManager

    var context: Context = this
    var handler: Handler? = null
    var runnable: Runnable? = null


    override fun onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()
        val lat = SimpleWeatherWidget.getSharedPref("last_location_lat", this)
        val lon = SimpleWeatherWidget.getSharedPref("last_location_lon", this)

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

        handler!!.postDelayed(runnable, TimeUnit.MINUTES.toMillis(60))
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
