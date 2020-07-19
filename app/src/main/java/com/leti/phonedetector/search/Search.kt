package com.leti.phonedetector.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.telephony.PhoneNumberUtils
import androidx.preference.PreferenceManager
import com.leti.phonedetector.api.GetContact.GetContactAPI
import com.leti.phonedetector.api.NeberitrubkuAPI
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneInfo
import com.leti.phonedetector.model.PhoneLogInfo
import java.text.SimpleDateFormat
import java.util.*

class Search(private val context: Context){
    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun findUserByNetwork(number : String, timeout : Int, time : String, date : String) : PhoneLogInfo {
        val use_getcontact = sharedPreferences.getBoolean("use_getcontact", true)
        val use_neberitrubku = sharedPreferences.getBoolean("use_neberitrubku", true)

        val nebUser = if (use_neberitrubku) NeberitrubkuAPI(number, timeout).getUser() else PhoneInfo(number = number)
        if (!nebUser.isDefault())
            return PhoneLogInfo(
                nebUser,
                time = time,
                date = date
            )

        val getUser = if (use_getcontact) GetContactAPI(context, timeout).getAllByPhone(number) else PhoneInfo(number = number)

        val resultUser =
            if (nebUser.isDefault() && !getUser.isDefault()){
                getUser
            }
            else if (!nebUser.isDefault() && getUser.isDefault()){
                nebUser
            }
            else if (!nebUser.isDefault() && nebUser.isSpam){
                nebUser
            }
            else{
                getUser
            }

        return PhoneLogInfo(
            resultUser,
            time = time,
            date = date
        )
    }


    @SuppressLint("SimpleDateFormat")
    fun startPhoneDetection(incomingNumberRaw : String) : PhoneLogInfo {
        val incomingNumber = this.formatE164NumberRU(incomingNumberRaw)
        val timeout = sharedPreferences.getInt("detection_delay_seekbar", 5)
        val isNetworkOnly = sharedPreferences.getBoolean("use_only_network_info",false)
        val noCacheEmpty = sharedPreferences.getBoolean("no_cache_empty_phones", true)

        val db = PhoneLogDBHelper(context)

        val date = SimpleDateFormat("yyyy.MM.dd").format(Date())
        val time = SimpleDateFormat("HH:mm:ss").format(Date())

        val user: PhoneLogInfo = if (isNetworkOnly){
            this.findUserByNetwork(incomingNumber, timeout, time, date)
        }
        else{
            val foundUser : PhoneInfo? = db.findPhoneByNumber(incomingNumber)

            if (foundUser != null && !foundUser.isDefault()) {
                PhoneLogInfo(foundUser, time, date)
            }
            else{
                val foundUserNetwork = this.findUserByNetwork(incomingNumber, timeout, time, date)

                if (!foundUserNetwork.toPhoneInfo().isDefault()){
                    foundUserNetwork
                }
                else {
                    PhoneLogInfo(
                        number = incomingNumber,
                        date = date,
                        time = time
                    )
                }
            }
        }

        if (!noCacheEmpty || !user.toPhoneInfo().isDefault()) {
            db.insertPhone(user)
        }
        return user
    }

    fun formatE164NumberRU(number : String) : String{
        return formatE164Number(number, "RU")
    }

    fun formatE164Number(phNum: String, countryCode: String): String {
        return PhoneNumberUtils.formatNumberToE164(phNum, countryCode) ?: phNum
    }

    fun findUserByPhone(number : String) : PhoneInfo {
        val db = PhoneLogDBHelper(context)
        return db.findPhoneByNumber(number) ?: PhoneInfo(number = number)
    }

}