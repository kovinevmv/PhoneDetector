package com.leti.phonedetector.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class Contacts(val context: Context) {
    fun getContactNameByPhone(phoneNumber: String?): String? {
        if (!isPermissionGranted(context)) return null

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        var contactName : String? = null
        val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }
        return contactName
    }

    private fun isPermissionGranted(context: Context) : Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
}