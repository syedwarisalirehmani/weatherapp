package com.ramalingam.localforecast.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.kaopiz.kprogresshud.KProgressHUD
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.adapters.AdapterCityWeatherList
import com.ramalingam.localforecast.common.CommonConstants
import com.ramalingam.localforecast.common.CommonUtilities
import com.ramalingam.localforecast.common.NetworkUtil
import com.ramalingam.localforecast.custom.CustomProgressDialog
import com.ramalingam.localforecast.custom.CustomToast
import com.ramalingam.localforecast.database.DataBaseHelper
import com.ramalingam.localforecast.pojo.CityWeatherClass
import com.ramalingam.localforecast.volley.JsonObjectUTF8
import com.ramalingam.localforecast.volley.VolleySingleton

import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class CityWeatherFragment : Fragment() {

    private lateinit var nestedSv: NestedScrollView
    private lateinit var imgCurrentWeather: AppCompatImageView
    private lateinit var tvCurrentMinMaxTemp: AppCompatTextView
    private lateinit var tvDate: AppCompatTextView

    private lateinit var tvCurrentTemp: AppCompatTextView
    private lateinit var rvWeatherList: RecyclerView
    private lateinit var tvSunrise: AppCompatTextView
    private lateinit var tvSunset: AppCompatTextView

    private lateinit var imgWindMill: AppCompatImageView
    private lateinit var tvDirection: AppCompatTextView
    private lateinit var tvSpeed: AppCompatTextView

    private lateinit var tvPressure: AppCompatTextView
    private lateinit var tvVisibility: AppCompatTextView
    private lateinit var tvHumidity: AppCompatTextView

    private lateinit var cityWeatherClassArrayList: ArrayList<CityWeatherClass>
    private lateinit var adapterCityWeatherList: AdapterCityWeatherList

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_city_weather, container, false)
        initViews(view)
        checkAndSetData()
        return view
    }

    private fun initViews(view: View) {
        try {
            cityWeatherClassArrayList = ArrayList()

            nestedSv = view.findViewById(R.id.nestedSvFgCityWeather)
            nestedSv.visibility = View.INVISIBLE

            imgCurrentWeather = view.findViewById(R.id.imgCurrentWeatherFgCityWeather)
            tvCurrentMinMaxTemp = view.findViewById(R.id.tvCurrentMinMaxTempFgCityWeather)
            tvDate = view.findViewById(R.id.tvDateFgCityWeather)
            tvCurrentTemp = view.findViewById(R.id.tvCurrentTempFgCityWeather)

            rvWeatherList = view.findViewById(R.id.rvWeatherListFgCityWeather)
            rvWeatherList.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
            rvWeatherList.isNestedScrollingEnabled = false

            tvSunrise = view.findViewById(R.id.tvSunriseFgCityWeather)
            tvSunset = view.findViewById(R.id.tvSunsetFgCityWeather)
            tvPressure = view.findViewById(R.id.tvPressureFgCityWeather)

            imgWindMill = view.findViewById(R.id.imgWindMillFgCityWeather)
            tvDirection = view.findViewById(R.id.tvDirectionFgCityWeather)
            tvSpeed = view.findViewById(R.id.tvSpeedFgCityWeather)

            tvVisibility = view.findViewById(R.id.tvVisibilityFgCityWeather)
            tvHumidity = view.findViewById(R.id.tvHumidityFgCityWeather)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun callApiCityWeatherData(context: Context, dialog: KProgressHUD, cityName: String){
        try {
            val serviceUrl = CommonUtilities.getCityWiseWeatherUrl(cityName)
            val jsonObjReq = object : JsonObjectUTF8(Method.GET, serviceUrl, null, Response.Listener { jsonObject ->
                if (jsonObject.length() > 0) {
                    val data = jsonObject.toString()
                    Log.e("TAG", "callApiCityWeatherData::::::data===>>> $data")
                    setNewData(context, true, dialog, jsonObject)
//                    setCityWeatherData(context, true, dialog, jsonObject)
                }
            }, Response.ErrorListener { e ->
                e.printStackTrace()
                CommonUtilities.cityNameMain = ""
                setOfflineData(dialog)
            }) {
                override fun getHeaders(): HashMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            jsonObjReq.retryPolicy = CommonUtilities.retryPolicy
            VolleySingleton.getInstance(context).addToRequestQueue(jsonObjReq)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun clearAllData() {
        nestedSv.visibility = View.INVISIBLE
        imgCurrentWeather.setImageDrawable(null)
        tvDate.text = ""
        tvCurrentTemp.text = ""
        tvCurrentMinMaxTemp.text = ""
        adapterCityWeatherList = AdapterCityWeatherList(context!!, ArrayList())
        rvWeatherList.adapter = adapterCityWeatherList
        tvSunrise.text = ""
        tvSunset.text = ""
        imgWindMill.setImageDrawable(null)
        tvDirection.text = ""
        tvSpeed.text = ""
        tvPressure.text = ""
        tvVisibility.text = ""
        tvHumidity.text = ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatterTime(zoneId: String):DateTimeFormatter{
        return DateTimeFormatter
                .ofPattern("h:mm a", Locale.ENGLISH)
                .withZone(ZoneId.of(zoneId))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(time: Instant?, zoneId: String): String? {
        return formatterTime(zoneId).format(time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNewData(context: Context, isInsert: Boolean, dialog: KProgressHUD, jsonObject: JSONObject) {
        try {
            if (jsonObject.length() > 0) {
                nestedSv.visibility = View.VISIBLE
                val jsonObjmain = jsonObject.getJSONObject("main")
                val jsonObjSys = jsonObject.getJSONObject("sys")
                if (jsonObjmain.length() > 0 && jsonObjSys.length()  > 0) {
                    try {
                        val jArray: JSONArray = jsonObject.getJSONArray("weather")
                        if (jArray.length() > 0) {
                            val jsonObjectImage = jArray.getJSONObject(0)

                            Picasso.get()
                                    .load(CommonConstants.IconLoadUrl + "" + jsonObjectImage.getString("icon").plus(".png"))
                                    .noFade()
                                    .into(imgCurrentWeather);

                        }
                        tvDate.text = CommonUtilities.getDateFromTimeStampForMain(context, jsonObject.getLong("dt"))

//                        val currentTemp = CommonUtilities.convertFahrenheitToCelcius(jsonObjmain.getInt("temp").toString()).plus("°")
                        val currentTemp = CommonUtilities.changeTemp(jsonObjmain.getInt("temp").toString()).plus("°")
                        tvCurrentTemp.text = currentTemp
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    try {
                        val tempMinMax = "Max: " + CommonUtilities.
                        changeTemp(jsonObjmain.getInt("temp_max").toString()) + "°    Min: " + CommonUtilities.changeTemp(jsonObjmain.getInt("temp_min").toString()) + "°"
                        tvCurrentMinMaxTemp.text = tempMinMax
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }



                    try {
                        val zoneId: String = Calendar.getInstance().timeZone.toZoneId().toString()
                        tvSunrise.text = formatTime(Instant.ofEpochSecond(jsonObjSys.getInt("sunrise").toLong()), zoneId)
                        tvSunset.text = formatTime(Instant.ofEpochSecond(jsonObjSys.getInt("sunset").toLong()), zoneId)
//                        formatterTime().format(time)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        Glide.with(context)
                                .load(R.raw.wind_compressed)
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                                .into(imgWindMill)


                        val direction = "Direction: " + getDirectionFromDegrees(jsonObject.getJSONObject("wind").getInt("deg").toFloat())
                        tvDirection.text = direction

//                        val speed = "Speed: " + getConvertedSpeed(jsonObject.getJSONObject("wind").getInt("speed").toFloat())
                        val speed = "Speed: " + jsonObject.getJSONObject("wind").getInt("speed").toString()
                        tvSpeed.text = speed
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        tvPressure.text = jsonObjmain.getString("pressure").toString()
                        tvVisibility.text =(jsonObject.getInt("visibility")/100).toString()
                        tvHumidity.text = jsonObjmain.getString("humidity").toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }


                    try {
                        val serviceUrl = CommonUtilities.getCityWiseForecast(jsonObject.getJSONObject("coord").getLong("lat"),
                                jsonObject.getJSONObject("coord").getLong("lon"))
                        val jsonObjReq = object : JsonObjectUTF8(Method.GET, serviceUrl, null, Response.Listener { jsonObject ->
                            if (jsonObject.length() > 0) {
                                val jArray: JSONArray = jsonObject.getJSONArray("daily")

                                val length: Int = jArray.length()

                                cityWeatherClassArrayList = ArrayList()
                                for (i in 0 until length) {
                                    val aClass = CityWeatherClass()
                                    val jObj = jArray.getJSONObject(i)
                                    val date = CommonUtilities.getDateFromTimeStampForList(context, jObj.getLong("dt"))
                                    val minTemp = jObj.getJSONObject("temp").getInt("min")
                                    val maxTemp = jObj.getJSONObject("temp").getInt("max")
                                    val jsonArrayImage: JSONArray = jObj.getJSONArray("weather")
                                    var imageUrl =""
                                    if (jsonArrayImage.length() > 0) {
                                        val jsonObjectImage = jsonArrayImage.getJSONObject(0)
                                        imageUrl = CommonConstants.IconLoadUrl + "" + jsonObjectImage.getString("icon").plus(".png")
                                    }
//                                    val imageUrl = CommonConstants.IconLoadUrl+""+jObj.getJSONObject("weather").getString("icon").plus(".png")


                                    aClass.weatherIconUrl = imageUrl
                                    aClass.weatherDate = date
                                    aClass.tempMax = CommonUtilities.changeTemp(maxTemp.toString()).plus("° ")
                                    aClass.tempMin = CommonUtilities.changeTemp(minTemp.toString()).plus("° ")

                                    cityWeatherClassArrayList.add(aClass)
                                }
                                adapterCityWeatherList = AdapterCityWeatherList(context, cityWeatherClassArrayList)
                                rvWeatherList.adapter = adapterCityWeatherList
                            }
                        }, Response.ErrorListener { e ->
                            e.printStackTrace()
                        }) {
                            override fun getHeaders(): HashMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-Type"] = "application/json"
                                return headers
                            }
                        }
                        jsonObjReq.retryPolicy = CommonUtilities.retryPolicy
                        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjReq)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (isInsert) {
                        val dbHelper = DataBaseHelper(context)
                        dbHelper.deleteInsertOfflineData(jsonObject.toString())
                    }
                }
            }else {
                dismissDialog(dialog, CommonConstants.MsgNoDataFound)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dismissDialog(dialog, CommonConstants.MsgSomethingWrong)
        }
    }


    private fun setCityWeatherData(context: Context, isInsert: Boolean, dialog: KProgressHUD, jsonObject: JSONObject) {
        try {
            if (jsonObject.length() > 0) {
                nestedSv.visibility = View.VISIBLE
                val jsonObjLocation = jsonObject.getJSONObject("location")
                val jsonObjCurrentObservation = jsonObject.getJSONObject("current_observation")
                val jsonArrForecasts = jsonObject.getJSONArray("forecasts")
                if (jsonObjLocation.length() > 0 && jsonObjCurrentObservation.length() > 0 && jsonArrForecasts.length() > 0) {
                    val jsonObjFirstData = jsonArrForecasts.getJSONObject(0)
                    try {
                        var code = jsonObjFirstData.getString("code")
                        if (code == "na") {
                            code = "-1"
                        }
                        imgCurrentWeather.setImageResource(CommonUtilities.getWeatherDrawableFromCode(code.toInt()))

                        tvDate.text = CommonUtilities.getDateFromTimeStampForMain(context, jsonObjCurrentObservation.getLong("pubDate"))

                        val currentTemp = CommonUtilities.convertFahrenheitToCelcius(jsonObjCurrentObservation.getJSONObject("condition")
                                .getString("temperature")).plus("°")
                        tvCurrentTemp.text = currentTemp
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        val tempMinMax = "Max: " + CommonUtilities.convertFahrenheitToCelcius(jsonObjFirstData.getString("high")) + "°    Min: " + CommonUtilities.convertFahrenheitToCelcius(jsonObjFirstData.getString("low")) + "°"
                        tvCurrentMinMaxTemp.text = tempMinMax
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        cityWeatherClassArrayList = ArrayList()
                        for (i in 0 until jsonArrForecasts.length()) {
                            val jsonObjData = jsonArrForecasts.getJSONObject(i)

                            val aClass = CityWeatherClass()
                            var code = jsonObjData.getString("code")
                            if (code == "na") {
                                code = "-1"
                            }
//                            aClass.weatherCode = CommonUtilities.getWeatherDrawableFromCode(code.toInt())
                            aClass.weatherDate = CommonUtilities.getDateFromTimeStampForList(context, jsonObjData.getLong("date"))
                            aClass.tempMax = CommonUtilities.convertFahrenheitToCelcius(jsonObjData.getString("high")).plus("° ")
                            aClass.tempMin = CommonUtilities.convertFahrenheitToCelcius(jsonObjData.getString("low")).plus("° ")

                            cityWeatherClassArrayList.add(aClass)
                        }
                        adapterCityWeatherList = AdapterCityWeatherList(context, cityWeatherClassArrayList)
                        rvWeatherList.adapter = adapterCityWeatherList
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val jsonObjAstronomy = jsonObjCurrentObservation.getJSONObject("astronomy")
                        tvSunrise.text = jsonObjAstronomy.getString("sunrise")
                        tvSunset.text = jsonObjAstronomy.getString("sunset")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        Glide.with(context)
                                .load(R.raw.wind_compressed)
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                                .into(imgWindMill)

                        val jsonObjWind = jsonObjCurrentObservation.getJSONObject("wind")

                        val direction = "Direction: " + getDirectionFromDegrees(jsonObjWind.getString("direction").toFloat())
                        tvDirection.text = direction

                        val speed = "Speed: " + getConvertedSpeed(jsonObjWind.getString("speed").toFloat())
                        tvSpeed.text = speed
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        val jsonObjAtmosphere = jsonObjCurrentObservation.getJSONObject("atmosphere")
                        tvPressure.text = jsonObjAtmosphere.getString("pressure")
                        tvVisibility.text = jsonObjAtmosphere.getString("visibility")
                        tvHumidity.text = jsonObjAtmosphere.getString("humidity")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }

                    if (isInsert) {
                        val dbHelper = DataBaseHelper(context)
                        dbHelper.deleteInsertOfflineData(jsonObject.toString())
                    }
                } else {
                    dismissDialog(dialog, CommonConstants.MsgNoDataFound)
                }
            } else {
                dismissDialog(dialog, CommonConstants.MsgNoDataFound)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dismissDialog(dialog, CommonConstants.MsgSomethingWrong)
        }
    }

    private fun getDirectionFromDegrees(degree: Float): String {
        val directionsArrays = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val moduleInd = (degree + 11.25) / 22.5
        val index = moduleInd % 16
        return directionsArrays[index.toInt()]
    }

    private fun getConvertedSpeed(speed: Float): String {
        val mph = (speed / 2.24).toInt()
        return mph.toString()
    }

    private fun dismissDialog(dialog: KProgressHUD, msg: String) {
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            CustomToast.showToast(context, msg)
            clearAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndSetData() {
        val dialog = CustomProgressDialog().showProgressDialog(context!!)
        if (NetworkUtil.getConnectivityStatus(context!!)) {
            val city = PreferenceManager.getDefaultSharedPreferences(context!!).getString(CommonConstants.SelectedCity, "")
//            callCityWeatherService(context!!, dialog, city!!)
            callApiCityWeatherData(context!!, dialog, city!!)
        } else {
            setOfflineData(dialog)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setOfflineData(dialog: KProgressHUD) {
        try {
            val dbHelper = DataBaseHelper(context!!)
            val offlineData = dbHelper.getOfflineData()
            if (offlineData != null && offlineData.isNotEmpty()) {
                try {
                    val jsonObject = JSONObject(offlineData)
//                    setCityWeatherData(context!!, false, dialog, jsonObject)
                    setNewData(context!!, false, dialog, jsonObject)

                } catch (e: Exception) {
                    e.printStackTrace()
                    dismissDialog(dialog, CommonConstants.MsgSomethingWrong)
                }
            } else {
                dismissDialog(dialog, CommonConstants.MsgSomethingWrong)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dismissDialog(dialog, CommonConstants.MsgSomethingWrong)
        }
    }


    private fun dismissDialog(context: Context, dialog: KProgressHUD, msg: String) {
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            if (msg.isNotEmpty()) {
                CustomToast.showToast(context, msg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}