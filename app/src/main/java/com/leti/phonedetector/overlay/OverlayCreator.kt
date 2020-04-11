package com.leti.phonedetector.overlay

import android.content.Context
import android.content.Intent
import com.leti.phonedetector.OverlayActivity
import com.leti.phonedetector.model.PhoneInfo

class OverlayCreator(private val context: Context) {

    fun createIntent( user: PhoneInfo, isDisplayButtons : Boolean) : Intent {
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
}