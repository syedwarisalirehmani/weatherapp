package com.ramalingam.localforecast.custom;

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.common.CommonConstants


import java.io.IOException
import java.util.ArrayList
import java.util.Locale

class FetchAddressIntentService : IntentService(TAG) {

    private var mReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        mReceiver = intent!!.getParcelableExtra(CommonConstants.RECEIVER)
        if (mReceiver == null) {
            return
        }

        val location = intent.getParcelableExtra<Location>(CommonConstants.LOCATION_DATA_EXTRA)
        if (location == null) {
            Log.e(TAG, getString(R.string.no_location_data_provided))
            deliverResultToReceiver(CommonConstants.FAILURE_RESULT, CommonConstants.MsgSomethingWrong)
            return
        }

        val geoCoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null

        try {
            addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: IOException) {
            Log.e(TAG, getString(R.string.service_not_available), ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, getString(R.string.invalid_lat_long_used) + ". " +
                    "Lat = " + location.latitude +
                    ", Lng = " + location.longitude, illegalArgumentException)
        }

        // Handle case where no address was found.
        try {
            if (addresses == null || addresses.size == 0) {
                Log.e(TAG, getString(R.string.no_address_found))
                deliverResultToReceiver(CommonConstants.FAILURE_RESULT, CommonConstants.MsgSomethingWrong)
            } else {
                val address = addresses[0]
                val addressFragments = ArrayList<String>()

                // Fetch the address lines using {@code getAddressLine},
                // join them, and send them to the thread. The {@link android.location.address}
                // class provides other options for fetching address details that you may prefer
                // to use. Here are some examples:
                // getLocality() ("Surat", for example)
                // getSubLocality() ("Vishal Nagar", for example)
                // getAdminArea() ("Gujarat", for example)
                // getSubAdminArea() ("Surat", for example)
                // getPostalCode() ("395004", for example)
                // getCountryCode() ("IN", for example)
                // getCountryName() ("India", for example)

                var cityName: String? = address.locality
                if (cityName == null || cityName.isEmpty()) {
                    cityName = address.subAdminArea
                    if (cityName == null || cityName.isEmpty()) {
                        cityName = address.adminArea
                    }
                }
                addressFragments.add(cityName!!)

                /*
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                */
                Log.e(TAG, getString(R.string.address_found))
                deliverResultToReceiver(CommonConstants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"), addressFragments))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle()
        bundle.putString(CommonConstants.RESULT_DATA_KEY, message)
        mReceiver!!.send(resultCode, bundle)
    }

    companion object {
        private val TAG = CommonConstants.LogTag + " FetchAddIS "
    }
}
