package com.tuomomees.myweatherapplication

import android.content.Context
import android.location.Location
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class Helper {

    fun getQueryStringCity(cityName: String): String {
        return "https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=$cityName"
    }

    fun getQueryStringLocation(lat: Double, lon: Double): String {
        return "https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=$lat,$lon"
    }

    fun getQueryStringCityWeekly(cityName: String): String {
        return "http://api.apixu.com/v1/forecast.json?key=f24e5163a3664d16b8692210192507&q=$cityName&days=7"
    }

    fun sendQueryWithCityString(cityName: String, context: Context){
        val queryString = Helper().getQueryStringCity(cityName)
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, context as WeatherDetailGetterThread.ThreadReport)
        weatherDetailGetterThread.call()
    }

    fun queryWithLocation(location: Location, markerId: Int, context: Context, threadObserver: WeatherDetailGetterThread.ThreadReport){
        val queryString = Helper().getQueryStringLocation(location.latitude, location.longitude)
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, context, threadObserver as WeatherDetailGetterThread.ThreadReport, markerId)
        weatherDetailGetterThread.call()
    }
}