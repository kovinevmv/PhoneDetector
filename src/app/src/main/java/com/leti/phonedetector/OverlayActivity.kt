package com.leti.phonedetector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.leti.phonedetector.model.PhoneInfo
import kotlinx.android.synthetic.main.activity_overlay.*


class OverlayActivity : Activity() {

    private lateinit var user : PhoneInfo

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        createUserByIntentExtra()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createUserByIntentExtra(){
        user = intent.getParcelableExtra("user") ?: return

        overlay_text_view_number.text = user.number
        overlay_text_view_name.text = user.name
        overlay_tags.text = user.tags.take(5).joinToString(separator = "\n")

        when(user.isSpam){
            true -> setSpamSettings()
            false -> setNotSpamSettings()
        }

        overlay_button_exit.setOnClickListener { finish() }

    }

    @SuppressLint("ServiceCast", "Recycle")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setSpamSettings(){
        overlay_user_image.setImageResource(R.drawable.ic_spam)
        val isDisplayButtons = intent.getBooleanExtra("is_display_buttons", true)
        if (!isDisplayButtons) {
            disableActionButton()
        } else {
            overlay_button_action.text = resources.getString(R.string.button_block_number)

            overlay_button_action.setOnClickListener{
                Toast.makeText(this@OverlayActivity, "Number has been copied to clipboard", Toast.LENGTH_SHORT).show()

                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("BLOCKED_NUMBER_${user.number}", user.number)
                clipboard.setPrimaryClip(clip)

                val telecomManager = this.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                this.startActivity(telecomManager.createManageBlockedNumbersIntent(), null)
            }
        }

    }

    private fun setNotSpamSettings(){
        overlay_user_image.setImageResource(R.drawable.ic_empty_user)
        val isDisplayButtons = intent.getBooleanExtra("is_display_buttons", true)
        if (!isDisplayButtons) {
            disableActionButton()
        } else {

            overlay_button_action.text = resources.getString(R.string.button_add_contact)

            overlay_button_action.setOnClickListener{
                val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
                contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE

                contactIntent
                    .putExtra(ContactsContract.Intents.Insert.NAME, user.name)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, user.number)

                startActivityForResult(contactIntent, 1)
            }
        }
    }

    private fun disableActionButton(){
        overlay_button_action.visibility = View.GONE
        overlay_button_exit.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
    }
}
