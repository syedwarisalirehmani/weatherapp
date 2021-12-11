package com.ramalingam.localforecast.custom;

import android.content.Context
import android.widget.Toast

class CustomToast {
    companion object {
        fun showToast(context: Context?, msg: String?) {
            val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}