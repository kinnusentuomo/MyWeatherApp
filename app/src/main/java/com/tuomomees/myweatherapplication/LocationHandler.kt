package com.tuomomees.myweatherapplication

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHandler {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Added
    fun getLastLocation(context: Context, callback: (param: Location) -> Unit) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        var returnLastLocation = Location("Washington")

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location ->
                Log.d("LastLongitude", location.longitude.toString())
                Log.d("LastLatitude", location.latitude.toString())
                returnLastLocation = location
            }

        fusedLocationClient.lastLocation
            .addOnCompleteListener{
                callback(returnLastLocation)
            }
    }
}