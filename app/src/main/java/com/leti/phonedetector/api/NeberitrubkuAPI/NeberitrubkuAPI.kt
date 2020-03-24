package com.leti.phonedetector.api

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.leti.phonedetector.LOG_TAG_ERROR
import com.leti.phonedetector.model.PhoneInfo
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class NeberitrubkuAPI(number_ : String, val timeout : Int){
    var number : String
    val url : String = "https://www.neberitrubku.ru/nomer-telefona/"

    init {
        number = convertPhoneToAPI(number_)
    }

    private fun convertPhoneToAPI(number: String) : String{
        if (number.startsWith("+7")){
            return number.replace("+7", "8")
        } else{
            return number
        }
    }

    private fun convertPhoneDefault(number: String) : String{
        if (number.startsWith("8")){
            return number.replace("^8".toRegex(), "+7")
        } else{
            return number
        }
    }

    fun getUser() : PhoneInfo {
        return NetworkTask().execute(url + number).get()
    }


    fun findInfo() : PhoneInfo {
        val doc = Jsoup.connect("https://www.neberitrubku.ru/nomer-telefona/$number").timeout(timeout * 1000 + 1).get()
        val categories = doc.select("div.categories")
        val ratings = doc.select("div.ratings")
        val comments = doc.select("span.review_comment")

        val name : String? = parseCategories(categories)
        val rating : String? = parseRating(ratings)
        val tags : Array<String> = parseComments(comments)

        val isSpam = rating?.contains("отриц") ?: false

        val user = if (name == null || rating == null) PhoneInfo(
            number = convertPhoneDefault(number)
        )
        else PhoneInfo(
            number = convertPhoneDefault(
                number
            ), name = "$name", tags = tags, isSpam = isSpam
        )

        return user
    }

    private fun parseCategories(categories: Elements) : String? {
        val resultCategories = ArrayList<String>()
        for (cat in categories){
            val catRaw = cat.select("li.active")
            resultCategories.add(catRaw.text().replace("\\d+x ".toRegex(), ""))
        }
        return if (resultCategories.size > 0) resultCategories[0] else null
    }

    private fun parseRating(ratings: Elements) : String? {
        val resultRating = ArrayList<String>()
        for (rat in ratings){
            val ratRaw = rat.select("li.active")
            resultRating.add(ratRaw.text().replace("\\d+x ".toRegex(), ""))
        }
        return if (resultRating.size > 0) resultRating[0] else null
    }

    private fun parseComments(comments : Elements) : Array<String>{
        val resultComments= ArrayList<String>()
        for (comment in comments){
            var commentText = comment.text()
            if (commentText.length >= 3 && !commentText.contains("Этот комментарий был")){
                commentText = if (commentText.length < 40) commentText else commentText.substring(0, 37) + "..."
                resultComments.add(commentText)
            }
        }
        return resultComments.toSet().toList().sortedWith(compareBy { it.length }).take(5).toTypedArray()
    }

    @SuppressLint("StaticFieldLeak")
    inner class NetworkTask : AsyncTask<String, Void, PhoneInfo>() {

        override fun doInBackground(vararg parts: String): PhoneInfo {
            return try {
                this@NeberitrubkuAPI.findInfo()
            } catch (e: Exception) {
                Log.d(LOG_TAG_ERROR, "Error on API: $e")
                PhoneInfo(
                    number = convertPhoneDefault(
                        number
                    )
                )
            }
        }
    }

}