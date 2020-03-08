package com.leti.phonedetector

import android.os.Parcel
import android.os.Parcelable

// Information about phone number
class PhoneInfo(val name: String = "Undefined user",
                 val number: String = "+7800553535",
                 val isSpam: Boolean = false,
                 val tags: Array<String> = emptyArray(),
                 val image: String = "empty") : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArray() as Array<String>,
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(number)
        parcel.writeByte(if (isSpam) 1 else 0)
        parcel.writeStringArray(tags)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhoneInfo

        if (name != other.name) return false
        if (number != other.number) return false
        if (isSpam != other.isSpam) return false
        if (!tags.contentEquals(other.tags)) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + isSpam.hashCode()
        result = 31 * result + tags.contentHashCode()
        result = 31 * result + image.hashCode()
        return result
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


// Log element with info about phone number
class PhoneLogInfo(val name: String = "Undefined user",
                   val number: String = "+7800553535",
                   val isSpam: Boolean = false,
                   val tags: Array<String> = emptyArray(),
                   val time: String = "23:59",
                   val date: String = "01.01.1970",
                   val image: String = "empty") : Parcelable{

    constructor(parcel: Parcel) : this( parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArray() as Array<String>,
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString())

    constructor(phoneInfo: PhoneInfo, time_: String = "23:59", date_: String = "01.01.1970") :
            this(phoneInfo.name, phoneInfo.number, phoneInfo.isSpam, phoneInfo.tags, time_, date_, phoneInfo.image)

    fun toPhoneInfo() : PhoneInfo{
        return PhoneInfo(name, number, isSpam, tags, image)
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhoneLogInfo

        if (name != other.name) return false
        if (number != other.number) return false
        if (isSpam != other.isSpam) return false
        if (!tags.contentEquals(other.tags)) return false
        if (time != other.time) return false
        if (date != other.date) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + isSpam.hashCode()
        result = 31 * result + tags.contentHashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<PhoneLogInfo> {
        override fun createFromParcel(parcel: Parcel): PhoneLogInfo {
            return PhoneLogInfo(parcel)
        }

        override fun newArray(size: Int): Array<PhoneLogInfo?> {
            return arrayOfNulls(size)
        }
    }

}