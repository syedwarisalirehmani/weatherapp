package com.ramalingam.localforecast.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.adsutils.Adshandler
import com.ramalingam.localforecast.common.CommonConstantAd
import com.ramalingam.localforecast.common.CommonConstants
import com.ramalingam.localforecast.common.Utils
import com.ramalingam.localforecast.database.DataBaseHelper
import com.ramalingam.localforecast.interfaces.AdsCallback
import com.ramalingam.localforecast.interfaces.CallbackListener

class SplashActivity : AppCompatActivity(), CallbackListener, AdsCallback {

    private var isLoaded = false
    private val handler = Handler()
    private val myRunnable = Runnable {
        if (Utils.isNetworkConnected(this@SplashActivity)) {
            if (!isLoaded) {
//                startNextActivity(0)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        callApi()
    }

    fun callApi() {
        if (Utils.isNetworkConnected(this)) {
            successCall()
        } else {
            Utils.openInternetDialog(this, this, true)
        }
        handler.postDelayed(myRunnable, 10000)
    }


    private fun startNextActivity(time: Long) {
        try {
            Thread {
                kotlin.run {
                    synchronized(this) {
                        DataBaseHelper(this).getReadWriteDB()
                        Thread.sleep(time)
                        runOnUiThread {
                            finish()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {
        callApi()
    }


    private fun successCall() {
        if (Utils.getPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 1) === 1) {
            Utils.setPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 2)
//            startNextActivity(1000)
        } else {
            checkAd()
        }
    }

    private fun checkAd() {
        if (Utils.getPref(this, CommonConstants.STATUS_ENABLE_DISABLE, "")
                        .equals(CommonConstants.ENABLE)
        ) {
            if (Utils.getPref(this, CommonConstants.AD_TYPE_FB_GOOGLE, "")
                            .equals(CommonConstants.AD_GOOGLE)
            ) {
                CommonConstantAd.googlebeforloadAd(this)
            } else if (Utils.getPref(this, CommonConstants.AD_TYPE_FB_GOOGLE, "")
                            .equals(CommonConstants.AD_FACEBOOK)
            ) {
                CommonConstantAd.facebookbeforeloadFullAd(this)
            }
            if (Utils.getPref(this, CommonConstants.STATUS_ENABLE_DISABLE, "")
                            .equals(CommonConstants.ENABLE)
            ) {
                Handler().postDelayed({
                    if (Utils.getPref(
                                    this@SplashActivity,
                                    CommonConstants.AD_TYPE_FB_GOOGLE,
                                    ""
                            ).equals(CommonConstants.AD_GOOGLE)
                    ) {
                        CommonConstantAd.showInterstitialAdsGoogle(
                                this@SplashActivity,
                                this@SplashActivity
                        )
                    } else if (Utils.getPref(
                                    this@SplashActivity,
                                    CommonConstants.AD_TYPE_FB_GOOGLE,
                                    ""
                            ).equals(CommonConstants.AD_FACEBOOK)
                    ) {
                        CommonConstantAd.showInterstitialAdsFacebook(this@SplashActivity)
                    } else {
//                        startNextActivity(0)
                    }
                }, 3000)
                Utils.setPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 1)
            } else {
//                startNextActivity(0)
            }
        } else {
            Utils.setPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 1)
//            startNextActivity(1000)
        }
    }

    override fun adLoadingFailed() {
//        startNextActivity(0)
    }

    override fun adClose() {
//        startNextActivity(0)
    }

    override fun startNextScreen() {
//        startNextActivity(0)
    }

    override fun onLoaded() {
        isLoaded = true
    }


    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(myRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(myRunnable)
    }

    fun onClick(view: View) {
        Adshandler.showAd(this, object : Adshandler.OnClose {
            override fun onclick() {
                startNextActivity(0)
            }
        })
    }
}