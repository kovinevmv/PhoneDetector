package com.leti.phonedetector.bitmap

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import java.io.ByteArrayOutputStream


class BitmapReader {
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio =
                Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio =
                Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    fun decodeFromResource(res: String,  reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(res, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(res, options)
    }

    fun readFile(res: String): ArrayList<ContentValues> {
        val data = ArrayList<ContentValues>()
        val bit = decodeFromResource(res, 512, 512)

        val stream = ByteArrayOutputStream()
        bit.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        bit.recycle()

        val row = ContentValues()
        row.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
        row.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray)
        data.add(row)
        return data
    }
}