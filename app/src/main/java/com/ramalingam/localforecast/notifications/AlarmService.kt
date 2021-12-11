package com.ramalingam.localforecast.notifications

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ramalingam.localforecast.R
import com.ramalingam.localforecast.activities.MainActivity


class AlarmService : IntentService("AlarmService") {

    override fun onHandleIntent(intent: Intent?) {
        try {


            val context = this.applicationContext
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mIntent = Intent(context, MainActivity::class.java)
            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val icon = BitmapFactory.decodeResource(context.resources, R.drawable.app_icon_256)
            val builder = NotificationCompat.Builder(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.notification_icon)
                builder.color = context.resources.getColor(R.color.colorTheme)
            } else {
                builder.setSmallIcon(R.drawable.notification_icon)
            }
            builder.setContentTitle(context.resources.getString(R.string.app_name))
            builder.setContentText("Stay tuned with us and enjoy App !")
            builder.setLargeIcon(icon)
            builder.setAutoCancel(true)
            builder.setContentIntent(pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}