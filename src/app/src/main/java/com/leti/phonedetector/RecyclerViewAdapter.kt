package com.leti.phonedetector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

internal class DataAdapter(val context: Context, private val phones: Array<PhoneInfo>) :
    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

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
        holder.nameView.text = phone.name
        holder.numberView.text = phone.number

        holder.initClick(phone)
    }

    override fun getItemCount(): Int {
        return phones.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.log_element_user_image) as ImageView
        internal val nameView: TextView = view.findViewById(R.id.log_element_text_name) as TextView
        internal val numberView: TextView = view.findViewById(R.id.log_element_text_number) as TextView
        internal val checkBox: CheckBox = view.findViewById(R.id.checkbox_) as CheckBox
        private val logLayout : LinearLayout = view.findViewById(R.id.log_layout) as LinearLayout

        fun initClick(phone : PhoneInfo){
            logLayout.setOnClickListener{
                checkBox.isChecked = !checkBox.isChecked
            }
            logLayout.setOnLongClickListener{

                val mIntent = Intent(this@DataAdapter.context, OverlayActivity::class.java)
                mIntent.putExtra("user", phone)
                this@DataAdapter.context.startActivity(mIntent)

                return@setOnLongClickListener true
            }
        }
    }
}