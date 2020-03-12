package com.leti.phonedetector

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.leti.phonedetector.model.PhoneLogInfo

internal class DataAdapter(val context: Context, private var phones: ArrayList<PhoneLogInfo>) :

    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    val APP_PREFERENCES = "PHONEDETECTOR_PREFERENCES"
    private var sharedPreferences: SharedPreferences
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init{
        sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        this.update(phones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.element_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phone = phones[position]
        when(phone.isSpam){
            true -> holder.imageView.setImageResource(R.drawable.ic_spam)
            false -> holder.imageView.setImageResource(R.drawable.ic_empty_user)
        }
        holder.nameView.text = if (phone.name.length < 25) phone.name else phone.name.take(25) + "..."
        holder.numberView.text = phone.number
        holder.timeView.text = phone.time
        holder.dateView.text = phone.date

        holder.initClick(phone)

    }

    override fun getItemCount(): Int {
        return phones.size
    }

    fun update(data : ArrayList<PhoneLogInfo>) {
        phones = filterShow(data)
        this.notifyDataSetChanged()
    }

    private fun filterShow(data : ArrayList<PhoneLogInfo>) : ArrayList<PhoneLogInfo>{
        val showSpam : Boolean = sharedPreferences.getBoolean("is_show_spam", true)
        val showNotSpam: Boolean = sharedPreferences.getBoolean("is_show_not_spam", true)

        return when {
            showSpam && !showNotSpam -> ArrayList(data.filter { it.isSpam })
            !showSpam && showNotSpam -> ArrayList(data.filter { !it.isSpam })
            showSpam && showNotSpam -> data
            !showSpam && !showNotSpam -> ArrayList()
            else -> data
        }
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.log_element_user_image) as ImageView
        internal val nameView: TextView = view.findViewById(R.id.log_element_text_name) as TextView
        internal val numberView: TextView = view.findViewById(R.id.log_element_text_number) as TextView
        internal val checkBox: CheckBox = view.findViewById(R.id.checkbox_) as CheckBox
        private val logLayout : LinearLayout = view.findViewById(R.id.log_layout) as LinearLayout
        internal val timeView : TextView = view.findViewById(R.id.log_element_text_time) as TextView
        internal val dateView : TextView = view.findViewById(R.id.log_element_text_date) as TextView

        fun initClick(phone : PhoneLogInfo){
            logLayout.setOnClickListener{
                val mIntent = Intent(this@DataAdapter.context, OverlayActivity::class.java)
                mIntent.putExtra("user", phone.toPhoneInfo())
                mIntent.putExtra("is_display_buttons", true)
                this@DataAdapter.context.startActivity(mIntent)
            }
            logLayout.setOnLongClickListener{
                //checkBox.isChecked = !checkBox.isChecked
                return@setOnLongClickListener true
            }
        }
    }
}
