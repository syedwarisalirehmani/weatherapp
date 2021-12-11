package com.ramalingam.localforecast.common;

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.android.volley.DefaultRetryPolicy
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.interfaces.ConfirmDialogCallBack
import com.ramalingam.localforecast.pojo.WeatherClass
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class CommonUtilities {

    companion object {

        var cityNameMain = ""
        var authorizationLine = ""
//        var apiKey ="20a2b26cb3684a9be96ca2393cdb40a7"
        var apiKey ="ec18dc7daf98ed51e8c7f6b4b9f0a5e9"
        val retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        var cityStaticArrayList = ArrayList<String>()

        @SuppressLint("ServiceCast")
        fun hideKeyBoard(activity: Activity) {
            try {
                val view = activity.currentFocus
                if (view != null) {
                    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getLiveWeatherList(): ArrayList<WeatherClass> {

            val weatherClassArrayList = ArrayList<WeatherClass>()

            var aClass = WeatherClass()
            aClass.title = "Infra Red"
//            aClass.weatherUrl = "http://satellite.imd.gov.in/imc/3Dasiasec_ir1.jpg"
            aClass.weatherUrl = "https://www.mosdac.gov.in/live/index_one.php?url_name=india"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Satellite"
            aClass.weatherUrl = "http://tropic.ssec.wisc.edu/real-time/indian/images/xxirm5bbm.jpg"
            weatherClassArrayList.add(aClass)

           /* aClass = WeatherClass()
            aClass.title = "Heat Map"
//            aClass.weatherUrl = "http://satellite.imd.gov.in/img/3Dasiasec_bt1.jpg"
//            aClass.weatherUrl = "https://openweathermap.org/weathermap?basemap=map&cities=false&layer=temperature&lat=30&lon=-20&zoom=3"
            aClass.weatherUrl = "https://tile.openweathermap.org/map/temp_new/1/1/1.png?appid=ec18dc7daf98ed51e8c7f6b4b9f0a5e9"
            weatherClassArrayList.add(aClass)*/

            aClass = WeatherClass()
            aClass.title = "Wind Direction"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/india4.00hr.png"

            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Color Composite"
            aClass.weatherUrl = "http://tropic.ssec.wisc.edu/real-time/indian/images/irnm5.GIF"
            weatherClassArrayList.add(aClass)

            return weatherClassArrayList
        }

        fun getRainFallForecastList(): ArrayList<WeatherClass> {
            val weatherClassArrayList = ArrayList<WeatherClass>()

            var aClass = WeatherClass()
            /*aClass.title = "3 Hour"
            aClass.weatherUrl = "http://satellite.imd.gov.in/img/3Dhalfhr_qpe.jpg"
            weatherClassArrayList.add(aClass)*/

            aClass = WeatherClass()
            aClass.title = "Tomorrow"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/24hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 2"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/48hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 3"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/72hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 4"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/96hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 5"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/120hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 6"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/144hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 7"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/168hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 8"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/192hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 9"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/216hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "Day 10"
            aClass.weatherUrl = "http://nwp.imd.gov.in/gfs/240hGFS1534rain.gif"
            weatherClassArrayList.add(aClass)

            return weatherClassArrayList
        }

        fun getTemperatureForecastList(): ArrayList<WeatherClass> {
            val weatherClassArrayList = ArrayList<WeatherClass>()

            var aClass = WeatherClass()
            aClass.title = "ALL"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/temp12.png"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "South"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/ezindia1_day1.png"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "North"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/ezindia3_day1.png"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "East"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/ezindia4_day1.png"
            weatherClassArrayList.add(aClass)

            aClass = WeatherClass()
            aClass.title = "West"
            aClass.weatherUrl = "http://www.monsoondata.org/wx2/ezindia2_day1.png"
            weatherClassArrayList.add(aClass)

            return weatherClassArrayList
        }

        fun getWeatherMapUrl(): String {
            return "http://moba.ciao.jp/and/cloud2/c/index5b.html?basemap=satellite&cities=true&layer=precipitation&lat=23.2413&lon=60.8203&zoom=2"
        }

        fun getCityWiseWeatherUrl(cityName: String):String{
            return "http://api.openweathermap.org/data/2.5/weather?q=$cityName&APPID=$apiKey"
        }

        fun getCityWiseForecast(latitude: Long, longitude: Long):String{
            return "https://api.openweathermap.org/data/2.5/onecall?lat=$latitude&lon=$longitude&appid=$apiKey&exclude=current,minutely,hourly"
        }

        fun getCityWeatherUrl(cityName: String): String {
//            //val param  = URLEncoder.encode(cityName, "utf-8")
//            var url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text='$cityName')and%20u='c'&format=json"
//            var url = "https://query.yahooapis.com/v1/public/yql?q=select * from weather.forecast where woeid in (select woeid from geo.places(1) where text='$cityName')and u='c'&format=json"
//            url =  url.replace(" ", "%20")
//            Log.e(CommonConstants.LogTag, url)
//            return url
            val url = "https://weather-ydn-yql.media.yahoo.com/forecastrss?location=$cityName&format=json"
//            val url = "http://weather.yahooapis.com/forecastrss?location=$cityName&format=json"
            return url
        }

        fun getAuthorizationLine(cityName: String): String {
//val data1 = (Date().time / 1000).toString()
//val data2 = ((System.currentTimeMillis() - 1000) / 1000).toString()
//(Date().time / 1000).toString()+ "  --  "+((System.currentTimeMillis() - 1000) / 1000).toString()
//(Date().time).toString()+ "  --  "+((System.currentTimeMillis() - 1000)).toString()
//SimpleDateFormat("dd/MM/y").format(Date(1547636105418))

            val consumerKey = CommonConstants.YahooClientId
            val consumerSecret = CommonConstants.YahooClientSecret
            val url = "https://weather-ydn-yql.media.yahoo.com/forecastrss"

            val timestamp = Date().time / 1000
            val nonce = ByteArray(32)
            val rand = Random()
            rand.nextBytes(nonce)
            val oauthNonce = String(nonce).replace("\\W".toRegex(), "")

            val parameters = ArrayList<String>()
            parameters.add("oauth_consumer_key=$consumerKey")
            parameters.add("oauth_nonce=$oauthNonce")
            parameters.add("oauth_signature_method=HMAC-SHA1")
            parameters.add("oauth_timestamp=$timestamp")
            parameters.add("oauth_version=1.0")
            parameters.add("location=" + URLEncoder.encode(cityName, "UTF-8"))
            parameters.add("format=json")
            parameters.sort()

            val parametersList = StringBuffer()
            for (i in parameters.indices) {
                parametersList.append((if (i > 0) "&" else "") + parameters[i])
            }


            val signatureString = "GET&" +
                    URLEncoder.encode(url, "UTF-8") + "&" +
                    URLEncoder.encode(parametersList.toString(), "UTF-8")

            var signature: String? = null
            var rawHMAC: ByteArray? = null
            try {
                val signingKey = SecretKeySpec("$consumerSecret&".toByteArray(), "HmacSHA1")
                val mac = Mac.getInstance("HmacSHA1")
                mac.init(signingKey)
                rawHMAC = mac.doFinal(signatureString.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val encoder = java.util.Base64.getEncoder()
                    signature = encoder.encodeToString(rawHMAC)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    signature = android.util.Base64.encodeToString(rawHMAC, android.util.Base64.NO_WRAP);
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return "OAuth " +
                    "oauth_consumer_key=\"" + consumerKey + "\", " +
                    "oauth_nonce=\"" + oauthNonce + "\", " +
                    "oauth_timestamp=\"" + timestamp + "\", " +
                    "oauth_signature_method=\"HMAC-SHA1\", " +
                    "oauth_signature=\"" + signature + "\", " +
                    "oauth_version=\"1.0\""
        }

        fun getWeatherDrawableFromCode(code: Int): Int {

            var drawable = R.drawable.ana
            when (code) {
                -1 -> drawable = R.drawable.ana
                0 -> drawable = R.drawable.a0
                1 -> drawable = R.drawable.a1
                2 -> drawable = R.drawable.a2
                3 -> drawable = R.drawable.a3
                4 -> drawable = R.drawable.a4
                5 -> drawable = R.drawable.a5
                6 -> drawable = R.drawable.a6
                7 -> drawable = R.drawable.a7
                8 -> drawable = R.drawable.a8
                9 -> drawable = R.drawable.a9
                10 -> drawable = R.drawable.a10
                11 -> drawable = R.drawable.a11
                12 -> drawable = R.drawable.a12
                13 -> drawable = R.drawable.a13
                14 -> drawable = R.drawable.a14
                15 -> drawable = R.drawable.a15
                16 -> drawable = R.drawable.a16
                17 -> drawable = R.drawable.a17
                18 -> drawable = R.drawable.a18
                19 -> drawable = R.drawable.a19
                20 -> drawable = R.drawable.a20
                21 -> drawable = R.drawable.a21
                22 -> drawable = R.drawable.a22
                23 -> drawable = R.drawable.a23
                24 -> drawable = R.drawable.a24
                25 -> drawable = R.drawable.a25
                26 -> drawable = R.drawable.a26
                27 -> drawable = R.drawable.a27
                28 -> drawable = R.drawable.a28
                29 -> drawable = R.drawable.a29
                30 -> drawable = R.drawable.a30
                31 -> drawable = R.drawable.a31
                32 -> drawable = R.drawable.a32
                33 -> drawable = R.drawable.a33
                34 -> drawable = R.drawable.a34
                35 -> drawable = R.drawable.a35
                36 -> drawable = R.drawable.a36
                37 -> drawable = R.drawable.a37
                38 -> drawable = R.drawable.a38
                39 -> drawable = R.drawable.a39
                40 -> drawable = R.drawable.a40
                41 -> drawable = R.drawable.a41
                42 -> drawable = R.drawable.a42
                43 -> drawable = R.drawable.a43
                44 -> drawable = R.drawable.a44
                45 -> drawable = R.drawable.a45
                46 -> drawable = R.drawable.a46
                47 -> drawable = R.drawable.a47
            }
            return drawable
        }


        fun getDateFormat(context: Context): String {
            val dateFormat = android.text.format.DateFormat.getDateFormat(context)
            val pattern = (dateFormat as SimpleDateFormat).toLocalizedPattern()
            return pattern;
        }

        fun getDateFromTimeStampForMain(context: Context, timeStamp: Long): String {
            val formatter = SimpleDateFormat("EEE MMM dd, yyyy hh:mm a", Locale.getDefault());
            return formatter.format(Date(timeStamp * 1000))
        }

        fun getDateFromTimeStampForList(context: Context, timeStamp: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return formatter.format(Date(timeStamp * 1000))
        }

        fun convertFahrenheitToCelcius(fahrenheit: String): String {
            return String.format("%.2f", (fahrenheit.toFloat() - 32) * 5 / 9)
        }

        // Converts to fahrenheit
        fun convertCelciusToFahrenheit(celsius: Float): Float {
            return celsius * 9 / 5 + 32
        }

        fun changeTemp(x: String): String{
            val celsius = x.toDouble() - 273.0
            val i = celsius.toInt()
            return i.toString()
        }

        fun confirmationDialog(
                content: Context,
                confirmCallBack: ConfirmDialogCallBack,
                strTitle: String,
                strMsg: String
        ): Boolean {

            val builder1 = AlertDialog.Builder(content)
            builder1.setTitle(strTitle)
            builder1.setMessage(strMsg)
            builder1.setCancelable(true)

            builder1.setPositiveButton("Yes") { dialog, _ ->
                dialog.cancel()
                confirmCallBack.Okay()
            }

            builder1.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                confirmCallBack.cancel()
            }

            val alert11 = builder1.create()
            alert11.show()

            return false
        }

    }
}