package com.tuomomees.myweatherapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), WeatherDetailFragment.OnFragmentInteractionListener,
    MapViewFragment.OnFragmentInteractionListener, WeatherDetailGetterThread.ThreadReport,
    WeatherDetailListFragment.OnFragmentInteractionListener,
    TextWatcher {
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }


    override fun afterTextChanged(s: Editable?) {

    }

    val appId = "7ac8041476369264714a77f37e2f4141"
    private var TAG = "MainActivity"
    lateinit var toolbar: ActionBar
    private val LOCATION_REQUEST_CODE = 1706
    private lateinit var viewPager: androidx.viewpager.widget.ViewPager
    lateinit var fragmentList: ArrayList<androidx.fragment.app.Fragment>
    lateinit var weatherDetailObjectList: MutableList<MyWeatherDetailObject>
    private lateinit var lastLatLng: LatLng
    private lateinit var listFragment: WeatherDetailListFragment
    private lateinit var mapFragment: MapViewFragment

    private var notificationManager: NotificationManager? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var myEditTextCity: EditText

    //Application initialization when first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set edittext change listener which overrides functions



        myEditTextCity = findViewById(R.id.editTextCity)
        //editTextCity.addTextChangedListener(this)

        //init fusedLocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //init notification manager
        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        initActionBar()
        initFragments()
        initSettings()

        //Setup location permission
        setupPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE)



    }

    override fun onResume(){
        super.onResume()


        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val backgroundServiceEnabled = sharedPref.getBoolean("application_rain_notification", false)
        if(backgroundServiceEnabled && !isMyServiceRunning(UpdateWeatherService::class.java)){
            startService(Intent(this, UpdateWeatherService::class.java))
        }
        else{
            stopService(Intent(this, UpdateWeatherService::class.java))
        }
        Log.d(TAG, "Backgroundservice enabled: " + backgroundServiceEnabled)
    }

    private fun isMyServiceRunning(serviceClass:Class<*>):Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.name == service.service.className)
            {
                return true
            }
        }
        return false
    }

    private fun initSettings(){

        val defaultCity = getSharedPrefString("application_default_location")


        //SendQuery with default city when app starts
        if(defaultCity != "" && defaultCity != null){
            Log.d(TAG, "Default location set to: " + getSharedPrefString("application_default_location"))
            sendQueryWithCityString(defaultCity)
        }
    }

    private fun setupPermission(wantedPermission: String, requestCode: Int) {
        val permission = ContextCompat.checkSelfPermission(
            this,
            wantedPermission
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to $permission denied!")
            requestWantedPermission(wantedPermission, requestCode)
        } else {
            Log.i(TAG, "Permission to $permission granted!")
            getLastLocation()
            //startService(Intent(this, UpdateWeatherService::class.java))
        }
    }

    private fun requestWantedPermission(wantedPermission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(wantedPermission),
            requestCode
        )

        Log.d(TAG, "Requesting permission: $wantedPermission with code: $requestCode")
    }

    //this has to be implemented for some reason
    override fun onRequestPermissionsResult(requestCode: Any, permissions: Any, grantResults: Any) {
        Log.d(TAG, "onRequestPermissionResult")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission has been denied by user")
                    Toast.makeText(this, "Permission has to be granted for proper app usage.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Log.d(TAG, "Permission has been granted by user")
                    getLastLocation() //does not work when using virtual device

                }
            }
        }
    }

    //setup bottom navigation and set notification bar as transparent
    private fun initActionBar() {
        //supportActionBar?.hide()

        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar = supportActionBar!!


        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    //Add fragments to list and then visible to viewpager via adapter
    private fun initFragments() {
        viewPager = findViewById(R.id.viewPager)

        fragmentList = ArrayList()
        weatherDetailObjectList = ArrayList()

        listFragment = WeatherDetailListFragment()
        mapFragment = MapViewFragment()

        fragmentList.add(mapFragment)
        fragmentList.add(listFragment)

        viewPager.adapter = CustomViewPagerAdapter(supportFragmentManager, fragmentList)
    }

    override fun onFragmentInteraction(uri: Uri) {
        Log.d(TAG, uri.toString())
    }

    override fun addDataToList(myWeatherDetailObject: MyWeatherDetailObject) {

        weatherDetailObjectList.add(myWeatherDetailObject)

        try {
            val ft = supportFragmentManager.beginTransaction()
            ft.detach(listFragment)
            ft.attach(listFragment)
            ft.commit()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        viewPager.adapter?.notifyDataSetChanged()

        Log.d(TAG, "added fragment to list")
    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        if(myWeatherDetailObject.cityName != ""){
            val fragmentArgs = Bundle()
            fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

            val weatherDetailFragment = WeatherDetailFragment()
            weatherDetailFragment.arguments = fragmentArgs

            fragmentList.add(weatherDetailFragment)

            mapFragment.addMarkerWithDetails(myWeatherDetailObject)
            viewPager.adapter?.notifyDataSetChanged()
        }


        viewPagerProgressBar.visibility = View.INVISIBLE
    }

    /*
    fun createNotification(icon: Int, title: String, message: String) {

        val channelID = "weather notifications"
        createNotificationChannel(channelID, "tämän tuubin kautta ammutaan notifikaatioita", channelID)
        Log.d(TAG, message)

        val notification = Notification.Builder(applicationContext, channelID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setChannelId(channelID)
            .build()

        notificationManager?.notify(0, notification)
    }*/
/*
    private fun createNotificationChannel(name: String, description: String, id: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }*/

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {

                viewPager.currentItem = 0
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {

                viewPager.currentItem = 1
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_weather -> {

                viewPager.currentItem = 2
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    @SuppressLint("MissingPermission") //for some reason this moran IDE does not recognize my permission check done earlier....
    private fun getLastLocation() {

        var myLocation = Location("Washington")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {
                    myLocation = location
                }
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener {
                val queryString =
                    "https://api.openweathermap.org/data/2.5/weather?lat=" + myLocation.latitude + "&lon=" + myLocation.longitude + "&appid=" + appId
                val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)

                weatherDetailGetterThread.call()
                lastLatLng = LatLng(myLocation.latitude, myLocation.longitude)

                val intent = Intent(this, SimpleWeatherWidget::class.java)
                intent.putExtra("last_location", myLocation)
                sendBroadcast(intent)

                addSharedPref("last_location_lat", myLocation.latitude)
                addSharedPref("last_location_lon", myLocation.longitude)
            }
    }

    private fun addSharedPref(key: String, item: Double) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()

        editor.putFloat(key, item.toFloat())
        editor.apply()
    }

    fun getSharedPref(key: String): Float {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getFloat(key, 0.0f)
    }

    fun getSharedPrefString(key: String): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getString(key, "")
    }

    fun sendQueryWithCity(v: View) {

        viewPagerProgressBar.visibility = View.VISIBLE
        val queryString = "https://api.openweathermap.org/data/2.5/weather?q=" + myEditTextCity.text + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()
    }

    private fun sendQueryWithCityString(cityName: String){
        viewPagerProgressBar.visibility = View.VISIBLE
        val queryString = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()
    }

    fun sendQueryWithLocation(location: Location) {
        viewPagerProgressBar.visibility = View.VISIBLE
        val queryString =
            "https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()
    }


    fun goToSettings(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
