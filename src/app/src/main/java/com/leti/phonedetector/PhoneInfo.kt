package com.leti.phonedetector

import android.os.Parcel
import android.os.Parcelable


// TODO add image, tags, date, time and other
class PhoneInfo(val name: String = "Undefined user",
                val number: String = "+7800553535",
                val isSpam: Boolean = false,
                val tags: Array<String> = emptyArray(),
                val time: String = "23:59",
                val date: String = "01/01/1970",
                val image: String = "empty") : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArray() as Array<String>,
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(number)
        parcel.writeByte(if (isSpam) 1 else 0)
        parcel.writeStringArray(tags)
        parcel.writeString(time)
        parcel.writeString(date)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhoneInfo> {
        override fun createFromParcel(parcel: Parcel): PhoneInfo {
            return PhoneInfo(parcel)
        }

        override fun newArray(size: Int): Array<PhoneInfo?> {
            return arrayOfNulls(size)
        }
    }
}