package com.tuomomees.myweatherapplication

import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), WeatherDetailFragment.OnFragmentInteractionListener, MapViewFragment.OnFragmentInteractionListener,
    OnMapReadyCallback {
    override fun onMapReady(p0: GoogleMap?) {


    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    private val RECORD_REQUEST_CODE = 101
    private lateinit var viewPager: ViewPager
    private lateinit var fragmentList: ArrayList<Fragment>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewPager)

        fragmentList = ArrayList()
        fragmentList.add(WeatherDetailFragment())
        fragmentList.add(MapViewFragment())

        viewPager.adapter = CustomViewPagerAdapter(supportFragmentManager, fragmentList)
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
        val fragment = fragmentList.get(0) as WeatherDetailFragment
        fragment.getWeatherDataJson("city", city=editTextCity.text.toString())
    }

    fun sendQueryWithLocation(location: Location){
        val fragment = fragmentList.get(0) as WeatherDetailFragment
        fragment.getWeatherDataJson("gps", location)
    }
}
