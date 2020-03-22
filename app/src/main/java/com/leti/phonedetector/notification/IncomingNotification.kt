package com.leti.phonedetector.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.leti.phonedetector.CHANNEL_ID
import com.leti.phonedetector.R
import com.leti.phonedetector.model.PhoneInfo

class IncomingNotification(val context: Context, val intent : Intent, val phone : PhoneInfo) {

    private fun createNotification() : Notification {
        val snoozePendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            setContentTitle(phone.number)
            setContentText(phone.name)
            if (phone.tags.isNotEmpty()) setStyle(NotificationCompat.BigTextStyle().bigText(phone.tags.joinToString(separator ="\n")))
            setContentIntent(snoozePendingIntent)
            setAutoCancel(true)
            setSound(null)
        }

        return builder.build()
    }

    fun notifyNow(){
        val notification = createNotification()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}