package com.ramalingam.localforecast.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.kaopiz.kprogresshud.KProgressHUD
import com.ramalingam.localforecast.BuildConfig
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.adsutils.Adshandler
import com.ramalingam.localforecast.common.CommonConstants
import com.ramalingam.localforecast.common.CommonUtilities
import com.ramalingam.localforecast.common.NetworkUtil
import com.ramalingam.localforecast.common.Utils
import com.ramalingam.localforecast.custom.CustomProgressDialog
import com.ramalingam.localforecast.custom.CustomToast
import com.ramalingam.localforecast.custom.FetchAddressIntentService
import com.ramalingam.localforecast.fragments.CityWeatherFragment
import com.ramalingam.localforecast.fragments.LiveWeatherFragment
import com.ramalingam.localforecast.fragments.TemperatureForecastFragment
import com.ramalingam.localforecast.fragments.WindSpeedFragment
import com.ramalingam.localforecast.interfaces.CallbackListener
import com.ramalingam.localforecast.interfaces.ConfirmDialogCallBack
import com.ramalingam.localforecast.notifications.AlarmReceiver
import kotlinx.android.synthetic.main.activity_city_list.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, CallbackListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var imgToggle: AppCompatImageView
    private lateinit var tvTitleToolbar: AppCompatTextView
    private lateinit var imgPlusRefresh: AppCompatImageView

    private lateinit var navigationView: NavigationView
    private lateinit var tvWeather: AppCompatTextView
    private lateinit var tvLiveWeather: AppCompatTextView
    private lateinit var tvRainfallForecast: AppCompatTextView
    private lateinit var tvTemperatureForecast: AppCompatTextView
    private lateinit var tvShare: AppCompatTextView
    private lateinit var tvRate: AppCompatTextView
    private lateinit var tvFeedback: AppCompatTextView
    private lateinit var tvMoreApp: AppCompatTextView
    private lateinit var tvExit: AppCompatTextView

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    private var mResultReceiver: AddressResultReceiver? = null
    private var dialog: KProgressHUD? = null

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initViews()
        checkForNotification()
        checkForSelectedCity()
        successCall()
        try {
            //subScribeToFirebaseTopic();
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        Utils.loadBannerAd(llAdView, llAdViewFacebook, this)
    }

    private fun subScribeToFirebaseTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("weather_app_topic")
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.e("subScribeFirebaseTopic", ": Fail")
                        } else {
                            Log.e("subScribeFirebaseTopic", ": Success")
                        }
                    }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun successCall() {
        if (Utils.isNetworkConnected(this)) {
            if (CommonConstants.ENABLE_DISABLE == CommonConstants.ENABLE) {
                Utils.setPref(
                        this,
                        CommonConstants.AD_TYPE_FB_GOOGLE,
                        CommonConstants.AD_TYPE_FACEBOOK_GOOGLE
                )
                Utils.setPref(
                        this,
                        CommonConstants.FB_BANNER,
                        CommonConstants.FB_BANNER_ID
                )
                Utils.setPref(
                        this,
                        CommonConstants.FB_INTERSTITIAL,
                        CommonConstants.FB_INTERSTITIAL_ID
                )
                Utils.setPref(
                        this,
                        CommonConstants.GOOGLE_BANNER,
                        CommonConstants.GOOGLE_BANNER_ID
                )
                Utils.setPref(
                        this,
                        CommonConstants.GOOGLE_INTERSTITIAL,
                        CommonConstants.GOOGLE_INTERSTITIAL_ID
                )
                Utils.setPref(
                        this,
                        CommonConstants.STATUS_ENABLE_DISABLE,
                        CommonConstants.ENABLE_DISABLE
                )
                setAppAdId(CommonConstants.GOOGLE_ADMOB_APP_ID)
            } else {
                Utils.setPref(
                        this,
                        CommonConstants.STATUS_ENABLE_DISABLE,
                        CommonConstants.ENABLE_DISABLE
                )
            }
        } else {
            Utils.openInternetDialog(this, this, true)
        }
    }


    fun setAppAdId(id: String?) {
        try {
            val applicationInfo =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            val beforeChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:BeforeChange:::::  $beforeChangeId")
            applicationInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", id)
            val AfterChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:AfterChange::::  $AfterChangeId")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    private fun initViews() {

        CommonUtilities.cityNameMain = ""
        CommonUtilities.authorizationLine = ""

        mHandler = Handler()

        drawer = findViewById(R.id.drawerLayoutActMain)
        imgToggle = findViewById(R.id.imgToggleActMain)
        tvTitleToolbar = findViewById(R.id.tvTitleToolbarActMain)
        imgPlusRefresh = findViewById(R.id.imgPlusRefreshActMain)
        navigationView = findViewById(R.id.navViewActMain)

        tvWeather = findViewById(R.id.tvWeatherActMain)
        tvLiveWeather = findViewById(R.id.tvLiveWeatherActMain)
        tvRainfallForecast = findViewById(R.id.tvRainfallForecastActMain)
        tvTemperatureForecast = findViewById(R.id.tvTemperatureForecastActMain)
        tvShare = findViewById(R.id.tvShareActMain)
        tvRate = findViewById(R.id.tvRateActMain)
        tvFeedback = findViewById(R.id.tvFeedbackActMain)
        tvMoreApp = findViewById(R.id.tvMoreAppActMain)
        tvExit = findViewById(R.id.tvExitActMain)

        imgToggle.setOnClickListener(this)
        imgPlusRefresh.setOnClickListener(this)
        tvWeather.setOnClickListener(this)
        tvLiveWeather.setOnClickListener(this)
        tvRainfallForecast.setOnClickListener(this)
        tvTemperatureForecast.setOnClickListener(this)
        tvShare.setOnClickListener(this)
        tvRate.setOnClickListener(this)
        tvFeedback.setOnClickListener(this)
        tvMoreApp.setOnClickListener(this)
        tvExit.setOnClickListener(this)
    }

    private fun checkForNotification() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (!pref.getBoolean(CommonConstants.IsFirstTime, false)) {
            setAlarmManager()
            val editor = pref.edit()
            editor.putBoolean(CommonConstants.IsFirstTime, true)
            editor.apply()
        }
    }

    private fun checkForSelectedCity() {
        try {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val selectedCity = pref.getString(CommonConstants.SelectedCity, "")
            if (selectedCity!!.isNotEmpty()) {
                openNextFragment(CommonConstants.CityWeatherFg, CityWeatherFragment())
            } else {
                mResultReceiver = AddressResultReceiver(Handler())
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                if (!checkPermissions()) {
                    requestPermissions()
                } else {
                    getAddress()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private lateinit var adRequest: AdRequest
    private lateinit var adViewBottom: AdView


    override fun onClick(view: View) {
        when (val id = view.id) {
            R.id.imgToggleActMain -> openDrawer()
            R.id.imgPlusRefreshActMain ->
                Adshandler.showAd(this, object : Adshandler.OnClose {
                    override fun onclick() {
                        selectCityOrRefreshData()
                    }
                })

            R.id.tvWeatherActMain ->
                Adshandler.showAd(this, object : Adshandler.OnClose {
                    override fun onclick() {
                        openNextFragment(CommonConstants.CityWeatherFg, CityWeatherFragment())
                    }
                })

            R.id.tvLiveWeatherActMain ->
                Adshandler.showAd(this, object : Adshandler.OnClose {
                    override fun onclick() {
                        openNextFragment(CommonConstants.LiveWeatherFg, LiveWeatherFragment())
                    }
                })
            R.id.tvRainfallForecastActMain ->
                Adshandler.showAd(this, object : Adshandler.OnClose {
                    override fun onclick() {
                        openNextFragment(CommonConstants.RainFallForecastFg, WindSpeedFragment())
                    }
                })


            R.id.tvTemperatureForecastActMain ->
                Adshandler.showAd(this, object : Adshandler.OnClose {
                    override fun onclick() {
                        openNextFragment(CommonConstants.TemperatureForecastFg, TemperatureForecastFragment())
                    }
                })
            else -> {
                when (id) {
                    R.id.tvShareActMain ->
                        Adshandler.showAd(this, object : Adshandler.OnClose {
                            override fun onclick() {
                                shareAppLink()
                            }
                        })
                    R.id.tvRateActMain ->
                        Adshandler.showAd(this, object : Adshandler.OnClose {
                            override fun onclick() {
                                rateUs()
                            }
                        })

                    R.id.tvFeedbackActMain ->
                        Adshandler.showAd(this, object : Adshandler.OnClose {
                            override fun onclick() {
                                feedBack()
                            }
                        })


                    R.id.tvExitActMain -> CommonUtilities.confirmationDialog(this, object : ConfirmDialogCallBack {
                        override fun Okay() {
                            finish()
                        }

                        override fun cancel() {

                        }

                    }, "", getString(R.string.exit_confirmation))
                }
            }
        }
    }

    private fun selectCityOrRefreshData() {
        if (NetworkUtil.getConnectivityStatus(this)) {
            try {
                val fm = supportFragmentManager
                val count = fm.backStackEntryCount
                if (count == 0) {
                    val intent = Intent(this, CityListActivity::class.java)
                    startActivityForResult(intent, CommonConstants.ReqCodeCitySelection)
                } else {
                    val fragTag = supportFragmentManager.getBackStackEntryAt(count - 1).name
                    when (fragTag) {
                        CommonConstants.CityWeatherFg -> {
                            val intent = Intent(this, CityListActivity::class.java)
                            startActivityForResult(intent, CommonConstants.ReqCodeCitySelection)
                        }
                        CommonConstants.LiveWeatherFg -> {
                            val fragment = fm.findFragmentByTag(fragTag) as LiveWeatherFragment
                            fragment.loadData()
                        }
                        CommonConstants.TemperatureForecastFg -> {
                            val fragment = fm.findFragmentByTag(fragTag) as TemperatureForecastFragment
                            fragment.loadData()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            openNoInternetDialog()
        }
    }

    private fun openNoInternetDialog() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(CommonConstants.CapInternet)
            builder.setMessage(CommonConstants.MsgNoInternet)
            builder.setCancelable(false)

            builder.setPositiveButton(CommonConstants.BtnRetry) { dialog, _ ->
                if (NetworkUtil.getConnectivityStatus(this)) {
                    dialog.dismiss()
                } else {
                    showSnackBar(R.string.msgNoInternet, R.string.ok,
                            View.OnClickListener { })
                }


            }

            builder.setNegativeButton(CommonConstants.BtnCancel) { dialog, i ->
                dialog.dismiss()
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(homeIntent)
                finishAffinity()
            }
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openNextFragment(fragTag: String, fragment: Fragment) {
        closeDrawer()
        if (fragTag == CommonConstants.CityWeatherFg) {
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fm.popBackStack(fragTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            updateTitleAndRightIcon(fragTag)
            ft.replace(R.id.frameActMain, fragment, fragTag)
            ft.addToBackStack(fragTag)
            ft.commit()
            fm.executePendingTransactions()
        } else {
            if (NetworkUtil.getConnectivityStatus(this)) {
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                fm.popBackStack(fragTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                updateTitleAndRightIcon(fragTag)
                ft.replace(R.id.frameActMain, fragment, fragTag)
                ft.addToBackStack(fragTag)
                ft.commit()
                fm.executePendingTransactions()
            } else {
                openNoInternetDialog()
            }
        }
    }

    override fun onResume() {
        if (!NetworkUtil.getConnectivityStatus(this)) {
            openNoInternetDialog()
        }
        super.onResume()
    }

    private fun feedBack() {
        try {
            val sendIntentGmail = Intent(Intent.ACTION_SEND)
            sendIntentGmail.type = "plain/text"
            sendIntentGmail.setPackage("com.google.android.gm")
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("Siv.ram@gmail.com"))
//            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");

            if (resources.getString(R.string.app_name) != null)
//                sendIntentGmail.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " - Android")
                startActivity(sendIntentGmail)
        } catch (e: Exception) {
            val sendIntentIfGmailFail = Intent(Intent.ACTION_SEND)
            sendIntentIfGmailFail.type = "*/*"
            sendIntentIfGmailFail.putExtra(Intent.EXTRA_EMAIL, arrayOf("Siv.ram@gmail.com"))
            if (resources.getString(R.string.app_name) != null)
                sendIntentIfGmailFail.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " - Android")
            if (sendIntentIfGmailFail.resolveActivity(packageManager) != null) {
                startActivity(sendIntentIfGmailFail)
            }
        }
    }

    private fun shareAppLink() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        val link = "https://play.google.com/store/apps/details?id=$packageName"
        shareIntent.putExtra(Intent.EXTRA_TEXT, link)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name) + " - Android")
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.app_name) + " - Android"))
    }

    private fun moreApp() {
        val uri = Uri.parse("https://play.google.com/store/apps/developer?id=Ninety+Nine+Apps")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun rateUs() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    @SuppressLint("WrongConstant")
    private fun openDrawer() {
        if (!drawer.isDrawerOpen(Gravity.START)) {
            drawer.openDrawer(Gravity.START)
        }
    }

    @SuppressLint("WrongConstant")
    private fun closeDrawer() {
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == CommonConstants.ReqCodeCitySelection) {
                if (resultCode == RESULT_OK) {
                    val city = data!!.getStringExtra(CommonConstants.SelectedCity)
                    updateTitleAndRightIcon(CommonConstants.CityWeatherFg)
                    val fm = supportFragmentManager
                    val count = fm.backStackEntryCount
                    if (count == 0) {
                        openNextFragment(CommonConstants.CityWeatherFg, CityWeatherFragment())
                    } else {
                        val dialog = CustomProgressDialog().showProgressDialog(this)
                        val fragment = supportFragmentManager.findFragmentByTag(CommonConstants.CityWeatherFg) as CityWeatherFragment
//                        fragment.callCityWeatherService(this, dialog, city!!)
                        fragment.callApiCityWeatherData(this, dialog, city!!)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        try {
            closeDrawer()
            val fm = supportFragmentManager
            val count = fm.backStackEntryCount
            if (count <= 1) {
                CommonUtilities.confirmationDialog(this, object : ConfirmDialogCallBack {
                    override fun Okay() {
                        finish()
                    }

                    override fun cancel() {

                    }

                }, "", getString(R.string.exit_confirmation))
            } else {
                if (count > 1) {
                    val fragTag = fm.getBackStackEntryAt(count - 2).name
                    updateTitleAndRightIcon(fragTag!!)
                    Log.e(CommonConstants.LogTag, " Pos :$count Tag :$fragTag")
                    fm.popBackStack()
                } else {
                    CommonUtilities.confirmationDialog(this, object : ConfirmDialogCallBack {
                        override fun Okay() {
                            finish()
                        }

                        override fun cancel() {

                        }

                    }, "", getString(R.string.exit_confirmation))

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateTitleAndRightIcon(fragName: String) {
        try {
            when (fragName) {
                CommonConstants.CityWeatherFg -> {
                    val pref = PreferenceManager.getDefaultSharedPreferences(this)
                    val selectedCity = pref.getString(CommonConstants.SelectedCity, "")
                    if (selectedCity!!.isNotEmpty()) {
                        tvTitleToolbar.text = selectedCity
                    } else {
                        tvTitleToolbar.text = resources.getString(R.string.app_name)
                    }
                    imgPlusRefresh.visibility = View.VISIBLE
                    imgPlusRefresh.setImageResource(R.drawable.round_add_white_24)
                }
                CommonConstants.LiveWeatherFg -> {
                    tvTitleToolbar.text = CommonConstants.TitleLiveWeather
//                    imgPlusRefresh.setImageResource(R.drawable.round_refresh_white_24)
                    imgPlusRefresh.visibility = View.INVISIBLE
                }
                CommonConstants.RainFallForecastFg -> {
                    tvTitleToolbar.text = CommonConstants.TitleRainFallForecast
                    imgPlusRefresh.visibility = View.INVISIBLE
//                    imgPlusRefresh.setImageResource(R.drawable.round_refresh_white_24)
                }
                CommonConstants.TemperatureForecastFg -> {
                    tvTitleToolbar.text = CommonConstants.TitleTemperatureForecast
                    imgPlusRefresh.visibility = View.INVISIBLE
//                    imgPlusRefresh.setImageResource(R.drawable.round_refresh_white_24)
                }
                CommonConstants.WeatherMapFg -> {
                    tvTitleToolbar.text = CommonConstants.TitleWeatherMap
                    imgPlusRefresh.visibility = View.INVISIBLE
//                    imgPlusRefresh.setImageResource(R.drawable.round_refresh_white_24)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAlarmManager() {
        try {
            val alarmStartTime = Calendar.getInstance()
            alarmStartTime.set(Calendar.HOUR_OF_DAY, 12)
            alarmStartTime.set(Calendar.MINUTE, 0)
            alarmStartTime.set(Calendar.SECOND, 0)
            val alarmIntent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO --------------------------------- For Remission & Location ------------------------------------
    @SuppressLint("MissingPermission")
    private fun getAddress() {
        mFusedLocationClient!!.lastLocation
                .addOnSuccessListener(this, OnSuccessListener { location ->
                    try {
                        if (location == null) {
                            /*Log.e(CommonConstants.LogTag, "location = null")
                            try {
                                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
                                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                            } catch(ex: SecurityException) {
                                Log.d("myTag", "Security Exception, no location available");
                            }*/
                            val intent = Intent(this, CityListActivity::class.java)
                            startActivityForResult(intent, CommonConstants.ReqCodeCitySelection)
                            return@OnSuccessListener
                        }
                        mLastLocation = location
                        if (!Geocoder.isPresent()) {
                            CustomToast.showToast(this, CommonConstants.MsgSomethingWrong)
                            return@OnSuccessListener
                        }
                        if (mLastLocation != null) {
                            dialog = CustomProgressDialog().showProgressDialog(this)
                            startIntentService()
                            return@OnSuccessListener
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
                .addOnFailureListener(this) { e ->
                    Log.e(CommonConstants.LogTag, "getLastLocation:onFailure", e)
                }
    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            mLastLocation = location
            if (mLastLocation != null) {
                dialog = CustomProgressDialog().showProgressDialog(this@MainActivity)
                startIntentService()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    private fun startIntentService() {
        try {
            val intent = Intent(this, FetchAddressIntentService::class.java)
            intent.putExtra(CommonConstants.RECEIVER, mResultReceiver)
            intent.putExtra(CommonConstants.LOCATION_DATA_EXTRA, mLastLocation)
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class AddressResultReceiver internal constructor(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            try {
                dialog!!.dismiss()
                if (resultCode == CommonConstants.SUCCESS_RESULT) {
                    val selectedCity = resultData.getString(CommonConstants.RESULT_DATA_KEY)
                    val editor = PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit()
                    editor.putString(CommonConstants.SelectedCity, selectedCity)
                    editor.apply()
                    openNextFragment(CommonConstants.CityWeatherFg, CityWeatherFragment())
                } else {
                    CustomToast.showToast(this@MainActivity, CommonConstants.MsgSomethingWrong)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showSnackBar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        try {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(mainTextStringId),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(actionStringId), listener).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        try {
            val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (shouldProvideRationale) {
                showSnackBar(R.string.permission_rationale, android.R.string.ok,
                        View.OnClickListener {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CommonConstants.ReqCodeLocationPermission)
                        })

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), CommonConstants.ReqCodeLocationPermission)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == CommonConstants.ReqCodeLocationPermission) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAddress()
                } else {
                    showSnackBar(R.string.permission_denied_explanation, R.string.settings,
                            View.OnClickListener {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }
    //TODO --------------------------------- For Remission & Location ------------------------------------
}