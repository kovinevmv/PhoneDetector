package com.leti.phonedetector.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.leti.phonedetector.*
import com.leti.phonedetector.model.PhoneInfo
import com.leti.phonedetector.model.PhoneLogInfo
import kotlin.collections.ArrayList

class PhoneLogDBHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(LOG_TAG_VERBOSE, "Call onCreate class PhoneLogDBHelper")

        db.execSQL(create_info_table)
        db.execSQL(create_log_table)
        db.execSQL(create_tags_table)
    }

    fun fillSampleData(){
        val phones = arrayOf(
            PhoneLogInfo(
                "Max",
                "+79992295999",
                false
            ),
            PhoneLogInfo(
                "Сбербанк",
                "+79992295998",
                true,
                tags = arrayOf("Sberbank", "Постоянные звонки", "Мошенники")
            ),
            PhoneLogInfo(
                "Pizza",
                "+79992295997",
                false
            ),
            PhoneLogInfo(
                "Citron",
                "+79992295996",
                false
            )
        )

        for (phone in phones)
            this.insertPhone(phone)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(LOG_TAG_VERBOSE, "Call onUpgrade onCreate class PhoneLogDBHelper")
        cleanTables(db)

    }

    private fun cleanTables(db: SQLiteDatabase){
        db.execSQL(drop_info)
        db.execSQL(drop_log)
        db.execSQL(drop_tags)

        onCreate(db)
    }

    fun cleanTables(){
        val db = writableDatabase
        cleanTables(db)
        db.close()
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertPhone(phone: PhoneLogInfo): Boolean {
        Log.d(LOG_TAG_VERBOSE, "Call insertPhone with param PhoneLogInfo: '${phone.number}'")

        val foundUser = this.findPhoneByNumber(phone.number)
        Log.d(LOG_TAG_VERBOSE, "Found user: ${foundUser?.number}")

        if (foundUser != null && foundUser.isDefault()){
            this.deletePhoneInfo(phone.number)
        }

        val db = writableDatabase

        val valuesInfo = ContentValues()
        valuesInfo.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME, phone.name)
        valuesInfo.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER, phone.number)
        valuesInfo.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IS_SPAM, phone.isSpam)
        valuesInfo.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IMAGE, phone.image)
        db.insert(DBContract.PhoneInfoEntry.TABLE_NAME, null, valuesInfo)

        val valuesLog = ContentValues()
        valuesLog.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER, phone.number)
        valuesLog.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME, phone.time)
        valuesLog.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE, phone.date)
        db.insert(DBContract.PhoneLogEntry.TABLE_NAME, null, valuesLog)

        for (tag in phone.tags){
            val valuesTags = ContentValues()
            valuesTags.put(DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER, phone.number)
            valuesTags.put(DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG, tag)
            db.insert(DBContract.PhoneLogTagsEntry.TABLE_NAME, null, valuesTags)
        }

        db.close()
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun deletePhoneInfo(number: String): Boolean {
        Log.d(LOG_TAG_VERBOSE, "Call deletePhoneInfo with param number: '$number'")

        val db = writableDatabase

        // TODO SQL Injection
        db.execSQL("DELETE FROM ${DBContract.PhoneInfoEntry.TABLE_NAME} WHERE ${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER} = '$number'")

        // TODO fix delete tags
        db.execSQL("DELETE FROM ${DBContract.PhoneLogTagsEntry.TABLE_NAME} WHERE ${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER} = '$number'")
        db.close()
        return true
    }

    @Throws(SQLiteConstraintException::class)
    fun deletePhoneFull(number : String) : Boolean{
        Log.d(LOG_TAG_VERBOSE, "Call deletePhoneFull with param number: '$number'")

        this.deletePhoneInfo(number)

        val db = writableDatabase
        // TODO SQL Injection
        db.execSQL("DELETE FROM ${DBContract.PhoneLogEntry.TABLE_NAME} WHERE ${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER} = '$number'")
        db.close()
        return true
    }

    @SuppressLint("Recycle")
    @Throws(SQLiteConstraintException::class)
    private fun findTagsByPhoneNumber(number : String) : ArrayList<String>{
        Log.d(LOG_TAG_VERBOSE, "Call findTagsByPhone with param phone: '$number'")

        val db = readableDatabase
        val tags = ArrayList<String>()

        try {
            val cursorTags = db.rawQuery("SELECT ${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG}  " +
                    "FROM ${DBContract.PhoneLogTagsEntry.TABLE_NAME} " +
                    "WHERE ${DBContract.PhoneLogTagsEntry.TABLE_NAME}.${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER}" +
                    " = \"$number\";", null)

            if (cursorTags!!.moveToFirst()) {
                while (!cursorTags.isAfterLast) {
                    val tag = cursorTags.getString(cursorTags.getColumnIndex(DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG))
                    tags.add(tag)
                    cursorTags.moveToNext()
                }
            }
        }

        catch (e : SQLiteConstraintException){
            Log.e(LOG_TAG_ERROR, "Error in findTagsByPhone: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return tags
        }

        db.close()
        return tags
    }

    @SuppressLint("Recycle")
    @Throws(SQLiteConstraintException::class)
    fun findPhoneByNumber(number: String): PhoneInfo? {
        // TODO SQL Injection
        Log.d(LOG_TAG_VERBOSE, "Call findPhoneByNumber with param number: '$number'")

        val users = ArrayList<PhoneInfo>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM ${DBContract.PhoneInfoEntry.TABLE_NAME} WHERE " +
                    "${DBContract.PhoneInfoEntry.TABLE_NAME}.${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER} " +
                    "= \"${number}\";", null)

            if (cursor!!.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER))
                    val name = cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME))
                    val isSpam = cursor.getInt(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IS_SPAM))
                    val image = cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IMAGE))

                    val tags = this.findTagsByPhoneNumber(number)
                    users.add(
                        PhoneInfo(
                            name,
                            number,
                            isSpam != 0,
                            tags.toTypedArray(),
                            image
                        )
                    )
                    cursor.moveToNext()
                }
            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG_ERROR, "Error in findPhoneByNumber: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return null
        }

        db.close()
        return if (users.size > 0) users[0] else null
    }

    @SuppressLint("Recycle")
    @Throws(SQLiteConstraintException::class)
    fun readPhoneLog(): ArrayList<PhoneLogInfo> {
        Log.d(LOG_TAG_VERBOSE, "Call readPhoneLog")

        val phones = ArrayList<PhoneLogInfo>()
        val db = writableDatabase

        try {
            // TODO fix sort
            val cursor = db.rawQuery("SELECT * FROM ${DBContract.PhoneLogEntry.TABLE_NAME} " +
                    "ORDER BY ${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE} DESC", null)

            if (cursor!!.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val number = cursor.getString(cursor.getColumnIndex(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER))
                    val time = cursor.getString(cursor.getColumnIndex(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME))
                    val date = cursor.getString(cursor.getColumnIndex(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE))

                    val phoneInfo : PhoneInfo? = this.findPhoneByNumber(number)
                    if (phoneInfo != null)
                        phones.add(
                            PhoneLogInfo(
                                phoneInfo,
                                time,
                                date
                            )
                        )


                    cursor.moveToNext()
                }
            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG_ERROR, "Error in readPhoneLog: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return phones
        }

        db.close()
        return phones
    }

    @SuppressLint("Recycle")
    @Throws(SQLiteConstraintException::class)
    fun findPhonesByQuery(query : String) : ArrayList<PhoneLogInfo>{
        // TODO SQL Injection
        Log.d(LOG_TAG_VERBOSE, "Call findPhonesByQuery with param query: '$query'")

        val phones = ArrayList<PhoneLogInfo>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM ${DBContract.PhoneInfoEntry.TABLE_NAME} INNER " +
                    "JOIN ${DBContract.PhoneLogEntry.TABLE_NAME} ON " +
                    "${DBContract.PhoneInfoEntry.TABLE_NAME}.${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER} == " +
                    "${DBContract.PhoneLogEntry.TABLE_NAME}.${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER} WHERE " +
                    "${DBContract.PhoneInfoEntry.TABLE_NAME}.${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER} LIKE \"%${query}%\" " +
                    "OR ${DBContract.PhoneInfoEntry.TABLE_NAME}.${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME} LIKE \"%${query}%\"", null)

            if (cursor!!.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val number = cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER))
                    val name = cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME))
                    val isSpam = cursor.getInt(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IS_SPAM))
                    val image = cursor.getString(cursor.getColumnIndex(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IMAGE))
                    val time = cursor.getString(cursor.getColumnIndex(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME))
                    val date = cursor.getString(cursor.getColumnIndex(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE))

                    val tags = this.findTagsByPhoneNumber(number)
                    phones.add(
                        PhoneLogInfo(
                            PhoneInfo(
                                name,
                                number,
                                isSpam != 0,
                                tags.toTypedArray(),
                                image
                            ),
                            time,
                            date
                        )
                    )
                    cursor.moveToNext()
                }
            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG_ERROR, "Error in findPhoneByNumber: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return phones
        }

        db.close()
        return phones
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DEFAULT_DB_NAME

        private val create_info_table = "CREATE TABLE IF NOT EXISTS ${DBContract.PhoneInfoEntry.TABLE_NAME} (" +
                " ${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER} TEXT PRIMARY KEY," +
                " ${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME} TEXT," +
                " ${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IS_SPAM} INTEGER," +
                " ${DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IMAGE} TEXT);"

        private val create_log_table = "CREATE TABLE IF NOT EXISTS ${DBContract.PhoneLogEntry.TABLE_NAME} (" +
                "${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER} TEXT, " +
                "${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE} TEXT, " +
                "${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME} TEXT, " +
                "PRIMARY KEY ( ${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER}," +
                             " ${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE}, " +
                             "${DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME}));"

        private val create_tags_table = "CREATE TABLE IF NOT EXISTS ${DBContract.PhoneLogTagsEntry.TABLE_NAME} (" +
                "${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER} TEXT, " +
                "${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG} TEXT, " +
                "PRIMARY KEY (${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER}, " +
                             "${DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG}));"

        private val create_token_table = "CREATE TABLE IF NOT EXISTS ${DBContract.TokenEntry.TABLE_NAME} ( " +
                "${DBContract.TokenEntry.COLUMN_AES_KEY} TEXT, " +
                "${DBContract.TokenEntry.COLUMN_ANDROID_OS } TEXT, " +
                "${DBContract.TokenEntry.COLUMN_DEVICE_ID} TEXT, " +
                "${DBContract.TokenEntry.COLUMN_IS_ACTIVE} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_PRIVATE_KEY} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_REMAIN_COUNT} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_TOKEN} TEXT PRIMARY KEY, " +
                "${DBContract.TokenEntry.COLUMN_IS_PRIMARY_USE} INTEGER);"

        private val SQL_CREATE_ENTRIES = create_info_table + create_log_table + create_tags_table + create_token_table

        private val drop_info = "DROP TABLE IF EXISTS ${DBContract.PhoneInfoEntry.TABLE_NAME};"
        private val drop_log = "DROP TABLE IF EXISTS ${DBContract.PhoneLogEntry.TABLE_NAME};"
        private val drop_tags = "DROP TABLE IF EXISTS ${DBContract.PhoneLogTagsEntry.TABLE_NAME};"
        private val drop_tokens = "DROP TABLE IF EXISTS ${DBContract.TokenEntry.TABLE_NAME};"

        private val SQL_DELETE_ENTRIES = drop_info + drop_log + drop_tags + drop_tokens
    }

}