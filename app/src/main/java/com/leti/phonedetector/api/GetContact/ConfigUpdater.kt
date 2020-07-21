package com.leti.phonedetector.api.GetContact

import android.content.Context
import android.util.Log
import com.leti.phonedetector.LOG_TAG_VERBOSE
import com.leti.phonedetector.database.TokenDBHelper
import com.leti.phonedetector.model.Token

class ConfigUpdater(val context: Context) {
    private val db = TokenDBHelper(context)
    private var tokens : ArrayList<Token>

    init{
        val tokensInput = arrayOf(
            Token(aesKey = "7e754617777435ef17756995ba6e9151942418616f6c065caf32f1e5294ed249",
                androidOS = "android 6.0", deviceId = "3ba530698cff5145", isActive = true, privateKey = 133568, remainCount = 100,
                token = "lnxys0c7df450fd4e54ce512b5d90b6f4956c9e5c34e4661511ad89cf0a", isPrimaryUse = true),
            Token(aesKey = "e92c9987a83bb5ad0353769eade2f897cddeb8691bc9a953876478e6caded42b",
                androidOS = "android 6.0", deviceId = "3ba530698cff5145", isActive = true, privateKey = 3700313, remainCount = 1,
                token = "iiXya3eb642742bd2522ae965f31fdcbdd087293235e0d01a520026c44d", isPrimaryUse = false),
            Token(aesKey = "389383a471af66f4e84b6722d59b7d45e771620857e579565763e1fe3e8ebd0a",
                androidOS = "android 5.0", deviceId = "14130e29cebe9c39", isActive = true, privateKey = 2047896, remainCount = 1,
                token = "hEmffc9d833620e4e13cf96e56d13552c5284f5d99665aa8856a06d4990", isPrimaryUse = false))

        for (token in tokensInput){
            db.insertToken(token)
        }

        tokens = db.getTokens()
    }

    fun updateRemainCountByToken(tokenString : String, remainCount : Int){
        val token : Token? = db.findToken(tokenString)
        if (token != null){
            token.updateRemainCount(remainCount)
            db.updateToken(token)
        }
        updateStatus()
    }

    fun decreaseRemainCountByToken(tokenString: String){
        val token : Token? = db.findToken(tokenString)
        if (token != null){
            token.updateRemainCount(token.remainCount - 1)
            db.updateToken(token)
        }
        updateStatus()
    }

    private fun updateStatus(){
        tokens = db.getTokens()
    }

    fun getAllActive() : ArrayList<Token>{
        return ArrayList(tokens.filter { it.isActive })
    }

    fun getAnyActive() : Token{
        val activeTokens = this.getAllActive()
        return if (activeTokens.size > 0) activeTokens[0] else Token()
    }

    fun getRandomActive() : Token {
        val activeTokens = this.getAllActive().shuffled()
        return if (activeTokens.isNotEmpty()) activeTokens[0] else Token()
    }

    fun getPrimaryUse() : Token{
        val primaryTokens = this.getAllActive().filter { it.isPrimaryUse }
        Log.d(LOG_TAG_VERBOSE, "Count tokens: ${primaryTokens.size}")
        return if (primaryTokens.isNotEmpty()) primaryTokens.first() else Token()
    }
}