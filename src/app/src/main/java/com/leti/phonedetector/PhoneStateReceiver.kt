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
        val notFindInContacts = sharedPreferences.getBoolean("disable_search_in_contacts_switch",false)
        val isRun = sharedPreferences.getBoolean("activate_phone_detection_switch",false)

        if (!isRun) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (incomingNumber != null) {

            var contactName = ""
            if (notFindInContacts){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    contactName = getContactName(incomingNumber, context).toString()
                }
                if (notFindInContacts && contactName != "") return
            }

            val mIntent = Intent(context, OverlayActivity::class.java)
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val date = SimpleDateFormat("yyyy.MM.dd").format(Date())
            val time = SimpleDateFormat("HH:mm:ss").format(Date())

            val db = PhoneLogDBHelper(context)
            val foundUser : PhoneInfo? = db.findPhoneByNumber(incomingNumber)

            val user: PhoneLogInfo

            user = if (foundUser != null && !foundUser.isDefault())
                PhoneLogInfo(foundUser, time, date)
            else{
                val n_user = NeberitrubkuAPI(incomingNumber).getUser()

                if (!n_user.isDefault()){
                    PhoneLogInfo(n_user, time, date)
                }
                else {
                    PhoneLogInfo(number = incomingNumber, date=date, time=time)
                }
            }
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Handler().postDelayed({
                        mIntent.putExtra("user", user.toPhoneInfo())
                        mIntent.putExtra("is_display_buttons", false)
                        db.insertPhone(user)
                        context.startActivity(mIntent)
                    }, 200)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {}
                TelephonyManager.EXTRA_STATE_IDLE -> {}
            }
        }
    }

    private fun getContactName(phoneNumber: String?, context: Context): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName = ""
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