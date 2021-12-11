package com.ramalingam.localforecast.common;

class CommonConstants {

    companion object {

        const val YahooAppId = "V06Dlg7i"
        const val YahooClientId = "dj0yJmk9YlNLZ1lTcVZVMWFsJnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PWYy"
        const val YahooClientSecret = "8d629d6f1ae75cc191c4c9189d41af61a773cda0"


        const val LogTag = "<><>"

        const val CapVersion = "Version : "
        const val CapInternet = "No internet Connection"

        const val BtnCancel = "Cancel"
        const val BtnRetry = "Retry"

        const val MsgSomethingWrong = "Something went wrong. please try again!"
        const val MsgNoDataFound = "No data found. Please try again!."
        const val MsgNoInternet = "Please turn on internet connection to continue."
        const val MsgSelectCity = "Please select your city."

        const val ReqCodeLocationPermission = 111
        const val ReqCodeCitySelection = 222

        const val SelectedCity = "SelectedCity"
        const val ItemObject = "ItemObject"
        const val IsFirstTime = "IsFirstTime"

        //Location Related
        const val SUCCESS_RESULT = 0
        const val FAILURE_RESULT = 1
        private const val PACKAGE_NAME = "com.live.weather.forcast.temperature.report"
        const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
        const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
        const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"


        const val CityWeatherFg = "CityWeatherFg"
        const val LiveWeatherFg = "LiveWeatherFg"
//        const val RainFallForecastFg = "RainFallForecastFg"
        const val RainFallForecastFg = "WindSpeed"
        const val TemperatureForecastFg = "TemperatureForecastFg"
        const val WeatherMapFg = "WeatherMapFg"

        const val TitleLiveWeather = "Live Weather"
//        const val TitleRainFallForecast = "Rainfall Forecast"
        const val TitleRainFallForecast = "Wind Speed"
        const val TitleTemperatureForecast = "Temperature Forecast"
        const val TitleWeatherMap = "Weather Map"
        const val IconLoadUrl = "http://openweathermap.org/img/w/"

        const val GOOGLE_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val GOOGLE_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val GOOGLE_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

        const val FB_BANNER_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"
        const val FB_INTERSTITIAL_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID"

        const val AD_FACEBOOK = "facebook"
        const val AD_GOOGLE = "google"
        const val AD_TYPE_FACEBOOK_GOOGLE = AD_GOOGLE

        const val ENABLE = "Enable"
        const val DISABLE = "Disable"
        const val ENABLE_DISABLE = ENABLE

        const val AD_TYPE_FB_GOOGLE = "AD_TYPE_FB_GOOGLE"
        const val GOOGLE_BANNER = "GOOGLE_BANNER"
        const val GOOGLE_INTERSTITIAL = "GOOGLE_INTERSTITIAL"
        const val FB_BANNER = "FB_BANNER"
        const val FB_INTERSTITIAL = "FB_INTERSTITIAL"
        const val SPLASH_SCREEN_COUNT = "splash_screen_count"
        const val STATUS_ENABLE_DISABLE = "STATUS_ENABLE_DISABLE"
    }
}