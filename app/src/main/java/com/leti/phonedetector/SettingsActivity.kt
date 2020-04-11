package com.leti.phonedetector

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.database.TokenDBHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val REQUEST_CODE_READ_CALL_LOG = 1
        private val REQUEST_CODE_READ_OVERLAY = 2
        private val REQUEST_CODE_CONTACTS = 3


        private lateinit var activatePhoneDetectionSwitch : SwitchPreferenceCompat
        private lateinit var disableSearchInContactsSwitch : SwitchPreferenceCompat
        private lateinit var dropTables : Preference


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            activatePhoneDetectionSwitch = preferenceScreen.findPreference("activate_phone_detection_switch")!!
            disableSearchInContactsSwitch = preferenceScreen.findPreference("disable_search_in_contacts_switch")!!
            dropTables = preferenceScreen.findPreference("drop_table")!!

            dropTables.setOnPreferenceClickListener {
                val builder = AlertDialog.Builder(context!!)
                builder.setTitle("Clean all log and phone information")
                builder.setMessage("This action cannot be undone. Information from logs and all detected numbers will be deleted")

                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    val db = PhoneLogDBHelper(context!!)
                    db.cleanTables()
                    Toast.makeText(context,
                        "Database was cleaned", Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton(android.R.string.no) { _, _ ->}
                builder.show()

                val db = TokenDBHelper(context!!)
                val tokens = db.getTokens()

                var toastText = mutableListOf("Tokens info:")
                for (token in tokens){
                    toastText.add("Token: ${token.token.take(10)}..., Count: ${token.remainCount}")
                }
                Toast.makeText(context!!, toastText.joinToString("\n"), Toast.LENGTH_LONG).show()

                return@setOnPreferenceClickListener true
            }

            activatePhoneDetectionSwitch.setOnPreferenceClickListener {
                if (activatePhoneDetectionSwitch.isChecked){
                    callLogRequestPermissions()
                }
                return@setOnPreferenceClickListener true
            }
            disableSearchInContactsSwitch.setOnPreferenceClickListener {
                if (disableSearchInContactsSwitch.isChecked){
                    callContactPermission()
                }
                return@setOnPreferenceClickListener true
            }

        }

        private fun checkCallLogPermissions() : Array<String>{
            val arrayList = ArrayList<String>()

            when(context?.let { checkSelfPermission(it, android.Manifest.permission.READ_CALL_LOG) }){
                PackageManager.PERMISSION_DENIED -> arrayList.add(android.Manifest.permission.READ_CALL_LOG)
            }
            when(context?.let { checkSelfPermission(it, android.Manifest.permission.READ_PHONE_STATE) }){
                PackageManager.PERMISSION_DENIED -> arrayList.add(android.Manifest.permission.READ_PHONE_STATE)
            }
            return arrayList.toTypedArray()
        }

        private fun checkContactPermissions() : Array<String>{
            val arrayList = ArrayList<String>()

            when(context?.let { checkSelfPermission(it, android.Manifest.permission.READ_CONTACTS) }){
                PackageManager.PERMISSION_DENIED -> arrayList.add(android.Manifest.permission.READ_CONTACTS)
            }
            return arrayList.toTypedArray()
        }

        private fun callContactPermission(){
            val arrayList = checkContactPermissions()
            if (arrayList.isNotEmpty()){
                requestPermissions(arrayList, REQUEST_CODE_CONTACTS)
            }
        }

        private fun callLogRequestPermissions(){
            val arrayList = checkCallLogPermissions()
            if (arrayList.isNotEmpty()) {
                requestPermissions(arrayList, REQUEST_CODE_READ_CALL_LOG)
            }

            if (!Settings.canDrawOverlays(context)){
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context?.packageName}"))
                startActivityForResult(intent, REQUEST_CODE_READ_OVERLAY)
            }
        }


        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_CODE_READ_CALL_LOG) {
                val arrayList = checkCallLogPermissions()
                if (arrayList.isNotEmpty()){
                    Toast.makeText(context, "Not all permissions granted. Can't enable phone detection", Toast.LENGTH_SHORT).show()
                    activatePhoneDetectionSwitch.isChecked = false

                }
            }

            if (requestCode == REQUEST_CODE_CONTACTS){
                val arrayList = checkContactPermissions()
                if (arrayList.isNotEmpty()){
                    Toast.makeText(context, "Not permission granted. Search all phones", Toast.LENGTH_SHORT).show()
                    disableSearchInContactsSwitch.isChecked = false
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_CODE_READ_OVERLAY){
                if (!Settings.canDrawOverlays(context)){
                    Toast.makeText(context, "Overlay not granted", Toast.LENGTH_SHORT).show()
                    activatePhoneDetectionSwitch.isChecked = false
                }
            }
        }
    }
}