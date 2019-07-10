package com.tuomomees.myweatherapplication

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream


class MainActivity : AppCompatActivity(), WeatherDetailFragment.OnFragmentInteractionListener, MapViewFragment.OnFragmentInteractionListener, WeatherDetailGetterThread.ThreadReport, WeatherDetailListFragment.OnFragmentInteractionListener,
    TextWatcher {
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

    }

    fun getJsonFileWithFileName(fileName: String){
        try {
            val inputStream: InputStream = assets.open("$fileName.json")
            val inputString = inputStream.bufferedReader().use{it.readText()}
            Log.d(TAG,inputString)
        } catch (e:Exception){
            Log.d(TAG, e.toString())
        }
    }


    val appId = "7ac8041476369264714a77f37e2f4141"
    private var TAG = "MainActivity"
    lateinit var toolbar: ActionBar
    private val LOCATION_REQUEST_CODE = 1706
    private lateinit var viewPager: ViewPager
    private lateinit var fragmentList: ArrayList<Fragment>
    lateinit var weatherDetailObjectList: MutableList<MyWeatherDetailObject>
    lateinit var lastLatLng: LatLng
    lateinit var listFragment: WeatherDetailListFragment
    lateinit var mapFragment: MapViewFragment

    private var notificationManager: NotificationManager? = null





    //Application initialization when first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //viewPagerProgressBar.progressDrawable = getDrawable(R.drawable.custom_progress_bar)



        getJsonFileWithFileName("city_list")

        editTextCity.addTextChangedListener(this)

        //init fusedLocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

        initActionBar()

        //getLastLocation() //does not work when using virtual device
        initFragments()

        //Setup location permission
        setupPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE)
    }

    private fun setupPermission(wantedPermission: String, requestCode: Int) {
        val permission = ContextCompat.checkSelfPermission(this,
            wantedPermission)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to " + permission + " denied!")
            requestWantedPermission(wantedPermission, requestCode)
        }
        else{
            Log.i(TAG, "Permission to " + permission + " granted!")
            getLastLocation()
            startService(Intent(this, UpdateWeatherService::class.java))
        }
    }

    private fun requestWantedPermission(wantedPermission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this,
            arrayOf(wantedPermission),
            requestCode)

        Log.d(TAG, "Requesting permission: " + wantedPermission + " with code: " + requestCode)

    }

    override fun onRequestPermissionsResult(requestCode: Any, permissions: Any, grantResults: Any) {

        Log.d(TAG, "onRequestPermissionResult")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        Log.d(TAG, "onRequestPermissionResult")
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
    private fun initActionBar(){
        supportActionBar?.hide()
        toolbar = supportActionBar!!

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    //Add fragments to list and then visible to viewpager via adapter
    private fun initFragments(){
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
/*

        val weatherDetailListFragment = WeatherDetailListFragment()
        val fragmentArgs = Bundle()

        fragmentArgs.putParcelableArray("sentWeatherDetailObjectList", weatherDetailObjectList.toTypedArray())

        weatherDetailListFragment.arguments = fragmentArgs

        viewPager.adapter?.notifyDataSetChanged()
*/
        try{
            val ft = supportFragmentManager.beginTransaction()
            ft.detach(listFragment)
            ft.attach(listFragment)
            ft.commit()
        }
        catch(e: Exception){
            Log.e(TAG, e.toString())
        }

        viewPager.adapter?.notifyDataSetChanged()

        Log.d(TAG, "added fragment to list")
    }

    override fun ThreadReady(myWeatherDetailObject: MyWeatherDetailObject) {
        val fragmentArgs = Bundle()
        fragmentArgs.putParcelable("sentWeatherObject", myWeatherDetailObject)

        //createNotification(myWeatherDetailObject.icon, myWeatherDetailObject.cityName, "%.0f".format(myWeatherDetailObject.temp_c) + "°C")

        //Log.d("Main", myWeatherDetailObject.cityName + " " + myWeatherDetailObject.temp_c)

        val weatherDetailFragment = WeatherDetailFragment()
        weatherDetailFragment.arguments = fragmentArgs

        fragmentList.add(weatherDetailFragment)

        mapFragment.addMarkerWithDetails(myWeatherDetailObject)
        viewPager.adapter?.notifyDataSetChanged()

        viewPagerProgressBar.visibility = View.INVISIBLE
    }

    fun createNotification(icon: Int, title: String, message: String){

        val channelID = "weather notifications"
        createNotificationChannel(channelID, "tämän tuubin kautta ammutaan notifikaatioita", channelID)
        Log.d(TAG, message)

        val notification = Notification.Builder(applicationContext,
            channelID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setChannelId(channelID)
            .build()

        notificationManager?.notify(0, notification)
    }

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
    }

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

    fun getFragmentWithTag(tag: String): Fragment {
        for (fragment in fragmentList) {
            if(fragment.tag == tag){
                return fragment
            }
        }
        return fragmentList.get(0)
    }

    //Added
    lateinit var fusedLocationClient: FusedLocationProviderClient
    fun getLastLocation(){

        var myLocation: Location = Location("Washington")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location ->
                //Log.d("LastLongitude", location.longitude.toString())
                //Log.d("LastLatitude", location.latitude.toString())



                myLocation = location
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{
                val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + myLocation.latitude + "&lon=" + myLocation.longitude + "&appid=" + appId
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
             LOCATION_REQUEST_CODE)
    }

    fun addSharedPref(key: String, item: Double){
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()

        editor.putFloat(key, item.toFloat())
        editor.apply()
    }

    fun getSharedPref(key: String): Float {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getFloat(key, 0.0f)
    }

    fun sendQueryWithCity(v: View){

        viewPagerProgressBar.visibility = View.VISIBLE
        val queryString = "https://api.openweathermap.org/data/2.5/weather?q=" + editTextCity.text + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()
    }

    fun sendQueryWithLocation(location: Location){
        //val fragment = fragmentList.get(0) as WeatherDetailFragment
        //fragment.getWeatherDataJson("gps", location)


        viewPagerProgressBar.visibility = View.VISIBLE
        val queryString = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + appId
        val weatherDetailGetterThread = WeatherDetailGetterThread(queryString, this, this)
        weatherDetailGetterThread.call()
    }


    fun addMarker(myWeatherDetailObject: MyWeatherDetailObject){
        mapFragment.addMarkerWithDetails(myWeatherDetailObject)
    }
}
