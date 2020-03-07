package com.leti.phonedetector

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_overlay.*

class OverlayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay)

        createUserByIntentExtra()
    }

    private fun createUserByIntentExtra(){
        val extras = intent.extras

        var number: String? = ""
        var name: String? = resources.getString(R.string.info_user_not_found)
        var isSpam = false

        if (extras != null){
            if (extras.containsKey("number")) {
                number = intent.getStringExtra("number")
            }
            if (extras.containsKey("name")) {
                name = intent.getStringExtra("name")
            }
            if (extras.containsKey("is_spam")){
                isSpam = intent.getBooleanExtra("is_spam", false)
            }
        }

        overlay_text_view_number.text = number
        overlay_text_view_name.text = name

        when(isSpam){
            true -> setSpamSettings()
            false -> setNotSpamSettings()
        }

        overlay_button_exit.setOnClickListener { finish() }
    }

    private fun setSpamSettings(){
        overlay_user_image.setImageResource(R.mipmap.ic_spam_round)
        overlay_button_action.text = resources.getString(R.string.button_block_number)

        // TODO Add button listener
    }

    private fun setNotSpamSettings(){
        overlay_user_image.setImageResource(R.mipmap.ic_empty_user)
        overlay_button_action.text = resources.getString(R.string.button_add_contact)

        // TODO Add button listener
    }
}
