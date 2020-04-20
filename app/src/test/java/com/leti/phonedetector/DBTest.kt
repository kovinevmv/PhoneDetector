package com.leti.phonedetector

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneLogInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DataBaseTest {
    private lateinit var db: PhoneLogDBHelper

    @Before
    fun fillDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = PhoneLogDBHelper(context)
        db.fillSampleData()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val context = ApplicationProvider.getApplicationContext<Context>()
        Assert.assertEquals("com.leti.phonedetector", context.packageName)
    }

    @Test
    fun testConnect() {
        val data = db.readPhoneLog()
        data.forEach { it ->
            Assert.assertNotNull(it.name)
            Assert.assertNotNull(it.number)
            Assert.assertNotNull(it.isSpam)
            Assert.assertNotNull(it.time)
            Assert.assertNotNull(it.date)
        }
    }

    @Test
    fun testClean() {
        db.cleanTables()
        Assert.assertEquals(0, db.readPhoneLog().count())
        db.fillSampleData()
    }

    @Test
    fun testInsert() {
        val phone = PhoneLogInfo(
        "Test123",
        "+79999999999",
        true,
        date = "2020.01.01",
        time = "20:01"
        )
        db.insertPhone(phone)
        val data = db.readPhoneLog()
        Assert.assertEquals(1, data.filter { it -> it.name == "Test123" }.count())
    }

    @Test
    fun testDelete() {
        db.deletePhoneInfo("+79999999999")
        val data = db.readPhoneLog()
        Assert.assertEquals(0, data.filter { it -> it.number == "+79999999999" }.count())
    }
}