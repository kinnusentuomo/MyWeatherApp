package com.tuomomees.myweatherapplication

import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), WeatherDetailFragment.OnFragmentInteractionListener, MapViewFragment.OnFragmentInteractionListener, WeatherDetailGetterThread.ThreadReport{


    val appId = "7ac8041476369264714a77f37e2f4141"


    lateinit var toolbar: ActionBar

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {
        val fragmentArgs = Bundle()
        fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

        Log.d("Main", myWeatherDetailObject.cityName + myWeatherDetailObject.temp_c)

        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)



        val weatherDetailFragment = WeatherDetailFragment()
        weatherDetailFragment.arguments = fragmentArgs
        fragmentList.add(weatherDetailFragment)

        viewPager.adapter?.notifyDataSetChanged()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {


                viewPager.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_weather -> {

                viewPager.currentItem = 1
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }



    private val RECORD_REQUEST_CODE = 101
    private lateinit var viewPager: ViewPager
    private lateinit var fragmentList: ArrayList<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        viewPager = findViewById(R.id.viewPager)

        fragmentList = ArrayList()
        //fragmentList.add(WeatherDetailFragment())
        fragmentList.add(MapViewFragment())

        viewPager.adapter = CustomViewPagerAdapter(supportFragmentManager, fragmentList)
    }




    //Added
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    fun getLastLocation(){



        var myLocation: Location = Location("Washington")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location ->
                Log.d("LastLongitude", location.longitude.toString())
                Log.d("LastLatitude", location.latitude.toString())
                myLocation = location
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{

                val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + myLocation.latitude + "&lon=" + myLocation.longitude + "&appid=" + appId
                val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
                weatherDetailGetterThread.call()
            }
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

    fun addSharedPref(key: String, item: Float){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()

        editor.putFloat(key, item)
        editor.apply()
    }

    fun getSharedPref(key: String): Float {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getFloat(key, 0.0f)
    }

    fun sendQueryWithCity(v: View){
        //val fragment = fragmentList.get(0) as WeatherDetailFragment
        //fragment.getWeatherDataJson("city", city=editTextCity.text.toString())
    }

    fun sendQueryWithLocation(location: Location){
        //val fragment = fragmentList.get(0) as WeatherDetailFragment
        //fragment.getWeatherDataJson("gps", location)


        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()

    }

    fun addMarker(myWeatherDetailObject: MyWeatherDetailObject){

        val fragment = fragmentList.get(1) as MapViewFragment
        fragment.addMarkerWithDetails(myWeatherDetailObject)
    }
}
