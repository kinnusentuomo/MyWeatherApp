package com.tuomomees.myweatherapplication


import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.Callable


class WeatherDetailGetterThread(private var queryString: String, private var context: Context, private var threadObserver: ThreadReport, var markerId: Int = 0): Thread(),
    Callable<MyWeatherDetailObject> {

    interface ThreadReport {
        fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int)
        fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject)
    }

    val TAG = "WeatherDetailThread"

    private var running = true
    private var gettingData = false
    lateinit var myWeatherDetailObject: MyWeatherDetailObject

    override fun call(): MyWeatherDetailObject {
        Log.d(TAG, "Caller: " + context)

        myWeatherDetailObject = MyWeatherDetailObject()
        while(running){

            if(!gettingData){
                getDataFromApixu() //Apixu API
            }

            //Thread done -> stop loop -> return data
            running = false
        }

        return myWeatherDetailObject
    }


    private fun stopThread(stop: Boolean){
        running = stop
    }

    fun getDataFromOpenWeatherMap() {
        //val myConnection = URL(queryString).openConnection() as HttpsURLConnection
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


                    if(myWeatherDetailObject.cityName == ""){
                        myWeatherDetailObject.cityName = "lat " + lat.toString() + "° lon " + lon.toString() + "°"
                    }


                    var icon: Int = R.drawable.ic_cloud_white_24dp

                    when(weather){
                        "Clouds" -> icon = R.drawable.ic_cloud_white_24dp
                        "Clear" -> icon = R.drawable.ic_wb_sunny_white_24dp
                        "Rain" -> icon = R.drawable.ic_rain_white_24dp
                    }

                    myWeatherDetailObject.icon = icon

                    Log.d(TAG, myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)
                    stopThread(true)
                    gettingData = true

                    threadObserver.ThreadReady(myWeatherDetailObject, markerId)
                    threadObserver.addDataToList(myWeatherDetailObject)

                }, Response.ErrorListener {
                    Toast.makeText(context, "Could not find data with given city name, please try again." , Toast.LENGTH_SHORT).show()
                    threadObserver.ThreadReady(myWeatherDetailObject, markerId)
                    stopThread(true)})
            queue.add(stringRequest)
            //       }

    }

    private fun getDataFromApixu(){
            //Lat Lon
            //https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=48.8567,2.3508
            //City
            //https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=Paris
        //val myConnection = URL(queryString).openConnection() as HttpsURLConnection

        Log.d(TAG, queryString)
        val myWeatherDetailObject = MyWeatherDetailObject()

        //        if (myConnection.responseCode == 200) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest = StringRequest(
            Request.Method.GET, queryString, Response.Listener<String> { response ->

                val jsonObject = JSONObject(response)

                val locationBlock = jsonObject.getJSONObject("location")
                val currentBlock = jsonObject.getJSONObject("current")
                val conditionBlock = currentBlock.getJSONObject("condition")

                val cityName = locationBlock.getString("name")
                val cityCountry = locationBlock.getString("country")
                val temp_c = currentBlock.getDouble("temp_c")
                val temp_f = currentBlock.getDouble("temp_f")
                val wind_kph = currentBlock.getDouble("wind_kph")
                val uv_index = currentBlock.getDouble("uv")
                val humidity = currentBlock.getDouble("humidity")
                val weather = conditionBlock.getString("text")


                val lat = locationBlock.getDouble("lat")
                val lon = locationBlock.getDouble("lon")


                myWeatherDetailObject.humidity = humidity.toDouble()
                myWeatherDetailObject.temp_c = temp_c
                myWeatherDetailObject.weather = weather
                myWeatherDetailObject.windSpeed = (wind_kph / 3.6)
                myWeatherDetailObject.latitude = lat
                myWeatherDetailObject.longitude = lon
                myWeatherDetailObject.cityName = cityName.toString()


                if(myWeatherDetailObject.cityName == ""){
                    myWeatherDetailObject.cityName = "lat " + lat.toString() + "° lon " + lon.toString() + "°"
                }


                var icon: Int = R.drawable.ic_cloud_white_24dp

                when(weather){
                    "Cloudy", "Overcast" -> icon = R.drawable.ic_cloud_white_24dp
                    "Partly cloudy" -> icon = R.drawable.ic_cloud_white_24dp
                    "Sunny" -> icon = R.drawable.ic_wb_sunny_white_24dp
                    "Rain", "Light drizzle",
                    "Light rain shower"  -> icon = R.drawable.ic_rain_white_24dp
                }

                myWeatherDetailObject.icon = icon

                Log.d(TAG, myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)
                stopThread(true)
                gettingData = true

                threadObserver.ThreadReady(myWeatherDetailObject, markerId)
                threadObserver.addDataToList(myWeatherDetailObject)

            }, Response.ErrorListener {
                Toast.makeText(context, "Could not find data with given city name, please try again." , Toast.LENGTH_SHORT).show()
                threadObserver.ThreadReady(myWeatherDetailObject, markerId)
                stopThread(true)})
        queue.add(stringRequest)
    }
}



