package com.ramalingam.localforecast.fragments;


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.kaopiz.kprogresshud.KProgressHUD
import com.ramalingam.localforecast.common.CommonConstants
import com.ramalingam.localforecast.custom.CustomProgressDialog
import com.ramalingam.localforecast.custom.CustomToast
import com.ramalingam.localforecast.pojo.WeatherClass
import com.ramalingam.localforecast.volley.TouchImageView


class ChildViewWeatherFragment : Fragment(), OnMapReadyCallback {

    private lateinit var dialog: KProgressHUD
    private lateinit var imgWeather: TouchImageView
    private lateinit var webView: WebView


    companion object {
        fun newInstance(aClass: WeatherClass): ChildViewWeatherFragment {
            val frag = ChildViewWeatherFragment()
            val args = Bundle()
            args.putSerializable(CommonConstants.ItemObject, aClass)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(container!!.context).inflate(com.ramalingam.localforecast.R.layout.fragment_child_view_weather, container, false)
        initViews(view, savedInstanceState)
        loadDataFromBundle()
        return view

    }

    private fun initViews(view: View, savedInstanceState: Bundle?) {
        imgWeather = view.findViewById(com.ramalingam.localforecast.R.id.imgWeatherFgImgView)
        webView = view.findViewById(com.ramalingam.localforecast.R.id.urlWebView)


    }

    private fun loadDataFromBundle() {
        try {
            val bundle = arguments
            if (bundle != null && bundle.containsKey(CommonConstants.ItemObject)) {
                val aClass = bundle.getSerializable(CommonConstants.ItemObject) as WeatherClass

                if (aClass.title == "Infra Red") {
                    webView.visibility = View.VISIBLE
                    imgWeather.visibility = View.GONE

                    webView.webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            try {
                                dialog = CustomProgressDialog().showProgressDialog(context!!)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            try {
                                if (dialog.isShowing) {
                                    dialog.dismiss()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    webView.settings.javaScriptEnabled = true
                    webView.settings.loadWithOverviewMode = true
                    webView.settings.useWideViewPort = true
                    webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webView.loadUrl(aClass.weatherUrl)
                } else {
                    webView.visibility = View.GONE
                    imgWeather.visibility = View.VISIBLE





                    Glide.with(context!!)
                            .setDefaultRequestOptions(RequestOptions()
                                    .placeholder(com.ramalingam.localforecast.R.drawable.default_place_holder_500)
                                    .error(com.ramalingam.localforecast.R.drawable.default_place_holder_500))

                            .load(aClass.weatherUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    Log.e("TAG", "onLoadFailed:::::Child weather:::  " + e.toString())
                                    CustomToast.showToast(context, CommonConstants.MsgSomethingWrong)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }
                            })
                            .into(imgWeather)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            /*if (dialog.isShowing) {
                dialog.dismiss()
            }*/
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

    }
}