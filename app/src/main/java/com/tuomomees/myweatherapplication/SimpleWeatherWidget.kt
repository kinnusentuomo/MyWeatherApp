package com.tuomomees.myweatherapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.cast.CastRemoteDisplayLocalService.startService
import android.content.Intent
import android.content.ComponentName
import android.provider.SyncStateContract
import android.util.Log


/**
 * Implementation of App Widget functionality.
 */
class SimpleWeatherWidget : AppWidgetProvider() {


    val TAG = "WidgetProvider"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            Log.w(TAG, "onUpdate method called")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    companion object : WeatherDetailGetterThread.ThreadReport {

        private lateinit var views: RemoteViews
        private lateinit var myAppWidgetManager: AppWidgetManager
        private var myWidgetId: Int = 0
        val appId = "7ac8041476369264714a77f37e2f4141"
        override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {
            Log.d("ok", "ok")
        }

        override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {
            Log.d("ThreadReady", "Data: " + myWeatherDetailObject.cityName)
            views.setTextViewText(R.id.appwidget_text, myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
            myAppWidgetManager.updateAppWidget(myWidgetId, views)
        }

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            myWidgetId = appWidgetId
            myAppWidgetManager = appWidgetManager




            val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + 65.0 + "&lon=" + 25.0 + "&appid=" + appId
            val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, this)
            weatherDetailGetterThread.call()

            // Instruct the widget manager to update the widget
            views = RemoteViews(context.packageName, R.layout.simple_weather_widget)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

