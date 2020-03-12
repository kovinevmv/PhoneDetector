package com.leti.phonedetector

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.leti.phonedetector.api.NeberitrubkuAPI
import com.leti.phonedetector.database.PhoneLogDBHelper
import java.text.SimpleDateFormat
import java.util.*


class PhoneStateReceiver : BroadcastReceiver() {
    
    @SuppressLint("UnsafeProtectedBroadcastReceiver", "SimpleDateFormat")
    override fun onReceive(context: Context, intent: Intent) {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isRun = sharedPreferences.getBoolean("activate_phone_detection_switch",false)
        val notFindInContacts = sharedPreferences.getBoolean("disable_search_in_contacts_switch",false)
        val showEmptyUser = sharedPreferences.getBoolean("show_empty_user", false)
        val isCreatePushUp = sharedPreferences.getBoolean("notification_switch", false)
        val delayNotificationTime = sharedPreferences.getInt("time_notification", 1)

        if (!isRun) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Handler().postDelayed({
                    if (incomingNumber != null) {
                        if (notFindInContacts){
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                                val contactName = getContactName(incomingNumber, context)
                                if (contactName != null) return@postDelayed
                            }
                        }

                        val user = startPhoneDetection(context, incomingNumber)
                        if (!user.toPhoneInfo().isDefault() || showEmptyUser) {
                            val mIntent = createIntent(context, user.toPhoneInfo(), false)
                            context.startActivity(mIntent)
                        }

                    }
                }, 100)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {}
            TelephonyManager.EXTRA_STATE_IDLE -> {
                if (incomingNumber != null && isCreatePushUp){
                    val user = findUserByPhone(context, incomingNumber)
                    if (user.isSpam) createSheduledPushUp(context, createPushUp(context, user), delayNotificationTime)
                }
            }
        }
    }

    private fun findUserByPhone(context : Context, number : String) : PhoneInfo{
        val db = PhoneLogDBHelper(context)
        return db.findPhoneByNumber(number) ?: PhoneInfo(number=number)
    }

    private fun createPushUp(context : Context, user : PhoneInfo) : Notification{
        val intent = createIntent(context, user, true)
        val snoozePendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        val builder = NotificationCompat.Builder(context, "PHONEDETECTOR_CHANNEL_ID")
            .setSmallIcon(android.R.drawable.alert_dark_frame)
            .setContentTitle("Don't forget to block incoming number")
            .setContentText("${user.number} - ${user.name}")
            .setContentIntent(snoozePendingIntent)

        return builder.build()
    }

    private fun createSheduledPushUp(context: Context, notification : Notification, delayTime : Int){
        val notificationIntent = Intent(context, NotificationPublisher::class.java)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1)
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val futureInMillis = SystemClock.elapsedRealtime() + delayTime * 60 * 1000
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis] = pendingIntent

    }
    private fun createIntent(context: Context, user: PhoneInfo, isDisplayButtons : Boolean) : Intent{
        val mIntent = Intent(context, OverlayActivity::class.java)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        mIntent.putExtra("user", user)
        mIntent.putExtra("is_display_buttons", isDisplayButtons)
        return mIntent
    }

    private fun startPhoneDetection(context: Context, incomingNumber : String) : PhoneLogInfo{

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timeout = sharedPreferences.getInt("detection_delay_seekbar", 5)
        val isNetworkOnly = sharedPreferences.getBoolean("use_only_network_info",true)

        val db = PhoneLogDBHelper(context)

        val date = SimpleDateFormat("yyyy.MM.dd").format(Date())
        val time = SimpleDateFormat("HH:mm:ss").format(Date())

        val user: PhoneLogInfo = if (isNetworkOnly){
            findUserByNetwork(incomingNumber, timeout, time, date)
        }
        else{
            val foundUser : PhoneInfo? = db.findPhoneByNumber(incomingNumber)

            if (foundUser != null && !foundUser.isDefault()) {
                PhoneLogInfo(foundUser, time, date)
            }
            else{
                val foundUserNetwork = findUserByNetwork(incomingNumber, timeout, time, date)

                if (!foundUserNetwork.toPhoneInfo().isDefault()){
                    foundUserNetwork
                }
                else {
                    PhoneLogInfo(number=incomingNumber, date=date, time=time)
                }
            }
        }

        db.insertPhone(user)
        return user
    }

    private fun findUserByNetwork(number : String, timeout : Int, time : String, date : String) : PhoneLogInfo{
        val newUser = NeberitrubkuAPI(number, timeout).getUser()
        // TODO add here GetContact
        return PhoneLogInfo(newUser, time=time, date=date)
    }

    private fun getContactName(phoneNumber: String?, context: Context): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName : String? = null
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }
        return contactName
    }
}