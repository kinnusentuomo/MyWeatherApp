package com.tuomomees.myweatherapplication

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private var TAG = "MainActivity"
    private val LOCATION_REQUEST_CODE = 1706
    private lateinit var viewPager: androidx.viewpager.widget.ViewPager
    lateinit var fragmentList: ArrayList<androidx.fragment.app.Fragment>
    lateinit var weatherDetailObjectList: MutableList<MyWeatherDetailObject>
    //private lateinit var lastLatLng: LatLng
    lateinit var listFragment: WeatherDetailListFragment
    private lateinit var mapFragment: MapViewFragment

    private var notificationManager: NotificationManager? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var myEditTextCity: EditText

    //Application initialization when first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        initWidget()

        //Setup location permission
        setupPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE)
    }

    override fun onResume(){
        super.onResume()


        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val backgroundServiceEnabled = sharedPref.getBoolean("application_rain_notification", false)
        if(backgroundServiceEnabled && !isMyServiceRunning(UpdateWeatherService::class.java)){
            //startService(Intent(this, UpdateWeatherService::class.java))
            //bindService(this, UpdateWeatherService::class.java)
            Intent(this, UpdateWeatherService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        if(!backgroundServiceEnabled && isMyServiceRunning(UpdateWeatherService::class.java)){
            stopService(Intent(this, UpdateWeatherService::class.java))
        }
        else{

        }
        Log.d(TAG, "Backgroundservice enabled: " + backgroundServiceEnabled)
    }


    private lateinit var mService: UpdateWeatherService
    private var mBound: Boolean = false


    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
           // val binder = service as LocalService.LocalBinder
           // mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
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
            viewPagerProgressBar.visibility = View.VISIBLE
            Helper().sendQueryWithCityString(defaultCity, this)
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
                    //Toast.makeText(this, "Permission has to be granted for proper app usage.", Toast.LENGTH_LONG).show()

                    //If location permission not granted, create dialog to inform user to grant it
                    val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                    alertDialog.setTitle("Are you sure?")
                    alertDialog.setMessage("Permission has to be granted for proper app usage.")
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Try again"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        setupPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE)
                    }
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Close the app"
                    ) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    alertDialog.show()

                    //finish()
                } else {
                    Log.d(TAG, "Permission has been granted by user")
                    getLastLocation() //does not work when using virtual device
                }
            }
        }
    }

    //setup bottom navigation and set notification bar as transparent
    private fun initActionBar() {

        window.navigationBarColor = resources.getColor(R.color.cardview_dark_background)
        supportActionBar?.hide()
        window.statusBarColor = resources.getColor(R.color.cardview_dark_background)
        setSupportActionBar(findViewById(R.id.toolbar))

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

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject, markerId: Int) {

        //Set Progressbar invisible -> Job done no more waiting
        viewPagerProgressBar.visibility = View.INVISIBLE

        //prevent nameless data
        if(myWeatherDetailObject.cityName != ""){
            //Add marker to MapView
            mapFragment.addMarkerWithDetails(myWeatherDetailObject)

            //Add data item to list
            weatherDetailObjectList.add(myWeatherDetailObject)

            //Notify listview that data has changed
            listFragment.adapter.notifyDataSetChanged()

            //Create detail fragment
            val fragmentArgs = Bundle()
            fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

            val weatherDetailFragment = WeatherDetailFragment()
            weatherDetailFragment.arguments = fragmentArgs

            //Add detail fragment to list
            fragmentList.add(weatherDetailFragment)

            //Update adapter
            viewPager.adapter?.notifyDataSetChanged()
        }
    }

    private fun getLastLocation() {

        var myLocation = Location("")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {
                    myLocation = location
                }
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener {
                Helper().sendQueryWithLocation(myLocation, 0, this, this)
            }
    }


    private fun initWidget(){
        val intent = Intent(this, SimpleWeatherWidget::class.java)
        sendBroadcast(intent)
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

    //UI INTERACTIONS
    fun onSearchButtonClicked(v: View){
        viewPagerProgressBar.visibility = View.VISIBLE
        Helper().sendQueryWithCityString(myEditTextCity.text.toString(), this)
    }

    fun goToSettings(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    //Bottom navigation bar
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
}
