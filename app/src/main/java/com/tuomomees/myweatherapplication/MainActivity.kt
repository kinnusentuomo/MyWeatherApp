package com.tuomomees.myweatherapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity(){



    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
    }

    fun getLastLocation(): Location {

        var returnLastLocation = Location("Washington")

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location ->
                Log.d("LastLongitude", location?.longitude.toString())
                Log.d("LastLatitude", location?.latitude.toString())
                returnLastLocation = location
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{
                getWeatherDataJson("gps", returnLastLocation)
            }


        return returnLastLocation
    }





    fun hasPermission(permission: String): Boolean {

        val wantedPermission = ContextCompat.checkSelfPermission(this, permission)
        var permissionGranted = false
        if (wantedPermission == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        }

        return permissionGranted
    }


    fun requestPermission(permission: String){

         ActivityCompat.requestPermissions(this,
                        arrayOf(permission),
                        RECORD_REQUEST_CODE)
    }


    fun addSharedPref(key: String, item: String){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()

        editor.putString(key, item)

        editor.apply()
    }


    fun getSharedPref(key: String): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getString(key, "")
    }


    fun getWeatherDataJson(type: String, location: Location){

        val appId = "7ac8041476369264714a77f37e2f4141"
        val cityString = editTextCity.text





/*
        val queryString: URL
        when(type) {
            "city" ->  queryString = URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityString + "&appid=" + appId)
            "gps" -> queryString = URL("api.openweathermap.org/data/2.5/weather?lat="+ getLat() +"&lon=" + getLon())
        }


        addSharedPref("type", type)*/


        AsyncTask.execute {



            var queryString = URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityString + "&appid=" + appId)
            when(type) {
                "city" ->  queryString = URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityString + "&appid=" + appId)
                "gps" ->  queryString = URL("https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId)
            }


            Log.d("Query", queryString.toString())

            // All your networking logic
            // should be here

            // Create URL

            //val queryString = URL("https://api.openweathermap.org/data/2.5/weather?q=" + cityString + "&appid=" + appId)

            // Create connection
            val myConnection = queryString.openConnection() as HttpsURLConnection


            if (myConnection.responseCode == 200) {
                // Success
                // Further processing here
                val queue = Volley.newRequestQueue(this)

                val stringRequest = StringRequest(
                    Request.Method.GET, queryString.toString(), Response.Listener<String> { response ->
                        val jsonObject = JSONObject(response)

                        val weatherBlock = jsonObject.getJSONArray("weather")
                        val mainBlock = jsonObject.getJSONObject("main")
                        val windBlock = jsonObject.getJSONObject("wind")

                        val temp_k = mainBlock.getString("temp").toDouble()
                        val temp_c = temp_k - 273.15
                        val cityName = jsonObject.get("name")
                        var weather = ""


                        var windSpeed = windBlock.getString("speed")
                        var humidity = mainBlock.getString("humidity")

                        for (i in 0..(weatherBlock.length() - 1)) {
                            val item = weatherBlock.getJSONObject(i)

                            weather = item.get("main").toString()
                        }

                        when(weather){
                            "Clear" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_wb_sunny_white_24dp)
                            "Cloudy" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_cloud_white_24dp)
                        }

                        runOnUiThread {
                            textViewWeather.text = weather
                            textViewCity.text = cityName.toString()
                            textViewTemperature.text = "%.0f".format(temp_c) + "°C"
                            textViewHumidity.text = humidity + "%"
                            textViewWindSpeed.text = windSpeed + "m/s"
                        }

                        Log.d("Request", "ready")

                    }, Response.ErrorListener { })
                queue.add(stringRequest)
            }
        }
    }


    fun sendQueryWithCity(v: View){
        getWeatherDataJson("city", Location("Washington"))
    }
}
