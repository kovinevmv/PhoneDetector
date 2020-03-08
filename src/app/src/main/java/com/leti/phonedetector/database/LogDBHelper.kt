package com.leti.phonedetector.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.leti.phonedetector.PhoneInfo

class PhoneLogDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    @Throws(SQLiteConstraintException::class)
    fun insertPhone(phone: PhoneInfo): Boolean {
        val db = writableDatabase

        val values_info = ContentValues()
        values_info.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NAME, phone.name)
        values_info.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_NUMBER, phone.number)
        values_info.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IS_SPAM, phone.isSpam)
        values_info.put(DBContract.PhoneInfoEntry.COLUMN_INFO_PHONE_IMAGE, phone.image)
        db.insert(DBContract.PhoneLogEntry.TABLE_NAME, null, values_info)

        val values_log = ContentValues()
        values_info.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER, phone.number)
        values_log.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_TIME, phone.time)
        values_log.put(DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_DATE, phone.date)
        db.insert(DBContract.PhoneLogEntry.TABLE_NAME, null, values_log)

        for (tag in phone.tags){
            val values_tags = ContentValues()
            values_tags.put(DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_NUMBER, phone.number)
            values_tags.put(DBContract.PhoneLogTagsEntry.COLUMN_PHONE_LOG_TAGS_TAG, tag)
            db.insert(DBContract.PhoneLogTagsEntry.TABLE_NAME, null, values_tags)
        }

        return true
    }

//    @Throws(SQLiteConstraintException::class)
//    fun deletePhone(number: String): Boolean {
//        val db = writableDatabase
//
//        // TODO SQL Injection
//        val selection = DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER + " LIKE ?"
//        val selectionArgs = arrayOf(number)
//
//        db.delete(DBContract.PhoneLogEntry.TABLE_NAME, selection, selectionArgs)
//        db.delete(DBContract.PhoneLogTagsEntry.TABLE_NAME, selection, selectionArgs)
//
//        return true
//    }
//
//    fun readPhone(phone: String): ArrayList<PhoneInfo> {
//        val users = ArrayList<PhoneInfo>()
//        val db = writableDatabase
//        var cursor: Cursor? = null
//
//        try {
//            // TODO join with TAGS
//            // TODO SQL Injection
//            cursor = db.rawQuery("SELECT * from " + DBContract.PhoneLogEntry + " WHERE " + DBContract.PhoneLogEntry.COLUMN_LOG_PHONE_NUMBER + "='" + phone + "'", null)
//
//        } catch (e: SQLiteException) {
//            // if table not yet present, create it
//            db.execSQL(SQL_CREATE_ENTRIES)
//            return ArrayList()
//        }
//
//        var name: String
//        var age: String
//        if (cursor!!.moveToFirst()) {
//            while (cursor.isAfterLast == false) {
//                name = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NAME))
//                age = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_AGE))
//
//                users.add(UserModel(userid, name, age))
//                cursor.moveToNext()
//            }
//        }
//        return users
//    }
//
//    fun readAllUsers(): ArrayList<UserModel> {
//        val users = ArrayList<UserModel>()
//        val db = writableDatabase
//        var cursor: Cursor? = null
//        try {
//            cursor = db.rawQuery("select * from " + DBContract.UserEntry.TABLE_NAME, null)
//        } catch (e: SQLiteException) {
//            db.execSQL(SQL_CREATE_ENTRIES)
//            return ArrayList()
//        }
//
//        var userid: String
//        var name: String
//        var age: String
//        if (cursor!!.moveToFirst()) {
//            while (cursor.isAfterLast == false) {
//                userid = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_USER_ID))
//                name = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_NAME))
//                age = cursor.getString(cursor.getColumnIndex(DBContract.UserEntry.COLUMN_AGE))
//
//                users.add(UserModel(userid, name, age))
//                cursor.moveToNext()
//            }
//        }
//        return users
//    }
//
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "phonedetector.db"

        private val SQL_CREATE_ENTRIES = ""
//            "CREATE TABLE " + DBContract.PhoneLogEntry.TABLE_NAME + " (" +
//                    DBContract.UserEntry.COLUMN_USER_ID + " TEXT PRIMARY KEY," +
//                    DBContract.UserEntry.COLUMN_NAME + " TEXT," +
//                    DBContract.UserEntry.COLUMN_AGE + " TEXT)"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBContract.PhoneLogEntry.TABLE_NAME
    }

}