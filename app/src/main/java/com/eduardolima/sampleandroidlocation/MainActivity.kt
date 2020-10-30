package com.eduardolima.sampleandroidlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MY_LOCATION_CONFIG"
        val PERMISSION_ID = 42
    }

    lateinit var mFusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onResume() {
        super.onResume()
        verifyGooglePlayServices()
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latTextView.text = "Latitude: " + location.latitude.toString()
                        lonTextView.text = "Longitude: " + location.longitude.toString()
                        location?.let {
                            CoroutineScope(Dispatchers.Main).launch {

                                var addresses = listOf<Address>()

                                try {
                                    addresses = convertLocationAddress(
                                        location,
                                        Geocoder(this@MainActivity, Locale.getDefault())
                                    )
                                } catch (e: Exception) {
                                    Log.i(TAG, e.message.toString())
                                }

                                if (addresses == null || addresses.isEmpty()) {
                                    Log.i(TAG, "Nenhum endereço encontrado")
                                } else {
                                    addressTextView.text = "Endereço: " + addresses.get(0).getAddressLine(0)
                                    Log.i(TAG, addresses.toString())
                                }

                            }

                        }
                    }


                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }


    fun verifyGooglePlayServices() {

        val REQUEST_CODE = 0;

        val codeResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (codeResult != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance()
                .getErrorDialog(this, codeResult, REQUEST_CODE, DialogInterface.OnCancelListener {
                    finish();
                }).show()
        }

        when (codeResult) {
            ConnectionResult.SERVICE_MISSING -> Log.i(TAG, "Não tem o Google Play Services")
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> Log.i(
                TAG,
                "Precisa atualizar o Google Play Services"
            )
            ConnectionResult.SERVICE_MISSING -> Log.i(TAG, "O Google Play Services está desativado")
            ConnectionResult.SUCCESS -> {
                Log.i(TAG, "Google Play Services ok")
            }

        }
    }


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permissão para acessar Localização concedida")
            return true
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var result =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        if (result) {
            Log.i(TAG, "Localização do dispositivo ativada")
        } else {
            Log.i(TAG, "Localização do dispositivo desativada")
        }

        return result
    }

    private fun requestPermissions() {
        Log.i(TAG, "Requisitando permissão de acesso a localização")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.i(TAG, "Permissão para acessar Localização concedida")
            } else {
                finish()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 10



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            latTextView.text = "Latitude: " + mLastLocation.latitude.toString()
            lonTextView.text = "Longitude: " + mLastLocation.longitude.toString()

            Log.i(TAG, "Acuracia: " + mLastLocation.accuracy)
            Log.i(TAG, "Latitude: " + mLastLocation.latitude)
            Log.i(TAG, "Longetude: " + mLastLocation.longitude)

            mLastLocation?.let {


                CoroutineScope(Dispatchers.Main).launch {

                    var addresses = listOf<Address>()

                    try {

                        //bloco de execução em thread separada
                        addresses = convertLocationAddress(
                            mLastLocation,
                            Geocoder(this@MainActivity, Locale.getDefault())
                        )
                    } catch (e: Exception) {
                        Log.i(TAG, e.message.toString())
                    }

                    if (addresses == null || addresses.isEmpty()) {
                        Log.i(TAG, "Nenhum endereço encontrado")
                    } else {
                        Log.i(TAG, addresses.toString())
                        addressTextView.text = "Endereço: " + addresses.get(0).getAddressLine(0)
                    }


                }
            }


        }
    }


    suspend fun convertLocationAddress(location: Location, geocoder: Geocoder): List<Address> {

        return withContext(Dispatchers.Default) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)

        }
    }


}
