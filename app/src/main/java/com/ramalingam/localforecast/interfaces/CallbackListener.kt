package com.ramalingam.localforecast.interfaces

interface CallbackListener {
    fun onSuccess()
    fun onCancel()
    fun onRetry()
}