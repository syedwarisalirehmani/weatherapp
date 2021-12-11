package com.ramalingam.localforecast.interfaces;

import android.content.Context

interface AdsCallback {
    fun adLoadingFailed()
    fun adClose()
    fun startNextScreen()
    fun onLoaded()
}