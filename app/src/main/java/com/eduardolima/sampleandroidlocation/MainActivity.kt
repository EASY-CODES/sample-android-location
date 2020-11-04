package com.eduardolima.sampleandroidlocation

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.eduardolima.sampleandroidlocation.getMyLastLocation.Companion.PERMISSION_ID
import com.eduardolima.sampleandroidlocation.getMyLastLocation.Companion.TAG
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()


        val gtml = getMyLastLocation(this)
        gtml.verifyGooglePlayServices()
        gtml.getLastLocation { result: resultLocatioinRequest ->

            when (result) {

                is resultLocatioinRequest.Sucesso -> {
                    val address = (result as resultLocatioinRequest.Sucesso).adress

                    Log.d(TAG, address.postalCode)
                    latTextView.text = address.latitude.toString()
                    lonTextView.text = address.longitude.toString()
                    addressTextView.text = address.postalCode

                }

                is resultLocatioinRequest.Erro -> {
                    val msg = (result as resultLocatioinRequest.Erro).msg
                    Log.d(TAG, msg)
                }

            }


        }


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


}
