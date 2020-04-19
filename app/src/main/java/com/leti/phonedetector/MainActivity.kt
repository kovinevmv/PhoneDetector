package com.leti.phonedetector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneLogInfo
import com.leti.phonedetector.overlay.OverlayCreator
import com.leti.phonedetector.search.Search
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

// Testing phone numbers:
// +79858123876 - Профиль с фото
// +74995505479 - Эльдорадо Спам
// +79112770029 - Марк Маркович

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var adapter : DataAdapter

    private val db = PhoneLogDBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        db.fillSampleData()

        createSharedPref()
        createRecycleView()
        createSwipeRefresh()
        createChannel()
    }

    private fun createRecycleView(){
        /**
         * Create RecycleView with list of phone call activity of user
         */
        val recyclerView = findViewById<View>(R.id.list_of_phones) as RecyclerView
        adapter = DataAdapter(this, db.readPhoneLog())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
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
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView.clearFocus()
                val foundPhones = db.findPhonesByQuery(query)

                if (foundPhones.isEmpty()){
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Nothing found.")
                    builder.setMessage("Are you want to make search for phone number: \"${query}\"?")

                    builder.setPositiveButton("Yes"){ dialog, _ ->
                        val searcher = Search(applicationContext)
                        val phone: PhoneLogInfo = searcher.startPhoneDetection(query)
                        val overlayCreator = OverlayCreator(applicationContext)
                        val mIntentEnabledButtons = overlayCreator.createIntent(phone.toPhoneInfo(), true)
                        applicationContext.startActivity(mIntentEnabledButtons)

                        adapter.update(db.findPhonesByQuery(query))
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
                    adapter.update(db.findPhonesByQuery(newText))
                } else{
                    editor.putBoolean("is_use_query", false)
                    editor.apply()
                    adapter.update(db.readPhoneLog())
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
        adapter.update(db.readPhoneLog())

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
            adapter.update(db.findPhonesByQuery(sharedPreferences.getString("last_query", "*").toString()))
        }
        else {
            adapter.update(db.readPhoneLog())
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