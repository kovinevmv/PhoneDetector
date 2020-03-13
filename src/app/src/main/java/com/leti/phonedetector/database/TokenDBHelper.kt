package com.leti.phonedetector.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.leti.phonedetector.*
import com.leti.phonedetector.model.PhoneInfo
import com.leti.phonedetector.model.PhoneLogInfo
import com.leti.phonedetector.model.Token
import kotlin.collections.ArrayList

class TokenDBHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(LOG_TAG_VERBOSE, "Call onCreate class TokenLogDBHelper")

        db.execSQL(create_token_table)
    }

    fun fillSampleData(){
        val tokens = arrayOf(
            Token("hEmffc9d833620e4e13cf96e56d13552c5284f5d99665aa8856a06d4990",
                "389383a471af66f4e84b6722d59b7d45e771620857e579565763e1fe3e8ebd0a",
                "android 5.0",
                "14130e29cebe9c39",
                true,
                2047896,
                200,
                false)
        )

        for (token in tokens)
            this.insertToken(token)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(LOG_TAG_VERBOSE, "Call onUpgrade onCreate class TokenLogDBHelper")
        cleanTables(db)
    }

    private fun cleanTables(db: SQLiteDatabase){
        db.execSQL(drop_tokens)

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
    fun insertToken(token: Token): Boolean {
        Log.d(LOG_TAG_VERBOSE, "Call insertToken with param Token: '${token.token}'")

        val foundToken = this.findToken(token.token)
        Log.d(LOG_TAG_VERBOSE, "Found token: ${foundToken?.token}")

        if (foundToken != null){
            this.deleteToken(token.token)
        }

        val db = writableDatabase

        val valuesInfo = ContentValues()
        valuesInfo.put(DBContract.TokenEntry.COLUMN_TOKEN, token.token)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_AES_KEY, token.aesKey)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_ANDROID_OS, token.androidOS)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_DEVICE_ID, token.deviceId)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_IS_ACTIVE, token.isActive)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_PRIVATE_KEY, token.privateKey)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_REMAIN_COUNT, token.remainCount)
        valuesInfo.put(DBContract.TokenEntry.COLUMN_IS_PRIMARY_USE, token.isPrimaryUse)

        db.close()
        return true
    }


    @Throws(SQLiteConstraintException::class)
    fun deleteToken(token : String) : Boolean{
        Log.d(LOG_TAG_VERBOSE, "Call deleteToken with param token: '$token'")

        val db = writableDatabase
        // TODO SQL Injection
        db.execSQL("DELETE FROM ${DBContract.TokenEntry.TABLE_NAME} WHERE ${DBContract.TokenEntry.COLUMN_TOKEN} = '$token'")
        db.close()
        return true
    }

    @SuppressLint("Recycle")
    @Throws(SQLiteConstraintException::class)
    fun findToken(tokenInput: String): Token? {
        // TODO SQL Injection
        Log.d(LOG_TAG_VERBOSE, "Call findToken with param token: '$tokenInput'")

        val tokens = ArrayList<Token>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM ${DBContract.TokenEntry.TABLE_NAME} WHERE " +
                    "${DBContract.TokenEntry.TABLE_NAME}.${DBContract.TokenEntry.COLUMN_TOKEN} " +
                    "= \"${tokenInput}\";", null)

            if (cursor!!.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    tokens.add(parseCursor(cursor))
                    cursor.moveToNext()
                }
            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG_ERROR, "Error in findToken: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return null
        }

        db.close()
        return if (tokens.size > 0) tokens[0] else null
    }

    fun getTokens(): ArrayList<Token> {
        // TODO SQL Injection
        Log.d(LOG_TAG_VERBOSE, "Call getTokens")

        val tokens = ArrayList<Token>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM ${DBContract.TokenEntry.TABLE_NAME};", null)

            if (cursor!!.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    tokens.add(parseCursor(cursor))
                    cursor.moveToNext()
                }
            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG_ERROR, "Error in getTokens: $e")
            db.execSQL(SQL_CREATE_ENTRIES)
            db.close()
            return tokens
        }

        db.close()
        return tokens
    }

    private fun parseCursor(cursor : Cursor) : Token{
        val aesKey = cursor.getString(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_AES_KEY))
        val androidOS = cursor.getString(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_ANDROID_OS))
        val deviceId = cursor.getString(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_DEVICE_ID))
        val isActive = cursor.getInt(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_IS_ACTIVE)) != 0
        val privateKey = cursor.getInt(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_PRIVATE_KEY))
        val remainCount = cursor.getInt(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_REMAIN_COUNT))
        val token = cursor.getString(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_TOKEN))
        val isPrimaryUse = cursor.getInt(cursor.getColumnIndex(DBContract.TokenEntry.COLUMN_IS_PRIMARY_USE)) != 0

        return Token(token, aesKey, androidOS, deviceId, isActive, privateKey, remainCount, isPrimaryUse)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = DEFAULT_DB_NAME

        private val create_token_table = "CREATE TABLE IF NOT EXISTS ${DBContract.TokenEntry.TABLE_NAME} ( " +
                "${DBContract.TokenEntry.COLUMN_AES_KEY} TEXT, " +
                "${DBContract.TokenEntry.COLUMN_ANDROID_OS } TEXT, " +
                "${DBContract.TokenEntry.COLUMN_DEVICE_ID} TEXT, " +
                "${DBContract.TokenEntry.COLUMN_IS_ACTIVE} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_PRIVATE_KEY} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_REMAIN_COUNT} INTEGER, " +
                "${DBContract.TokenEntry.COLUMN_TOKEN} TEXT PRIMARY KEY, " +
                "${DBContract.TokenEntry.COLUMN_IS_PRIMARY_USE} INTEGER);"

        private val SQL_CREATE_ENTRIES = create_token_table

        private val drop_tokens = "DROP TABLE IF EXISTS ${DBContract.TokenEntry.TABLE_NAME};"

        private val SQL_DELETE_ENTRIES = drop_tokens
    }

}