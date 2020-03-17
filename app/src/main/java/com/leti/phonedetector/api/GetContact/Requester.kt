package com.leti.phonedetector.api.GetContact

import android.content.Context
import android.util.Log
import com.leti.phonedetector.model.Token
import org.json.JSONObject
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Response
import com.leti.phonedetector.LOG_TAG_VERBOSE
import java.nio.charset.Charset

class Requester(val context: Context, var token : Token, val timeout : Int) {

    private var cipherAES: AESCipher = AESCipher(token)
    private var timestamp: String = updateTimestamp()
    private val baseUrl = BASE_URL
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var requestData: MutableMap<String, String> = mutableMapOf()
    private val methods: Map<String, String> = mapOf(
                                                "number-detail" to "details",
                                                "search" to "search",
                                                "verify-code" to "",
                                                "register" to "")

    init {
        updateHeaders()
    }

    private fun updateHeaders() {
        headers = mutableMapOf(
            "X-App-Version" to APP_VERSION,
            "X-Token" to token.token,
            "X-Os" to token.androidOS,
            "X-Client-Device-Id" to token.deviceId,
            "Content-Type" to "application/json; charset=utf-8",
            "Connection" to "close",
            "Accept-Encoding" to "gzip, deflate",
            "X-Req-Timestamp" to timestamp,
            "X-Req-Signature" to "",
            "X-Encrypted" to "1"
        )

        requestData = mutableMapOf(
            "countryCode" to COUNTRY,
            "source" to "",
            "token" to token.token
        )
    }

    fun updateToken(token_ : Token){
        token = token_
        cipherAES.updateToken(token_)
        timestamp = updateTimestamp()
        updateHeaders()
    }

    private fun updateTimestamp(): String {
        return System.currentTimeMillis().toString()
    }

    private fun preparePayload(data: MutableMap<String, String>): String {
        return JSONObject(data as Map<*, *>).toString().replace("[ ~]".toRegex(), "")
    }

    private fun sendPost(url : String, data : String) : Pair<Boolean, String> {
        Log.d(LOG_TAG_VERBOSE, "Header $headers, data: $data")
        val (_, response, _) = Fuel.post(url).header(headers).body(data).timeout(timeout * 1000).response()
        Log.d(LOG_TAG_VERBOSE, "${response.statusCode}, ${response.data.toString(Charset.defaultCharset())}")
        timestamp = updateTimestamp()
        return parseResponse(response)
    }

    private fun sendRequestEncrypted(url : String, data : String) : Pair<Boolean, String>{
        headers["X-Encrypted"] = "1"
        return sendPost(url, JSONObject(mapOf("data" to cipherAES.encryptAESWithBase64(data))).toString())
    }

    private fun sendRequestNoEncrypted(url : String, data : String) : Pair<Boolean, String>{
        headers["X-Encrypted"] = "0"
        return sendPost(url, data)
    }

    private fun parseResponse(response : Response) : Pair<Boolean, String>{
        Log.d(LOG_TAG_VERBOSE, "${response.statusCode} code")
        return when (response.statusCode){
            200 -> Pair(true, JSONObject(response.data.toString(Charset.defaultCharset())).getString("data"))
            201 -> Pair(true, response.data.toString(Charset.defaultCharset()))
            404 -> Pair(false, response.data.toString())
            else -> {
                val responseText = JSONObject(response.data.toString(Charset.defaultCharset())).getString("data")
                val responseDecrypted = cipherAES.decryptAESWithBase64(responseText.toString())
                val errorCode = JSONObject(responseDecrypted).getJSONObject("meta").getString("errorCode")
                Log.d(LOG_TAG_VERBOSE, "Error in parseResponse: $errorCode, $responseDecrypted")

                // TODO captcha bypass
                when(errorCode){
                    "403004" -> {Pair(false, response.data.toString(Charset.defaultCharset())) }
                    else -> {
                          Pair(false, response.data.toString(Charset.defaultCharset()))
                    }
                }
                Pair(false, response.data.toString(Charset.defaultCharset()))
            }
        }
    }

    private fun sendReqToTheServer(url : String, payload : MutableMap<String, String>, noEncryption : Boolean=false) : String{
        val payloadPrepared = preparePayload(payload)
        Log.d(LOG_TAG_VERBOSE,"Payload: $payloadPrepared, $timestamp")
        headers["X-Req-Signature"] = cipherAES.createSignature(payloadPrepared, timestamp)

        val (isOk, response) = if (noEncryption){ sendRequestNoEncrypted(url, payloadPrepared) }
        else { sendRequestEncrypted(url, payloadPrepared) }

        return if (isOk) cipherAES.decryptAESWithBase64(response) else JSONObject(mapOf("error" to response)).toString()

    }

    fun getPhoneName(number : String) : String{
        val method = "search"
        requestData["source"] = methods[method].toString()
        requestData["phoneNumber"] = number
        return sendReqToTheServer("$baseUrl/$API_VERSION/$method", requestData)
    }

    fun getPhoneTags(number : String) : String{
        val method = "number-detail"
        requestData["source"] = methods[method].toString()
        requestData["phoneNumber"] = number
        return sendReqToTheServer("$baseUrl/$API_VERSION/$method", requestData)
    }
}
