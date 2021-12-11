package com.ramalingam.localforecast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.interfaces.AdapterItemCallBack

class AdapterCityList(private val context: Context,
                      private val cityArrayList: ArrayList<String>,
                      private val adapterItemCallBack: AdapterItemCallBack) : RecyclerView.Adapter<AdapterCityList.AdapterViewHolder>() {

    inner class AdapterViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        var llMain = view.findViewById(R.id.llMainClCityList) as LinearLayout
        var tvCityName = view.findViewById(R.id.tvCityNameClCityList) as AppCompatTextView

        init {
            llMain.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val id = view.id
            if (id == R.id.llMainClCityList) {
                adapterItemCallBack.onMethodCallBack(view.tag as Int)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_city_list, parent, false)
        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {

        try {
            holder.tvCityName.text = cityArrayList[position]

            holder.llMain.tag = position
            holder.tvCityName.tag = position
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return cityArrayList.size
    }
}