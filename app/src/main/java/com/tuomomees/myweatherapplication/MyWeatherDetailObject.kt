package com.tuomomees.myweatherapplication

import android.os.Parcel
import android.os.Parcelable

class MyWeatherDetailObject() : Parcelable {
    var cityName: String = ""
    var humidity: Double = 0.0
    var temp_c: Double = 0.0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var weather: String = ""
    var windSpeed: Double = 0.0
    var icon: Int = R.drawable.ic_cloud_white_24dp

    constructor(parcel: Parcel) : this() {
        cityName = parcel.readString()?: ""
        humidity = parcel.readDouble()
        temp_c = parcel.readDouble()
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        weather = parcel.readString()?: ""
        windSpeed = parcel.readDouble()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cityName)
        parcel.writeDouble(humidity)
        parcel.writeDouble(temp_c)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(weather)
        parcel.writeDouble(windSpeed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyWeatherDetailObject> {
        override fun createFromParcel(parcel: Parcel): MyWeatherDetailObject {
            return MyWeatherDetailObject(parcel)
        }

        override fun newArray(size: Int): Array<MyWeatherDetailObject?> {
            return arrayOfNulls(size)
        }
    }
}