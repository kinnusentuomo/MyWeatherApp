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


        views = RemoteViews(context.packageName, R.layout.simple_weather_widget)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                lastLocation = location ?: Location("Oulu")

            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{

                // There may be multiple widgets active, so update all of them
                for (appWidgetId in appWidgetIds) {
                    Log.w(TAG, "onUpdate method called")
                    Log.d(TAG, "last Location " + lastLocation)
                    updateAppWidget(context, appWidgetManager, appWidgetId, lastLocation)
                }
            }
        /*
        for (appWidgetId in appWidgetIds) {
            Log.w(TAG, "onUpdate method called")
            updateAppWidget(context, appWidgetManager, appWidgetId, Location("Washington"))
        }*/

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

        override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {
            Log.d("ThreadReadyWidget", "Data: " + myWeatherDetailObject.cityName)
            //views = RemoteViews("com.tuomomees.myweatherapplication", R.layout.simple_weather_widget)


            if(myWeatherDetailObject.cityName != ""){
                //update texts
                views.setTextViewText(R.id.appwidget_text_cityname, myWeatherDetailObject.cityName)
                views.setTextViewText(R.id.appwidget_text_temp_c, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
                views.setTextViewText(R.id.appwidget_text_humidity, myWeatherDetailObject.humidity.toString() + "%")
                views.setTextViewText(R.id.appwidget_text_wind_speed, myWeatherDetailObject.windSpeed.toString() + "m/s")



                //views.setTextViewText(R.id.appwidget_text_wind_speed, "ASDASDASDASD")
                //update icon
                views.setImageViewResource(R.id.imageViewWidget, myWeatherDetailObject.icon)

                //notify widget manager to update widget
                myAppWidgetManager.updateAppWidget(myWidgetId, views)
            }
            else{
                //try again
                Log.e("WeatherWidget Thread", "Thread returned no data, cityname: " + myWeatherDetailObject.cityName)
            }
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
            myWidgetId = appWidgetId
            myAppWidgetManager = appWidgetManager

            // Instruct the widget manager to update the widget


            val lat = getSharedPref("last_location_lat", context)
            val lon = getSharedPref("last_location_lon", context)

            Log.d("updateWidget", lastLocation.toString())
            //val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appId
            val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + lastLocation.latitude + "&lon=" + lastLocation.longitude + "&appid=" + appId
            //val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + 65.0 + "&lon=" + 25.0 + "&appid=" + appId //Hailuoto
            val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, this)
            weatherDetailGetterThread.call()

            //views.setTextViewText(R.id.appwidget_text_wind_speed, "Tervepä terve")
            views = RemoteViews(context.packageName, R.layout.simple_weather_widget)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

