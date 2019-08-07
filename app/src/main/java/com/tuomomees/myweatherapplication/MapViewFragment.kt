package com.tuomomees.myweatherapplication

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import androidx.annotation.ColorInt
import android.util.TypedValue




class MapViewFragment : androidx.fragment.app.Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    WeatherDetailGetterThread.ThreadReport {

    private val TAG = "MapViewFragment"
    lateinit var mMap: GoogleMap
    lateinit var markerList: MutableList<Marker>

    val helper = Helper()


    //var unAddedMarkerBuffer: MutableList<MyWeatherDetailObject> = ArrayList()

    //Callback when thread ready
    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        //Set Progressbar invisible -> Job done no more waiting
        (activity as MainActivity).viewPagerProgressBar.visibility = View.INVISIBLE


        //Get color for current theme
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(R.attr.primaryTextColor, typedValue, true)
        @ColorInt val color = typedValue.data

        //AddMarker with data / Update marker with data by id
        markerList[markerId].title = myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        markerList[markerId].setIcon(bitmapDescriptorFromVector(this.requireContext(), myWeatherDetailObject.icon, color))

        //Add data to list (ListView)
        //(activity as MainActivity).addDataToList(myWeatherDetailObject)
        (activity as MainActivity).weatherDetailObjectList.add(myWeatherDetailObject)

        //Notify listview that data has changed
        (activity as MainActivity).listFragment.adapter.notifyDataSetChanged()

        //Create WeatherDetailFragment which holds just queried data
        val fragmentArgs = Bundle()
        fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

        val weatherDetailFragment = WeatherDetailFragment()
        weatherDetailFragment.arguments = fragmentArgs

        //Add new fragment to list so that adapter holds it and sets visible to ViewPager
        (activity as MainActivity).fragmentList.add(weatherDetailFragment)

        //Update Adapter -> notify adapter to update (ViewPager)
        (activity as MainActivity).viewPager.adapter?.notifyDataSetChanged()
    }

    override fun onMapLongClick(p0: LatLng) {

        val marker = mMap.addMarker(MarkerOptions()
            .position(p0)
            .title("Fetching weather information...")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        markerList.add(marker)

        val addedMarkerId = markerList.indexOf(marker)

        moveCamera(p0)

        val location = Location("")
        location.longitude = p0.longitude
        location.latitude = p0.latitude

        (activity as MainActivity).viewPagerProgressBar.visibility = View.VISIBLE
        Helper().sendQueryWithLocation(location, addedMarkerId, this.requireContext(), this as WeatherDetailGetterThread.ThreadReport)
    }

    fun addMarkerWithDetails(myWeatherDetailObject: MyWeatherDetailObject){

        val titleString = myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C"
        val latLng = LatLng(myWeatherDetailObject.latitude, myWeatherDetailObject.longitude)

        try{
            //Get color for current theme
            val typedValue = TypedValue()
            context?.theme?.resolveAttribute(R.attr.primaryTextColor, typedValue, true)
            @ColorInt val color = typedValue.data

                mMap.setOnMapLoadedCallback {
                    mMap.addMarker(MarkerOptions()
                        .position(latLng)
                        .title(titleString)
                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                        .icon(bitmapDescriptorFromVector(this.requireContext(), myWeatherDetailObject.icon, color)))
                    moveCamera(latLng)

                    Log.d(TAG, "Added marker to map: " + myWeatherDetailObject.cityName)
                }
            }
            catch (e: Exception){
                Log.e(TAG, e.toString())
                Log.d(TAG, "Could not add marker to map: " + myWeatherDetailObject.cityName)
                //unAddedMarkerBuffer.add(myWeatherDetailObject)

                helper.tryAgain ({ addMarkerWithDetails(myWeatherDetailObject)}, 3, TimeUnit.SECONDS.toMillis(1))
            }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, color: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        vectorDrawable.setTint(color) //set Tint to get different colored icon
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



        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
        val theme = sharedPref.getString("outfit_theme", "1")

        if(theme == "1"){
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
        }



        var myLocation = Location("")
        (activity as MainActivity).fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    myLocation = location
                }
            }

        (activity as MainActivity).fusedLocationClient.lastLocation.addOnCompleteListener{
            if(myLocation.latitude != 0.0 && myLocation.longitude != 0.0){
                moveCamera(LatLng(myLocation.latitude, myLocation.longitude))
                mMap.addMarker(MarkerOptions()
                    .position(LatLng(myLocation.latitude, myLocation.longitude))
                    .title("Current location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
            }
        }

/*
        //Adding not added markers from buffer
        unAddedMarkerBuffer.forEach{
            addMarkerWithDetails(it)
            Log.d(TAG, "Adding from buffer: " + it.cityName)
        }*/
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

    //For some reason onCreateView is called every time switching 2 pages @viewpager
    private var alreadyCalled: Boolean = false //TODO: fix this in a better way... original reason must be fixed
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myRootView = inflater.inflate(R.layout.fragment_map_view, container, false)
        if(!alreadyCalled){
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
            mapFragment.getMapAsync(this)
            alreadyCalled = true
        }

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
        fun newInstance() =
            MapViewFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

}
