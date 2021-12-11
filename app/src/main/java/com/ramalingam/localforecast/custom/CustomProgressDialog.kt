package com.ramalingam.localforecast.custom;


import android.content.Context
import com.kaopiz.kprogresshud.KProgressHUD
import com.ramalingam.localforecast.R

public class CustomProgressDialog {

    fun showProgressDialog(context : Context) : KProgressHUD {
        lateinit var dialog : KProgressHUD
        try {
            dialog =  KProgressHUD.create(context)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .setBackgroundColor(context.resources.getColor(R.color.colorTheme))
                    .setAnimationSpeed(1)
                    .setDimAmount(0f)
                    .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return  dialog
    }
}
/*


                        class CustomProgressDialog {
    companion object {
        private lateinit var pDialogMain : KProgressHUD
        fun showProgressDialog(context : Context) : KProgressHUD {
            if (pDialogMain!=null) {
                return  pDialogMain
            } else{

                val  pDialog = KProgressHUD.create(context)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setCancellable(false)
                        .setBackgroundColor(context.resources.getColor(R.color.colorTheme))
                        .setAnimationSpeed(1)
                        .setDimAmount(0f)
                pDialogMain = pDialog
                return pDialogMain
            }
        }
    }
}
*/