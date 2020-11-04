package com.eduardolima.sampleandroidlocation

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationCallback

interface interfaceLastLocation {

    fun verifyGooglePlayServices()

    @SuppressLint("MissingPermission")
    fun getLastLocation(resutCallbeck: (result: resultLocatioinRequest) -> Unit)
}