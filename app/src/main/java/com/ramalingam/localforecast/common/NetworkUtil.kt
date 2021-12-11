package com.ramalingam.localforecast.common;

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


class NetworkUtil {

    companion object {
        fun getConnectivityStatus(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting == true
        }
    }
}