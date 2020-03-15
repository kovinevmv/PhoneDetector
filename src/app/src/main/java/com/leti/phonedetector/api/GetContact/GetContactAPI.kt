package com.leti.phonedetector.api.GetContact

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.leti.phonedetector.LOG_TAG_VERBOSE
import com.leti.phonedetector.model.DEFAULT_IMAGE
import com.leti.phonedetector.model.PhoneInfo
import org.json.JSONObject
import java.io.File

class GetContactAPI(context: Context, private val timeout : Int) {
    private var updater: ConfigUpdater = ConfigUpdater(context)
    private var token = updater.getPrimaryUse()
    private var requester: Requester = Requester(context, token, timeout)


    fun getAllByPhone(number : String) : PhoneInfo{
        return callFindAllByPhoneAsync(number)
    }

    private fun callFindAllByPhoneAsync(number : String) : PhoneInfo{
        return NetworkTaskGetContact().execute(number).get()
    }

    @SuppressLint("StaticFieldLeak")
    inner class NetworkTaskGetContact : AsyncTask<String, Void, PhoneInfo>() {
        override fun doInBackground(vararg parts: String): PhoneInfo {
            return try {
                val p = this@GetContactAPI.findAllInfoByPhone(parts[0])
                Log.d(LOG_TAG_VERBOSE, p.toString())
                return p

            } catch (e: Exception) {
                Log.d(LOG_TAG_VERBOSE, "Error on API NetworkTaskGetContact: $e")
                PhoneInfo(number = parts[0])
            }
        }
    }

    private fun parse(s : String?) : String?{
        return if (s == "null" || s == null || s == "") return null else s
    }

    private fun findNameByPhone(number : String) : PhoneInfo{
        val response = requester.getPhoneName(number)
        if (response.isNotBlank() && !JSONObject(response).has("error")){
            val json = JSONObject(response)
            val profile = json.getJSONObject("result").getJSONObject("profile")
            val name = profile.getString("name")
            val surname = profile.getString("surname")
            val displayName = profile.getString("displayName")

            val finalName =
            if (name == "null" && surname == "null"){
                displayName
            } else {
                "$name $surname"
            }

            val country = parse(profile.getString("country"))

            var profileImage = parse(profile.getString("profileImage")) ?: DEFAULT_IMAGE
            if (profileImage != DEFAULT_IMAGE){
                profileImage = saveImage(profileImage)
            }

            val email = parse(profile.getString("email"))
            val isSpam = json.getJSONObject("result").getJSONObject("spamInfo").getString("degree") == "high"

            val tags = ArrayList<String>()
            if (country != null) tags.add(country)
            if (email != null) tags.add(email)

            val remainCount = json.getJSONObject("result").getJSONObject("subscriptionInfo").getJSONObject("usage").getJSONObject("search").getInt("remainingCount")
            updater.updateRemainCountByToken(token.token, remainCount)
            token = updater.getPrimaryUse()
            requester.updateToken(token)

            return PhoneInfo(number=number, name=finalName, tags=tags.toTypedArray(), isSpam = isSpam, image = profileImage)
        }

        return PhoneInfo(number=number)
    }

    private fun findTagsByPhone(number : String) : Array<String> {
        val response = requester.getPhoneTags(number)
        return if (response.isNotBlank() && !JSONObject(response).has("error")){
            val tags = JSONObject(response).getJSONObject("result").getJSONArray("tags")
            Array(tags.length()) {tags.getJSONObject(it).getString("tag")}
        } else emptyArray()
    }

    private fun findAllInfoByPhone(number : String) : PhoneInfo{
        val phoneInfo = findNameByPhone(number)
        val tags = findTagsByPhone(number)
        return if (tags.isNotEmpty()){
            PhoneInfo(number=phoneInfo.number,
                name=phoneInfo.name,
                isSpam = phoneInfo.isSpam,
                image = phoneInfo.image,
                tags = phoneInfo.tags + tags.take(5))
        } else phoneInfo
    }

    private fun saveImage(url : String) : String{
        val filename = File.createTempFile("profileImage",".jpg")
        Fuel.download(url)
            .fileDestination { _, _ -> filename }
            .response { _ -> }
        return filename.path
    }

}