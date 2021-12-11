package com.ramalingam.localforecast.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.StrictMode
import android.text.format.DateUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ramalingam.localforecast.R
import org.json.JSONObject
import java.net.URL
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {


    lateinit var context: Context
    var arrOfImage = ArrayList<String>()

    override fun onMessageReceived(message: RemoteMessage) {
        context = applicationContext
        arrOfImage = ArrayList()

        if (message.data.containsKey("data")) {
            val jsonObject = JSONObject(message.data["data"])
            arrOfImage.add(jsonObject.optString("image1"))
            arrOfImage.add(jsonObject.optString("image2"))
            Log.e("TAG", "onMessageReceived::Image1  " + jsonObject.optString("image1"))
        }
        generateNotification(message)
    }

    private fun generateNotification(p0: RemoteMessage) =
            try {


        val context = this.applicationContext
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var mIntent: Intent? = null



        val channelId = "11111"
        try {

            val channelName = context.resources.getString(R.string.app_name)
            val channelDescription = "Application_name Alert"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(channelId, channelName, importance)
                mChannel.description = channelDescription

                mChannel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    mChannel.audioAttributes
                )
                notificationManager.createNotificationChannel(mChannel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val builder = NotificationCompat.Builder(this, channelId)

        val expandedView = RemoteViews(packageName, R.layout.item_notification_expand)
        val collapsedView = RemoteViews(packageName, R.layout.item_notification_coll)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.notification_icon)
            builder.color = ContextCompat.getColor(context, R.color.colorBlack)

            expandedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
            expandedView.setTextViewText(
                R.id.timestamp,
                DateUtils.formatDateTime(
                    this,
                    System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME
                )
            )

            collapsedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
            collapsedView.setTextViewText(
                R.id.timestamp,
                DateUtils.formatDateTime(
                    this,
                    System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME
                )
            )
        } else {
            builder.setSmallIcon(R.drawable.notification_icon)

            expandedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
            expandedView.setTextViewText(
                R.id.timestamp,
                DateUtils.formatDateTime(
                    this,
                    System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME
                )
            )

            collapsedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
            collapsedView.setTextViewText(
                R.id.timestamp,
                DateUtils.formatDateTime(
                    this,
                    System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_TIME
                )
            )

        }

        val title = context.resources.getString(R.string.app_name)

        val data = p0.notification
        if (data != null) {

            mIntent!!.putExtra("notificationData", Gson().toJson(p0.data))
            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            try {
                expandedView.setTextViewText(R.id.title_text, data.title)

                expandedView.setTextViewText(R.id.notification_message, data.body)

                collapsedView.setTextViewText(R.id.content_text, data.body)

                collapsedView.setTextViewText(R.id.title_text, data.title)

                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)

                val image1Url = URL(data.imageUrl.toString())

                val bmp1 = BitmapFactory.decodeStream(image1Url.openConnection().getInputStream())
                if (bmp1 != null) {
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(bmp1)
                            .setSummaryText(data.body)
                    )

                    expandedView.setBitmap(R.id.notification_img, "setImageBitmap", bmp1)
                }


            } catch (e: Exception) {
                e.printStackTrace()
                builder.setContentTitle(title)
            }
        } else {
            val jsonObjectData = JSONObject(Gson().toJson(p0.data))
            val Id = jsonObjectData.optString("id")
            mIntent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            builder.setContentText(title)
        }

        builder.setCustomContentView(collapsedView)
        builder.setCustomBigContentView(expandedView)
        builder.setShowWhen(false)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setAutoCancel(true)
        builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            mIntent!!,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        builder.setContentIntent(pendingIntent)

        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}