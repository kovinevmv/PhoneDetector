package com.leti.phonedetector

import android.content.*
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.leti.phonedetector.model.DEFAULT_IMAGE
import com.leti.phonedetector.model.PhoneLogInfo

internal class DataAdapter(val context: Context, private var phones: ArrayList<PhoneLogInfo>) :

    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    val APP_PREFERENCES = "PHONEDETECTOR_PREFERENCES"
    private var sharedPreferences: SharedPreferences
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var lastPosition = -1

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
        holder.timeView.text = phone.time.take(2 + 1 + 2)
        holder.dateView.text = phone.date
        if (phone.image != DEFAULT_IMAGE) holder.imageView.setImageBitmap(BitmapFactory.decodeFile(phone.image))

        holder.initClick(phone)
        setAnimation(holder.itemView, position);
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation: Animation =
                AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
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
                Toast.makeText(this@DataAdapter.context, "Number has been copied to clipboard", Toast.LENGTH_SHORT).show()

                val number = numberView.text
                val clipboard = this@DataAdapter.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ADD_PHONE_NUMBER_$number", number)
                clipboard.setPrimaryClip(clip)
                return@setOnLongClickListener true
            }
        }
    }
}
