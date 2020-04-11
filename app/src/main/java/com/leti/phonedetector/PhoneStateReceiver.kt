package com.leti.phonedetector

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED
import androidx.preference.PreferenceManager
import com.leti.phonedetector.api.GetContact.GetContactAPI
import com.leti.phonedetector.api.NeberitrubkuAPI
import com.leti.phonedetector.contacts.Contacts
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneInfo
import com.leti.phonedetector.model.PhoneLogInfo
import com.leti.phonedetector.notification.BlockNotification
import com.leti.phonedetector.notification.IncomingNotification
import com.leti.phonedetector.overlay.OverlayCreator
import com.leti.phonedetector.search.Search
import java.text.SimpleDateFormat
import java.util.*


class PhoneStateReceiver : BroadcastReceiver() {
    
    @SuppressLint( "SimpleDateFormat")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PHONE_STATE_CHANGED){

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val isRun = sharedPreferences.getBoolean("activate_phone_detection_switch",false)
            val notFindInContacts = sharedPreferences.getBoolean("disable_search_in_contacts_switch",false)
            val showEmptyUser = sharedPreferences.getBoolean("show_empty_user", false)
            val isCreatePushUp = sharedPreferences.getBoolean("notification_switch", false)
            val delayNotificationTime = sharedPreferences.getInt("time_notification", 1)
            val isShowNotificationInsteadOfPopup = sharedPreferences.getBoolean("notification_instead_overlay", false)

            if (!isRun) return

            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Handler().postDelayed({
                        if (incomingNumber != null) {
                            val searcher = Search(context)
                            val formattedIncoming = searcher.formatE164NumberRU(incomingNumber)
                            if (notFindInContacts){
                                val contactName = Contacts(context).getContactNameByPhone(formattedIncoming)
                                if (contactName != null) return@postDelayed
                            }

                            val user = searcher.startPhoneDetection(formattedIncoming)
                            if (!user.toPhoneInfo().isDefault() || showEmptyUser) {
                                val overlayCreator = OverlayCreator(context)

                                if (isShowNotificationInsteadOfPopup){
                                    val mIntent = overlayCreator.createIntent(user.toPhoneInfo(), true)
                                    IncomingNotification(context, mIntent, user.toPhoneInfo()).notifyNow()
                                }
                                else{
                                    val mIntentEnabledButtons = overlayCreator.createIntent(user.toPhoneInfo(), false)
                                    context.startActivity(mIntentEnabledButtons)
                                }
                            }
                        }
                    }, 100)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {}
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (incomingNumber != null && isCreatePushUp){
                        val searcher = Search(context)
                        val formattedIncoming = searcher.formatE164NumberRU(incomingNumber)
                        val user = searcher.findUserByPhone(formattedIncoming)
                        val overlayCreator = OverlayCreator(context)
                        val intentOnPushUpClick = overlayCreator.createIntent(user, true)
                        if (user.isSpam)
                            BlockNotification(context, intentOnPushUpClick, user).notify(delayNotificationTime)
                    }
                }
            }
        }

    }

    private fun showNotification(context: Context, phone: PhoneLogInfo){
        val overlayCreator = OverlayCreator(context)
        val intent = overlayCreator.createIntent(phone.toPhoneInfo(), true)
        BlockNotification(context, intent, phone.toPhoneInfo()).createNotification()
    }

}