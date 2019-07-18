package com.tuomomees.myweatherapplication

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*


class MapViewFragment : androidx.fragment.app.Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    WeatherDetailGetterThread.ThreadReport {

    private val appId = "7ac8041476369264714a77f37e2f4141"
    private val TAG = "MapViewFragment"
    lateinit var mMap: GoogleMap
    lateinit var markerList: MutableList<Marker>

    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {
        //(activity as MainActivity).weatherDetailObjectList.add(myWeatherDetailObject)
    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        (activity as MainActivity).viewPagerProgressBar.visibility = View.INVISIBLE

        markerList[markerId].title = myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        markerList[markerId].setIcon(bitmapDescriptorFromVector(this.requireContext(), myWeatherDetailObject.icon))

        (activity as MainActivity).addDataToList(myWeatherDetailObject)

        val fragmentArgs = Bundle()
        fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

        val weatherDetailFragment = WeatherDetailFragment()
        weatherDetailFragment.arguments = fragmentArgs

        (activity as MainActivity).fragmentList.add(weatherDetailFragment)

        (activity as MainActivity).viewPager.adapter?.notifyDataSetChanged()
    }

    override fun onMapLongClick(p0: LatLng) {


        val marker = mMap.addMarker(MarkerOptions()
            .position(p0)
            .title("Fetching weather information...")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))


        markerList.add(marker)

        val addedMarkerId = markerList.indexOf(marker)

        Log.d(TAG, "marker id : " + addedMarkerId)

        moveCamera(p0)


        Log.d(TAG, p0.toString())

        val location = Location("")
        location.longitude = p0.longitude
        location.latitude = p0.latitude
        //(activity as MainActivity).sendQueryWithLocation(location)
        queryWithLocation(location, addedMarkerId)
    }

    private fun queryWithLocation(location: Location, markerId: Int){
        (activity as MainActivity).viewPagerProgressBar.visibility = View.VISIBLE
        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this.requireContext(), this, markerId)
        weatherDetailGetterThread.call()
    }

    fun addMarkerWithDetails(myWeatherDetailObject: MyWeatherDetailObject){


        val titleString = myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        val latLng = LatLng(myWeatherDetailObject.latitude, myWeatherDetailObject.longitude)

            try{
                mMap.setOnMapLoadedCallback {
                    mMap.addMarker(MarkerOptions()
                        .position(latLng)
                        .title(titleString)
                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                        .icon(bitmapDescriptorFromVector(this.requireContext(), myWeatherDetailObject.icon)))
                    moveCamera(latLng)
                }
            }
            catch (e: Exception){
                Log.e(TAG, e.toString())
            }



    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "Map ready")

        mMap = p0

        mMap.setOnMapLongClickListener(this)
        markerList = ArrayList()


        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(), R.raw.style_json
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        var myLocation: Location = Location("")
        (activity as MainActivity).fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    myLocation = location
                }
            }

        (activity as MainActivity).fusedLocationClient.lastLocation.addOnCompleteListener{
            moveCamera(LatLng(myLocation.latitude, myLocation.longitude))
            mMap.addMarker(MarkerOptions()
                .position(LatLng(myLocation.latitude, myLocation.longitude))
                .title("Current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
        }
    }


    private fun moveCamera(latLng: LatLng = LatLng(0.0, 0.0)) {
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }


    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myRootView = inflater.inflate(R.layout.fragment_map_view, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Log.d(TAG, "onCreateView()")
        return myRootView
    }

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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapViewFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
