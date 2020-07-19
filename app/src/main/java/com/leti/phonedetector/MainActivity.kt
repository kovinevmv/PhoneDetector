package com.leti.phonedetector

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneLogInfo
import com.leti.phonedetector.overlay.OverlayCreator
import com.leti.phonedetector.search.Search
import com.tsuryo.swipeablerv.SwipeLeftRightCallback
import com.tsuryo.swipeablerv.SwipeableRecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

// Testing phone numbers:
// +79858123876 - Профиль с фото
// +74995505479 - Эльдорадо Спам
// +79112770029 - Марк Маркович
// +79992294383 - Спам

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var adapter : DataAdapter
    private lateinit var phones: ArrayList<PhoneLogInfo>

    private val db = PhoneLogDBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //db.fillSampleData()

        createSharedPref()
        createRecycleView()
        createSwipeRefresh()
        createChannel()
    }

    private fun createRecycleView(){
        /**
         * Create RecycleView with list of phone call activity of user
         */
        val recyclerView = findViewById<View>(R.id.list_of_phones) as SwipeableRecyclerView
        phones = db.readPhoneLog()
        adapter = DataAdapter(this, phones)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.setListener(object : SwipeLeftRightCallback.Listener {
            override fun onSwipedLeft(position: Int) {
                db.deleteByPhoneInfo(phones[position])
                phones = db.readPhoneLog()
                adapter.update(phones)
                adapter.notifyDataSetChanged()
            }

            override fun onSwipedRight(position: Int) {
                val sharedPreferencesGlobal = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val canCall = sharedPreferencesGlobal.getBoolean("make_call_on_swipe",false)
                if (canCall){
                    val intent =
                        Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phones[position].number))
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this@MainActivity,
                            "Permission not granted. Can't call :(", Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@MainActivity,
                        "Disabled. Change settings.", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
        })
    }

    @SuppressLint("CommitPrefEdits")
    fun createSharedPref(){
        /**
         * Init Shared Preferences for save current options in Menu after close app
         * Options: is_show_spam, is_show_not_spam
         */
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        editor = sharedPreferences.edit()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /**
         * Fill items of menu with their values from  Shared Preferences or set default
         * @param menu: Menu of filter: Show spam, Show not spam
         * @return default value true
         */
        menuInflater.inflate(R.menu.menu_main, menu)

        menu.findItem(R.id.item_menu_option_is_show_spam).isChecked = sharedPreferences.getBoolean("is_show_spam", true)
        menu.findItem(R.id.item_menu_option_is_not_show_spam).isChecked = sharedPreferences.getBoolean("is_show_not_spam", true)

        val mSearch = menu.findItem(R.id.action_search)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = "Search"
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView.clearFocus()
                val foundPhones = db.findPhonesByQuery(query)

                if (foundPhones.isEmpty()){
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Nothing found in log.")
                    builder.setMessage("Do you want to make search for phone number: \"${query}\" using API?")

                    builder.setPositiveButton("Yes"){ dialog, _ ->
                        val searcher = Search(applicationContext)
                        val phone: PhoneLogInfo = searcher.startPhoneDetection(query)
                        val overlayCreator = OverlayCreator(applicationContext)
                        val mIntentEnabledButtons = overlayCreator.createIntent(phone.toPhoneInfo(), true)
                        applicationContext.startActivity(mIntentEnabledButtons)

                        phones = db.findPhonesByQuery(query)
                        adapter.update(phones)
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("No"){ dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }

                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != ""){
                    editor.putBoolean("is_use_query", true)
                    editor.putString("last_query", newText)
                    editor.apply()
                    phones = db.findPhonesByQuery(newText)
                    adapter.update(phones)
                } else{
                    editor.putBoolean("is_use_query", false)
                    editor.apply()
                    phones = db.readPhoneLog()
                    adapter.update(phones)
                }

                return true
            }
        })
        mSearchView.setOnCloseListener{
            editor.putBoolean("is_use_query", false)
            editor.apply()

            adapter.update(db.readPhoneLog())
            return@setOnCloseListener true
        }

        return true
    }

    override fun onRestart() {
        super.onRestart()
        phones = db.readPhoneLog()
        adapter.update(phones)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * Detect touch event on item in menu list. Select action for every option
         * @param item element of list on which touch event is detected
         * @return default value true if ok
         */
        return when (item.itemId) {
            R.id.item_menu_option_is_show_spam -> {clickedOnShowSpam(item); true}
            R.id.item_menu_option_is_not_show_spam -> {clickedOnShowNotSpam(item); true}
            R.id.item_menu_option_statistics -> {clickedOnStatistics(); true}
            R.id.item_menu_option_setting -> {clickedOnSetting(); true}
            else -> false
        }
    }

    private fun clickedOnShowSpam(item : MenuItem){
        /**
         * Update state in Shared Pref and CheckBox state of 'Show Spam'
         * @param item element 'Show Spam'
         */
        updateMenuOptionState(item, "is_show_spam", !item.isChecked)
        updateRecycleView()
    }

    private fun clickedOnShowNotSpam(item : MenuItem){
        /**
         * Update state in Shared Pref and CheckBox state of 'Show Not Spam'
         * @param item element 'Show Spam'
         */
        updateMenuOptionState(item, "is_show_not_spam", !item.isChecked)
        updateRecycleView()
    }

    private fun updateRecycleView(){
        if (sharedPreferences.getBoolean("is_use_query", false)){
            phones = db.findPhonesByQuery(sharedPreferences.getString("last_query", "*").toString())
            adapter.update(phones)
        }
        else {
            phones = db.readPhoneLog()
            adapter.update(phones)
        }
    }

    private fun clickedOnStatistics(){
        /**
         * Redirect on Statistics Activity
         */
        val mIntent = Intent(this, StatisticsActivity::class.java)
        startActivity(mIntent)
    }

    private fun clickedOnSetting(){
        /**
         * Redirect on Settings Activity
         */
        val mIntent = Intent(this, SettingsActivity::class.java)
        startActivity(mIntent)
    }

    private fun updateMenuOptionState(item : MenuItem, key : String, state : Boolean){
        /**
         * Save state of element of list
         * @param item element of list. Example: 'Show spam'
         * @param key key in Shared Pref map. Example: 'is_show_not_spam'
         * @param state: boolean, clicked or not
         */
        editor.putBoolean(key, state)
        editor.apply()

        item.isChecked = state
    }

    private fun createSwipeRefresh(){
        swipe_refresh_layout.setOnRefreshListener {
            updateRecycleView()
            swipe_refresh_layout.isRefreshing = false
        }
    }

    private fun createChannel(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Block number",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notify to add phone number to block list"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
    }
}