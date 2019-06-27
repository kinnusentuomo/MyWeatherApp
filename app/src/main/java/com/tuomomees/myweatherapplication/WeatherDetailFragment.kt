package com.tuomomees.myweatherapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


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

    val TAG = "WeatherDetailFragment"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var myWeatherDetailObject: MyWeatherDetailObject

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myWeatherDetailObject = MyWeatherDetailObject()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            myWeatherDetailObject = it.getParcelable("sentWeatherObject") as MyWeatherDetailObject

            Log.d(TAG, myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_weather_detail, container, false)

        Log.d(TAG, myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)

        updateFragment(myWeatherDetailObject, view)

        return  view
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeatherDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun updateFragment(myWeatherDetailObject: MyWeatherDetailObject, view: View){

        view.findViewById<TextView>(R.id.textViewWeather).text = myWeatherDetailObject.weather
        view.findViewById<TextView>(R.id.textViewCity).text = myWeatherDetailObject.cityName
        view.findViewById<TextView>(R.id.textViewTemperature).text = "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        view.findViewById<TextView>(R.id.textViewHumidity).text = myWeatherDetailObject.humidity.toString() + "%"
        view.findViewById<TextView>(R.id.textViewWindSpeed).text = myWeatherDetailObject.windSpeed.toString() + "m/s"

        when(myWeatherDetailObject.weather){
            "Clear" -> view.findViewById<ImageView>(R.id.imageViewWeatherIcon).setImageResource(R.drawable.ic_wb_sunny_white_24dp)
            "Clouds" -> view.findViewById<ImageView>(R.id.imageViewWeatherIcon).setImageResource(R.drawable.ic_cloud_white_24dp)
            "Rain" -> view.findViewById<ImageView>(R.id.imageViewWeatherIcon).setImageResource(R.drawable.ic_rain_white_24dp)
        }
    }
}
