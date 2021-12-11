package com.ramalingam.localforecast.fragments;

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import com.ramalingam.localforecast.R
import java.net.URL
import java.util.*

class WindSpeedFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_wind_speed, container, false)
        initViews(view)
//        loadData()
        return view
    }

    private fun initViews(view: View) {
        try {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
            mapFragment.getMapAsync(this)
                  } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled =true
        mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                val url = URL(
                        String.format(
                                Locale.GERMAN,
                                "https://tile.openweathermap.org/map/wind_new/%d/%d/%d.png?appid=ec18dc7daf98ed51e8c7f6b4b9f0a5e9",
                                zoom,
                                x,
                                y
                        )
                )
                Log.e("TAG", "getTileUrl:::::URL::: $url" )
                return url
            }
        }).visible(true))

        mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID;

    }
}