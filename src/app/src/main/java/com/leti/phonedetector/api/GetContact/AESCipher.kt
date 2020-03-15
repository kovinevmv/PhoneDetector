package com.leti.phonedetector.api.GetContact
import android.util.Base64
import com.leti.phonedetector.model.Token
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.unHex() : ByteArray {
    return  this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

class AESCipher(var token : Token = Token()) {
    private lateinit var cipherDecrypt : Cipher
    private lateinit var cipherEncrypt : Cipher

    private val blockSize = 16

    init{
        setCiphers()
    }

    private fun setCiphers(){
        val AES_KEY = SecretKeySpec(token.aesKey.unHex(), "AES")

        cipherDecrypt = Cipher.getInstance("AES/ECB/NOPADDING")
        cipherDecrypt.init(Cipher.DECRYPT_MODE, AES_KEY)

        cipherEncrypt = Cipher.getInstance("AES/ECB/NOPADDING")
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, AES_KEY)
    }

    fun updateToken(token_ : Token){
        token = token_
        setCiphers()
    }

    fun createSignature(payload : String, timestamp: String) : String{
        val message = formatMessageToHMAC(payload, timestamp).toByteArray()
        val secret = HMAC_KEY.toByteArray()

        val sign = Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(secret, algorithm))
            doFinal(message)
        }
        return encodeBase64(sign)
    }

    private fun formatMessageToHMAC(msg : String, timestamp : String) : String{
        return "${timestamp}-${msg}"
    }

    private fun unpadding(s : String) : String {
        return s.dropLast(s.last().toInt())
    }

    private fun padding(s : String) : ByteArray{
        val padding = blockSize - s.length % blockSize
        return s.toByteArray() + ByteArray(padding) { padding.toByte()}
    }

    fun decodeBase64(data : String) : ByteArray{
        return Base64.decode(data, Base64.DEFAULT)
    }

    fun encodeBase64(data : ByteArray) : String{
        return Base64.encode(data, Base64.DEFAULT).toString(Charset.defaultCharset())
    }

    fun decryptAES(data : String) : ByteArray{
        return cipherDecrypt.doFinal(data.toByteArray())
    }

    fun decryptAES(data : ByteArray) : ByteArray{
        return cipherDecrypt.doFinal(data)
    }

    fun decryptAESWithBase64(data : String) : String{
        return unpadding(decryptAES(decodeBase64(data)).toString(Charset.defaultCharset()))
    }

    fun encryptAES(data : String) : ByteArray{
        return cipherEncrypt.doFinal(padding(data))
    }

    fun encryptAESWithBase64(data : String) : String{
        return encodeBase64(encryptAES(data))
    }
}


