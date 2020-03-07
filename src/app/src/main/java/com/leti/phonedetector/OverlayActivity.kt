package com.leti.phonedetector

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_overlay.*

class OverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        createUserByIntentExtra()
    }

    private fun createUserByIntentExtra(){
        val user = intent.getParcelableExtra<PhoneInfo>("user") ?: return

        overlay_text_view_number.text = user.number
        overlay_text_view_name.text = user.name
        overlay_tags.text = user.tags.take(5).joinToString(separator = "\n")

        when(user.isSpam){
            true -> setSpamSettings()
            false -> setNotSpamSettings()
        }

        overlay_button_exit.setOnClickListener { finish() }
    }

    private fun setSpamSettings(){
        overlay_user_image.setImageResource(R.drawable.ic_spam)
        overlay_button_action.text = resources.getString(R.string.button_block_number)

        // TODO Add button listener
    }

    private fun setNotSpamSettings(){
        overlay_user_image.setImageResource(R.drawable.ic_empty_user)
        overlay_button_action.text = resources.getString(R.string.button_add_contact)

        // TODO Add button listener
    }
}
