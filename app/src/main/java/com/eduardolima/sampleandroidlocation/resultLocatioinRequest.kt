package com.eduardolima.sampleandroidlocation

import android.location.Address

sealed class resultLocatioinRequest {
    class Sucesso(val msg: String, val adress: Address) : resultLocatioinRequest()
    class Erro(val msg: String) : resultLocatioinRequest()
}