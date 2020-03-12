package com.leti.phonedetector.database

import android.provider.BaseColumns

object DBContract {

    /* Inner class that defines the table contents */
    class PhoneInfoEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "info"
            const val COLUMN_INFO_PHONE_NAME = "name"
            const val COLUMN_INFO_PHONE_NUMBER = "number"
            const val COLUMN_INFO_PHONE_IS_SPAM = "isSpam"
            const val COLUMN_INFO_PHONE_IMAGE = "image"
        }
    }

    class PhoneLogEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "phone_log"
            const val COLUMN_LOG_PHONE_NUMBER = "number"
            const val COLUMN_LOG_PHONE_TIME= "time"
            const val COLUMN_LOG_PHONE_DATE = "date"
        }
    }

    class PhoneLogTagsEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "tags"
            const val COLUMN_PHONE_LOG_TAGS_NUMBER = "number"
            const val COLUMN_PHONE_LOG_TAGS_TAG = "tag"
        }
    }

    class TokenEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "tokens"
            const val COLUMN_AES_KEY = "aes_key"
            const val COLUMN_ANDROID_OS = "android_os"
            const val COLUMN_DEVICE_ID = "device_id"
            const val COLUMN_IS_ACTIVE = "is_active"
            const val COLUMN_PRIVATE_KEY = "private_key"
            const val COLUMN_REMAIN_COUNT = "remain_count"
            const val COLUMN_TOKEN = "token"
            const val COLUMN_IS_PRIMARY_USE = "is_primary_use"
        }
    }
}
