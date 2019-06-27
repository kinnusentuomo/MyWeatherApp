package com.tuomomees.myweatherapplication

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class WeatherDetailGetterThread(private var queryString: String, private var context: Context, private var threadObserver: ThreadReport): Thread(),
    Callable<MyWeatherDetailObject> {



    interface ThreadReport {
        fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject)
    }


    val TAG = "WeatherDetailThread"

    private var running = true
    private var gettingData = false
    lateinit var myWeatherDetailObject: MyWeatherDetailObject

    override fun call(): MyWeatherDetailObject {
        myWeatherDetailObject = MyWeatherDetailObject()
        while(running){

            if(!gettingData){
                getData()
            }

            //Thread done -> stop loop -> return data
            running = false
            Log.d(TAG, "running: " + running)

        }
        Log.d(TAG, "Returning: " + myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)
        return myWeatherDetailObject
    }


    fun stopThread(stop: Boolean){
        running = stop
    }

    fun getData() {
        val myConnection = URL(queryString).openConnection() as HttpsURLConnection
        val myWeatherDetailObject = MyWeatherDetailObject()

            //        if (myConnection.responseCode == 200) {
            val queue = Volley.newRequestQueue(context)

            val stringRequest = StringRequest(
                Request.Method.GET, queryString, Response.Listener<String> { response ->

                    val jsonObject = JSONObject(response)

                    val weatherBlock = jsonObject.getJSONArray("weather")
                    val coordBlock = jsonObject.getJSONObject("coord")
                    val mainBlock = jsonObject.getJSONObject("main")
                    val windBlock = jsonObject.getJSONObject("wind")

                    val temp_k = mainBlock.getString("temp").toDouble()
                    val temp_c = temp_k - 273.15
                    val cityName = jsonObject.get("name")
                    var weather = ""

                    val lat = coordBlock.getDouble("lat")
                    val lon = coordBlock.getDouble("lon")

                    val windSpeed = windBlock.getString("speed")
                    val humidity = mainBlock.getString("humidity")

                    for (i in 0..(weatherBlock.length() - 1)) {
                        val item = weatherBlock.getJSONObject(i)

                        weather = item.get("main").toString()
                    }


                    myWeatherDetailObject.humidity = humidity.toDouble()
                    myWeatherDetailObject.temp_c = temp_c
                    myWeatherDetailObject.weather = weather
                    myWeatherDetailObject.windSpeed = windSpeed.toDouble()
                    myWeatherDetailObject.latitude = lat
                    myWeatherDetailObject.longitude = lon
                    myWeatherDetailObject.cityName = cityName.toString()


                    Log.d(TAG, myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)
                    stopThread(true)
                    gettingData = true

                    threadObserver.ThreadReady(myWeatherDetailObject)

                }, Response.ErrorListener { stopThread(true)})
            queue.add(stringRequest)
            //       }

    }
}


