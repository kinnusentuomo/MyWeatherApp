package com.tuomomees.myweatherapplication

class Helper(){

    fun getQueryStringCity(cityName: String): String {
        return "https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=$cityName"
    }

    fun getQueryStringLocation(lat: Double, lon: Double): String {
        return "https://api.apixu.com/v1/current.json?key=f24e5163a3664d16b8692210192507&q=$lat,$lon"
    }
}