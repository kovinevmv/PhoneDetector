package com.leti.phonedetector.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.leti.phonedetector.CHANNEL_ID
import com.leti.phonedetector.R
import com.leti.phonedetector.model.PhoneInfo

class BlockNotification(private val context: Context, private val intent : Intent, private val user : PhoneInfo) {

    private fun createNotification() : Notification {
        val snoozePendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_icon)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            setContentTitle("Don't forget to block incoming number")
            setContentText("${user.number} - ${user.name}")
            setContentIntent(snoozePendingIntent)
            setAutoCancel(true)
        }

        return builder.build()
    }

    private fun createScheduledPushUp(notification: Notification, delayTime : Int){
        val notificationIntent = Intent(context, NotificationPublisher::class.java)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)

        val pendingIntent = PendingIntent.getBroadcast(context,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val futureInMillis = SystemClock.elapsedRealtime() + delayTime * 60 * 1000
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis] = pendingIntent
    }

    fun notify(delayTime: Int){
        val notification = createNotification()
        createScheduledPushUp(notification, delayTime)
    }
}