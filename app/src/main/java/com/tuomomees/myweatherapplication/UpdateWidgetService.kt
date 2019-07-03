package com.tuomomees.myweatherapplication

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.google.android.gms.location.FusedLocationProviderClient





class UpdateWidgetService : Service(), WeatherDetailGetterThread.ThreadReport {
    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {
        Log.d(TAG, "nothing to do")
    }

    val appId = "7ac8041476369264714a77f37e2f4141"
    val TAG = "UpdateWidgetService"

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {

        val remoteViews = RemoteViews(this.packageName, R.layout.simple_weather_widget)
        /*val remoteViews = RemoteViews(
            this
                .applicationContext.packageName,
            R.layout.simple_weather_widget
        )*/

        // Set the text
        remoteViews.setTextViewText(
            R.id.appwidget_text,
            myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c
        )

        remoteViews.setTextViewText(
            R.id.appwidget_text,
            "Joopajoo"
        )

        Log.d("UpdateWidgetService", "views" + remoteViews)



        Log.d("UpdateWidgetService", "Trying to update widget with: " + myWeatherDetailObject.cityName)
    }

    override fun onStart(intent: Intent, startId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(
            this
                .applicationContext
        )

        val allWidgetIds = intent
            .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

        //      ComponentName thisWidget = new ComponentName(getApplicationContext(),
        //              MyWidgetProvider.class);
        //      int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);

        for (widgetId in allWidgetIds) {



            val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + 25.0 + "&lon=" + 65.0 + "&appid=" + appId
            val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
            weatherDetailGetterThread.call()


            val remoteViews = RemoteViews(
                this
                    .applicationContext.packageName,
                R.layout.simple_weather_widget
            )


            // Register an onClickListener
            val clickIntent = Intent(
                this.applicationContext,
                SimpleWeatherWidget::class.java
            )

            clickIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            clickIntent.putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                allWidgetIds
            )

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            remoteViews.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)
            appWidgetManager.updateAppWidget(widgetId, remoteViews)

        }
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
