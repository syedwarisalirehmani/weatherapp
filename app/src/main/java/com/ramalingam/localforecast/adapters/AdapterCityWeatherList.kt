package com.ramalingam.localforecast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.pojo.CityWeatherClass
import com.squareup.picasso.Picasso

class AdapterCityWeatherList(private val context: Context,
                             private val cityWeatherClassArrayList: ArrayList<CityWeatherClass>) : RecyclerView.Adapter<AdapterCityWeatherList.AdapterViewHolder>() {

    inner class AdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvDate = view.findViewById(R.id.tvDateClCityWeather) as AppCompatTextView
        var imgTemp = view.findViewById(R.id.imgTempClCityWeather) as AppCompatImageView
        var tvTempMinMax = view.findViewById(R.id.tvTempMinMaxClCityWeather) as AppCompatTextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_city_weather_list, parent, false)
        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {

        try {
            val aClass = cityWeatherClassArrayList[position]

            holder.tvDate.text = aClass.weatherDate

            Picasso.get()
                    .load(aClass.weatherIconUrl)
                    .noFade()
                    .into(holder.imgTemp);

//            holder.imgTemp.setImageResource(aClass.weatherCode)
            holder.tvTempMinMax.text = aClass.tempMin.plus(" ").plus(aClass.tempMax)

            holder.tvDate.tag = position
            holder.imgTemp.tag = position
            holder.tvTempMinMax.tag = position
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return cityWeatherClassArrayList.size
    }
}