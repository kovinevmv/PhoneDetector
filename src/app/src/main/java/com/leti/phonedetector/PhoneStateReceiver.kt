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
import android.telephony.TelephonyManager
import android.util.Log
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
                            val mIntent = createIntent(context, user)
                            context.startActivity(mIntent)
                        }

                    }
                }, 100)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {}
            TelephonyManager.EXTRA_STATE_IDLE -> {}
        }
    }

    private fun createIntent(context: Context, user: PhoneLogInfo) : Intent{
        val mIntent = Intent(context, OverlayActivity::class.java)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        mIntent.putExtra("user", user.toPhoneInfo())
        mIntent.putExtra("is_display_buttons", false)
        return mIntent
    }

    private fun startPhoneDetection(context: Context, incomingNumber : String) : PhoneLogInfo{

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timeout = sharedPreferences.getInt("detection_delay_seekbar",5)
        val isNetworkOnly = sharedPreferences.getBoolean("use_only_network_info",true)

        val db = PhoneLogDBHelper(context)

        val date = SimpleDateFormat("yyyy.MM.dd").format(Date())
        val time = SimpleDateFormat("HH:mm:ss").format(Date())

        val user: PhoneLogInfo = if (isNetworkOnly){
            Log.d("PHONEDETECTOR", "WAS HERE")
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