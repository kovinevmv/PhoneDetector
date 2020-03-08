package com.leti.phonedetector.database

import android.provider.BaseColumns

object DBContract {

    /* Inner class that defines the table contents */
    class PhoneInfoEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "info"
            val COLUMN_INFO_PHONE_NAME = "name"
            val COLUMN_INFO_PHONE_NUMBER = "number"
            val COLUMN_INFO_PHONE_IS_SPAM = "isSpam"
            val COLUMN_INFO_PHONE_TAGS = "tags"
            val COLUMN_INFO_PHONE_IMAGE = "image"
        }
    }

    class PhoneLogEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "log"
            val COLUMN_LOG_PHONE_NUMBER = "number"
            val COLUMN_LOG_PHONE_TIME= "time"
            val COLUMN_LOG_PHONE_DATE = "date"
        }
    }

    class PhoneLogTagsEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "tags"
            val COLUMN_PHONE_LOG_TAGS_NUMBER = "number"
            val COLUMN_PHONE_LOG_TAGS_TAG = "tag"
        }
    }

    class TokenEntry : BaseColumns {
        companion object {
            val TABLE_NAME = "tokens"
            val COLUMN_AES_KEY = "aes_key"
            val COLUMN_ANDROID_OS = "android_os"
            val COLUMN_DEVICE_ID = "device_id"
            val COLUMN_IS_ACTIVE = "is_active"
            val COLUMN_PRIVATE_KEY = "private_key"
            val COLUMN_REMAIN_COUNT = "remain_count"
            val COLUMN_TOKEN = "token"
            val COLUMN_IS_PRIMARY_USE = "is_primary_use"
        }
    }
}
