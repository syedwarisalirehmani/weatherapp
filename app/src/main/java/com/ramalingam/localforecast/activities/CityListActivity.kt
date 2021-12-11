package com.ramalingam.localforecast.activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.kaopiz.kprogresshud.KProgressHUD
import com.ramalingam.localforecast.BuildConfig
import com.ramalingam.localforecast.R

import com.ramalingam.localforecast.adapters.AdapterCityList
import com.ramalingam.localforecast.common.CommonConstants
import com.ramalingam.localforecast.common.CommonUtilities
import com.ramalingam.localforecast.common.Utils
import com.ramalingam.localforecast.custom.CustomProgressDialog
import com.ramalingam.localforecast.custom.CustomToast
import com.ramalingam.localforecast.custom.FetchAddressIntentService
import com.ramalingam.localforecast.database.DataBaseHelper
import com.ramalingam.localforecast.interfaces.AdapterItemCallBack
import kotlinx.android.synthetic.main.activity_city_list.*

class CityListActivity : AppCompatActivity(), AdapterItemCallBack,
    SearchView.OnQueryTextListener, View.OnClickListener, SearchView.OnCloseListener {

    private lateinit var imgBack: AppCompatImageView
    private lateinit var tvTitleToolbar: AppCompatTextView
    private lateinit var searchView: SearchView
    private lateinit var imgCurrentLocation: AppCompatImageView

    private lateinit var rvCityList: RecyclerView
    private lateinit var tvNoData: AppCompatTextView

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapterCityList: AdapterCityList

    private var cityMainArrayList = ArrayList<String>()
    private var cityBindArrayList = ArrayList<String>()
    private var citySearchArrayList = ArrayList<String>()

    private val pageSize = 500
    private var previousTotal = 0
    private var firstVisibleItem: Int = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var loading = true

    private var startIndex = 0
    private var endIndex = 0

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    private var mResultReceiver: AddressResultReceiver? = null
    private var dialog: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupContentWindow()
        setContentView(R.layout.activity_city_list)
        initViews()
        Utils.loadBannerAd(llAdView,llAdViewFacebook,this)
        GetCityListFromDB(this@CityListActivity, dialog!!).execute()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupContentWindow() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = resources.getColor(R.color.colorFullTransparent)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    private fun initViews() {

        dialog = CustomProgressDialog().showProgressDialog(this)

        startIndex = 0
        endIndex = pageSize

        mResultReceiver = AddressResultReceiver(Handler())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cityMainArrayList = ArrayList()
        cityBindArrayList = ArrayList()
        citySearchArrayList = ArrayList()

        imgBack = findViewById(R.id.imgBackActCityList)
        tvTitleToolbar = findViewById(R.id.tvTitleToolbarActCityList)
        searchView = findViewById(R.id.searchViewActCityList)
        imgCurrentLocation = findViewById(R.id.imgCurrentLocationActCityList)

        rvCityList = findViewById(R.id.rvCityListActCityList)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCityList.layoutManager = layoutManager
        rvCityList.addOnScrollListener(scrollListener)

        tvNoData = findViewById(R.id.tvNoDataActCityList)

        imgBack.setOnClickListener(this)

        searchView.setOnClickListener(this)
        searchView.setOnSearchClickListener(this)
        searchView.setOnCloseListener(this)
        searchView.setOnQueryTextListener(this)
        imgCurrentLocation.setOnClickListener(this)

    }



    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.imgBackActCityList) {
            if (isCitySelected()) {
                finish()
            } else {
                CustomToast.showToast(this, CommonConstants.MsgSelectCity)
            }
        } else if (id == R.id.tvTitleToolbarActCityList) {
            tvTitleToolbar.text = resources.getString(R.string.search_here)
        } else if (id == R.id.imgCurrentLocationActCityList) {
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                getAddress()
            }
        }
    }

    override fun onClose(): Boolean {
        tvTitleToolbar.text = resources.getString(R.string.select_city)
        return false
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetCityListFromDB(val context: Context, private val dialog: KProgressHUD) : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            if (!dialog.isShowing) {
                dialog.show()
            }
        }

        override fun doInBackground(vararg params: Void?): String {
            try {
                cityMainArrayList = if (CommonUtilities.cityStaticArrayList.size > 0) {
                    CommonUtilities.cityStaticArrayList
                } else {
                    db.getAllCityList()
                }
                if (cityMainArrayList.size > 0) {
                    CommonUtilities.cityStaticArrayList = cityMainArrayList
                    if (cityMainArrayList.size < pageSize) {
                        startIndex = 0
                        endIndex = cityMainArrayList.size
                    }

                    for (i in startIndex until endIndex) {
                        cityBindArrayList.add(cityMainArrayList[i])
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (cityBindArrayList.size > 0) {
                    adapterCityList = AdapterCityList(context, cityBindArrayList, this@CityListActivity)
                    rvCityList.adapter = adapterCityList
                    hideShowNoData(false)
                } else {
                    hideShowNoData(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hideShowNoData(true)
            } finally {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun hideShowNoData(mShow: Boolean) {
        if (mShow) {
            CustomToast.showToast(this, CommonConstants.MsgSomethingWrong)
            rvCityList.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        } else {
            rvCityList.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView!!, dx, dy)
            if (dy > 0) {
                visibleItemCount = layoutManager.childCount
                totalItemCount = layoutManager.itemCount
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false
                        previousTotal = totalItemCount
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + pageSize) {
                    loading = true

                    startIndex = endIndex
                    endIndex = if (cityMainArrayList.size > endIndex + pageSize) endIndex + pageSize else cityMainArrayList.size

                    LoadMoreItems(this@CityListActivity).execute()
                }
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    inner class LoadMoreItems(val context: Context?) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            for (i in startIndex until endIndex) {
                cityBindArrayList.add(cityMainArrayList[i])
            }
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            adapterCityList.notifyItemRangeInserted(startIndex, endIndex)
        }
    }

    private val db = DataBaseHelper(this)
    override fun onQueryTextChange(newText: String): Boolean {
        citySearchArrayList = ArrayList()
        if (newText.isNotEmpty()) {
            citySearchArrayList = db.getCityBySearch(newText)
            rvCityList.adapter = AdapterCityList(this, citySearchArrayList, this@CityListActivity)
        } else if (newText.isEmpty()) {
            rvCityList.adapter = AdapterCityList(this, cityBindArrayList, this@CityListActivity)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onMethodCallBack(mPos: Int) {
        try {
            val selectedCity: String = if (citySearchArrayList.isNotEmpty()) {
                citySearchArrayList[mPos]
            } else {
                cityBindArrayList[mPos]
            }
            setCityAndFinish(selectedCity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCityAndFinish(city: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(CommonConstants.SelectedCity, city)
        editor.apply()

        val intent = intent
        intent.putExtra(CommonConstants.SelectedCity, city)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun isCitySelected(): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        return pref.getString(CommonConstants.SelectedCity, "")!!.isNotEmpty()
    }

    override fun onBackPressed() {
        if (isCitySelected()) {
            super.onBackPressed()
        } else {
            CustomToast.showToast(this, CommonConstants.MsgSelectCity)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getAddress() {
        dialog = CustomProgressDialog().showProgressDialog(this)
        mFusedLocationClient!!.lastLocation
                .addOnSuccessListener(this, OnSuccessListener { location ->
                    try {
                        if (location == null) {
                            Log.e(CommonConstants.LogTag, "location = null")
                            if (dialog!!.isShowing) {
                                dialog!!.dismiss()
                            }
                            return@OnSuccessListener
                        }
                        mLastLocation = location
                        if (!Geocoder.isPresent()) {
                            CustomToast.showToast(this, CommonConstants.MsgSomethingWrong)
                            return@OnSuccessListener
                        }
                        if (mLastLocation != null) {
                            startIntentService()
                            return@OnSuccessListener
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
                .addOnFailureListener(this) { e ->
                    Log.e(CommonConstants.LogTag, "getLastLocation:onFailure", e)
                    if (dialog!!.isShowing) {
                        dialog!!.dismiss()
                    }
                    CustomToast.showToast(this, CommonConstants.MsgSomethingWrong)
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
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                }
                if (resultCode == CommonConstants.SUCCESS_RESULT) {
                    val selectedCity = resultData.getString(CommonConstants.RESULT_DATA_KEY)
                    val editor = PreferenceManager.getDefaultSharedPreferences(this@CityListActivity).edit()
                    editor.putString(CommonConstants.SelectedCity, selectedCity)
                    editor.apply()
                    setCityAndFinish(selectedCity!!)
                } else {
                    CustomToast.showToast(this@CityListActivity, CommonConstants.MsgSomethingWrong)
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
}