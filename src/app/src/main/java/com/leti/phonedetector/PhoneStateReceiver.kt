package com.leti.phonedetector

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.leti.phonedetector.api.NeberitrubkuAPI
import com.leti.phonedetector.contacts.Contacts
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneInfo
import com.leti.phonedetector.model.PhoneLogInfo
import com.leti.phonedetector.notification.BlockNotification
import java.text.SimpleDateFormat
import java.util.*


class PhoneStateReceiver : BroadcastReceiver() {
    
    @SuppressLint( "SimpleDateFormat")
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
                        val formattedIncoming = formatE164NumberRU(incomingNumber)
                        if (notFindInContacts){
                            val contactName = Contacts(context).getContactNameByPhone(formattedIncoming)
                            if (contactName != null) return@postDelayed
                        }

                        val user = startPhoneDetection(context, formattedIncoming)
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
                    val formattedIncoming = formatE164NumberRU(incomingNumber)
                    val user = findUserByPhone(context, formattedIncoming)
                    val intentOnPushUpClick = createIntent(context, user, true)
                    if (user.isSpam)
                        BlockNotification(context, intentOnPushUpClick, user).notify(delayNotificationTime)
                }
            }
        }
    }
    fun formatE164NumberRU(number : String) : String{
        return formatE164Number(number, "RU")
    }

    fun formatE164Number(phNum: String, countryCode: String): String {
        return PhoneNumberUtils.formatNumberToE164(phNum, countryCode) ?: phNum
    }

    private fun findUserByPhone(context : Context, number : String) : PhoneInfo {
        val db = PhoneLogDBHelper(context)
        return db.findPhoneByNumber(number) ?: PhoneInfo(number = number)
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

    @SuppressLint("SimpleDateFormat")
    private fun startPhoneDetection(context: Context, incomingNumber : String) : PhoneLogInfo {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timeout = sharedPreferences.getInt("detection_delay_seekbar", 5)
        val isNetworkOnly = sharedPreferences.getBoolean("use_only_network_info",false)

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
                    PhoneLogInfo(
                        number = incomingNumber,
                        date = date,
                        time = time
                    )
                }
            }
        }

        db.insertPhone(user)
        return user
    }

    private fun findUserByNetwork(number : String, timeout : Int, time : String, date : String) : PhoneLogInfo {
        val newUser = NeberitrubkuAPI(number, timeout).getUser()
        // TODO add here GetContact
        return PhoneLogInfo(
            newUser,
            time = time,
            date = date
        )
    }
}