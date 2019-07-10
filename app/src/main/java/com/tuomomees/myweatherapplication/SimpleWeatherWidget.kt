package com.tuomomees.myweatherapplication

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import android.util.Log
import android.widget.RemoteViews
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Implementation of App Widget functionality.
 */
class SimpleWeatherWidget : AppWidgetProvider() {

    val TAG = "WidgetProvider"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location ->
                lastLocation = location
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{

                // There may be multiple widgets active, so update all of them
                for (appWidgetId in appWidgetIds) {
                    Log.w(TAG, "onUpdate method called")
                    updateAppWidget(context, appWidgetManager, appWidgetId, lastLocation)
                }
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

        }

        override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {
            Log.d("ThreadReady", "Data: " + myWeatherDetailObject.cityName)

            //update texts
            views.setTextViewText(R.id.appwidget_text_cityname, myWeatherDetailObject.cityName)
            views.setTextViewText(R.id.appwidget_text_temp_c, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
            views.setTextViewText(R.id.appwidget_text_humidity, myWeatherDetailObject.humidity.toString() + "%")
            views.setTextViewText(R.id.appwidget_text_wind_speed, myWeatherDetailObject.windSpeed.toString() + "m/s")

            //update icon
            views.setImageViewResource(R.id.imageViewWidget, myWeatherDetailObject.icon)

            //notify widget manager to update widget
            myAppWidgetManager.updateAppWidget(myWidgetId, views)
        }


        fun getSharedPref(key: String, context: Context): Float {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getFloat(key, 0.0f)
        }

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            lastLocation: Location
        ) {
            //myWidgetId = appWidgetId
            myAppWidgetManager = appWidgetManager

            //val lat = getSharedPref("last_location_lat", context)
            //val lon = getSharedPref("last_location_lon", context)

            //val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appId
            val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lastLocation.latitude + "&lon=" + lastLocation.longitude + "&appid=" + appId
            val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, this)
            weatherDetailGetterThread.call()

            // Instruct the widget manager to update the widget
            views = RemoteViews(context.packageName, R.layout.simple_weather_widget)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

