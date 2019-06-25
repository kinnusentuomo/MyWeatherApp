package com.tuomomees.myweatherapplication

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.app.Fragment


class MainActivity : AppCompatActivity(), WeatherDetailFragment.OnFragmentInteractionListener{
    override fun onFragmentInteraction(uri: Uri) {

    }

    private val RECORD_REQUEST_CODE = 101

    private lateinit var viewPager: ViewPager


    private lateinit var fragmentList: ArrayList<Fragment>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewPager)

        //Initialize Viewpager

        fragmentList = ArrayList()
        fragmentList.add(WeatherDetailFragment())
        fragmentList.add(WeatherDetailFragment())


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

    fun sendQueryWithCity(v: View){
        val fragment = fragmentList.get(0) as WeatherDetailFragment
        fragment.getWeatherDataJson("city", city=editTextCity.text.toString())
    }
}
