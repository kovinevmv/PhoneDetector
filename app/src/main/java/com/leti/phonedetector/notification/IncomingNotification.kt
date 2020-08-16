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

class IncomingNotification(val context: Context, val intent: Intent, val phone: PhoneInfo) {

    private fun createNotification(): Notification {
        val snoozePendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(phone.number + "\n" + phone.tags.joinToString(separator = "\n"))
        bigText.setBigContentTitle(phone.name)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            setContentTitle(phone.name)
            setContentText(phone.number)
            if (phone.tags.isNotEmpty()) setStyle(bigText)
            setContentIntent(snoozePendingIntent)
            setAutoCancel(true)
            setSound(null)
            priority = NotificationCompat.PRIORITY_HIGH
        }

        return builder.build()
    }

    fun notifyNow() {
        val notification = createNotification()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}
