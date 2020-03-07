package com.leti.phonedetector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class DataAdapter(context: Context, private val phones: Array<PhoneInfo>) :
    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
        val view = inflater.inflate(R.layout.element_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
        val phone = phones[position]
        holder.imageView.setImageResource(R.mipmap.ic_spam)
        holder.nameView.text = phone.name
        holder.numberView.text = phone.number
        holder.checkBox.isChecked = phone.isSpam
    }

    override fun getItemCount(): Int {
        return phones.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.log_element_user_image) as ImageView
        internal val nameView: TextView = view.findViewById(R.id.log_element_text_name) as TextView
        internal val numberView: TextView = view.findViewById(R.id.log_element_text_number) as TextView
        internal val checkBox: CheckBox = view.findViewById(R.id.checkbox_) as CheckBox
    }
}