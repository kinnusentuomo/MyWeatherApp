package com.tuomomees.myweatherapplication

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MapViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MapViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MapViewFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    WeatherDetailGetterThread.ThreadReport {
    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {
        //(activity as MainActivity).weatherDetailObjectList.add(myWeatherDetailObject)
    }


    val appId = "7ac8041476369264714a77f37e2f4141"
    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {
        addMarkerWithDetails(myWeatherDetailObject)
    }

    override fun onMapLongClick(p0: LatLng) {

        /*
        mMap.addMarker(MarkerOptions()
            .position(p0)
            .title("Fetching weather information...")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))*/

        Log.d(TAG, p0.toString())

        val location = Location("")
        location.longitude = p0.longitude
        location.latitude = p0.latitude
        (activity as MainActivity).sendQueryWithLocation(location)
        queryWithLocation(location)
    }

    fun queryWithLocation(location: Location){
        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this.requireContext(), this)
        weatherDetailGetterThread.call()
    }


    fun addMarkerWithDetails(myWeatherDetailObject: MyWeatherDetailObject){


        val titleString = myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        val latLng = LatLng(myWeatherDetailObject.latitude, myWeatherDetailObject.longitude)
        mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title(titleString)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
    }


    val TAG = "MapViewFragment"
    private lateinit var mMap: GoogleMap


    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "Map ready")


        //val locationHandler = LocationHandler()
        //locationHandler.getLastLocation(this.requireContext(), Location("") -> moveCamera())


        mMap = p0

        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        val oulu = LatLng(65.01, 25.50)


        val currentLocation = LatLng((activity as MainActivity).getSharedPref("locationLatitude").toDouble(),
            (activity as MainActivity).getSharedPref("locationLongitude").toDouble()
        )

        //mMap.addMarker(MarkerOptions().position(oulu).title("Oulu"))
        //mMap.addMarker(MarkerOptions().position(currentLocation).title("Current location"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(oulu))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
    }


    fun moveCamera(latLng: LatLng = LatLng(0.0, 0.0)) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

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

        val myRootView = inflater.inflate(R.layout.fragment_map_view, container, false)

        //mapView.getMapAsync(this)
        // Inflate the layout for this fragment

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d(TAG, "onCreateView()")
        return myRootView
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
         * @return A new instance of fragment MapViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
