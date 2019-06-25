package com.tuomomees.myweatherapplication

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_weather_detail.*
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WeatherDetailFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WeatherDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WeatherDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null



    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_weather_detail, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())

        getLastLocation()

        return  view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeatherDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeatherDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //Added
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

    fun getWeatherDataJson(type: String, location: Location = Location("Washington"), city: String = ""){

        val appId = "7ac8041476369264714a77f37e2f4141"

        val cityString = city

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
                val queue = Volley.newRequestQueue(this.requireContext())

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


                        val windSpeed = windBlock.getString("speed")
                        val humidity = mainBlock.getString("humidity")

                        for (i in 0..(weatherBlock.length() - 1)) {
                            val item = weatherBlock.getJSONObject(i)

                            weather = item.get("main").toString()
                        }

                        when(weather){
                            "Clear" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_wb_sunny_white_24dp)
                            "Clouds" -> imageViewWeatherIcon.setImageResource(R.drawable.ic_cloud_white_24dp)
                        }

                        this.activity!!.runOnUiThread {
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
}
