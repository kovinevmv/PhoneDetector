package com.leti.phonedetector.model

const val AES_KEY = "764fe50cdb21a07c4c049377754c2f50f127febb3aa67e03c7334f414e0fa7db"
const val ANDROID_OS = "android 5.0"
const val DEVICE_ID = "8edbe110a4079828"
const val IS_ACTIVE = true
const val PRIVATE_KEY = 3272978
const val REMAIN_COUNT = 200
const val TOKEN = "hphofd5757307f5dbffce25ae9ef4bd54dc56e770bc763215e7dc4f02e3"
const val IS_PRIMARY_USE = false


class Token(val token: String = TOKEN,
            val aesKey : String = AES_KEY,
            val androidOS : String = ANDROID_OS,
            val deviceId : String = DEVICE_ID,
            var isActive : Boolean = IS_ACTIVE,
            val privateKey : Int = PRIVATE_KEY,
            var remainCount : Int = REMAIN_COUNT,
            var isPrimaryUse : Boolean = IS_PRIMARY_USE) {

    fun isValid() : Boolean{
        return isActive && remainCount > 0
    }

    fun isDefault() : Boolean{
        return token == TOKEN &&
                aesKey == AES_KEY &&
                androidOS == ANDROID_OS &&
                deviceId == DEVICE_ID &&
                privateKey == PRIVATE_KEY &&
                isPrimaryUse == isPrimaryUse
    }


}