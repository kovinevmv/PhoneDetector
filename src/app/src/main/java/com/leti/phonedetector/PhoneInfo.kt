package com.leti.phonedetector

import android.os.Parcel
import android.os.Parcelable


val DEFAULT_NAME = "Undefined user"
val DEFAULT_NUMBER = "+7800553535"
val DEFAULT_IS_SPAM_STATE = false
val DEFAULT_TAGS : Array<String> = emptyArray()
val DEFAULT_IMAGE = "empty"
val DEFAULT_TIME = "23:59:59"
val DEFAULT_DATE = "1970.01.01"

// Information about phone number
class PhoneInfo(val name: String = DEFAULT_NAME,
                 val number: String = DEFAULT_NUMBER,
                 val isSpam: Boolean = DEFAULT_IS_SPAM_STATE,
                 val tags: Array<String> = DEFAULT_TAGS,
                 val image: String = DEFAULT_IMAGE) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArray() as Array<String>,
        parcel.readString().toString()
    )

    fun isDefault() : Boolean{
        return name == DEFAULT_NAME &&
               isSpam == DEFAULT_IS_SPAM_STATE &&
               tags.contentEquals(DEFAULT_TAGS) &&
               image == DEFAULT_IMAGE

    }

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
class PhoneLogInfo(val name: String = DEFAULT_NAME,
                   val number: String = DEFAULT_NUMBER,
                   val isSpam: Boolean = DEFAULT_IS_SPAM_STATE,
                   val tags: Array<String> = DEFAULT_TAGS,
                   val time: String = DEFAULT_TIME,
                   val date: String = DEFAULT_DATE,
                   val image: String = DEFAULT_IMAGE) : Parcelable{

    constructor(parcel: Parcel) : this( parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArray() as Array<String>,
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString())

    constructor(phoneInfo: PhoneInfo, time: String = DEFAULT_TIME, date: String = DEFAULT_DATE) :
            this(phoneInfo.name, phoneInfo.number, phoneInfo.isSpam, phoneInfo.tags, time, date, phoneInfo.image)

    fun toPhoneInfo() : PhoneInfo{
        return PhoneInfo(name, number, isSpam, tags, image)
    }

    fun isDefault() : Boolean{
        return this.toPhoneInfo().isDefault() && time == DEFAULT_TIME && date == DEFAULT_DATE
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