package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.database.AppDatabase
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.Contact
import com.example.model.Recording
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("iPhone-style Dialer", appName)
    }

    @Test
    fun testInsertAndRetrieveContact() = runBlocking {
        val contactDao = database.contactDao()
        val testContact = Contact(
            name = "Sarah Connor",
            phoneNumber = "+1 (555) 999-0000",
            email = "sarah@resistance.org",
            isFavorite = true
        )
        val id = contactDao.insertContact(testContact)
        assertTrue(id > 0)

        val retrieved = contactDao.getContactById(id)
        assertNotNull(retrieved)
        assertEquals("Sarah Connor", retrieved?.name)
        assertTrue(retrieved?.isFavorite == true)
    }

    @Test
    fun testCallHistoryLogging() = runBlocking {
        val callDao = database.callLogDao()
        val call = CallRecord(
            phoneNumber = "+1 (555) 123-4567",
            contactName = "John Connor",
            callType = CallType.OUTGOING,
            durationSeconds = 120,
            hasRecording = false
        )
        val callId = callDao.insertCallRecord(call)
        assertTrue(callId > 0)

        val records = callDao.getAllCallRecords().first()
        assertTrue(records.any { it.phoneNumber == "+1 (555) 123-4567" })
    }

    @Test
    fun testRecordingPersistence() = runBlocking {
        val recordingDao = database.recordingDao()
        val recording = Recording(
            phoneNumber = "+1 (555) 123-4567",
            contactName = "John Connor",
            filePath = "/data/data/com.example/files/test.m4a",
            fileName = "test.m4a",
            durationSeconds = 60,
            fileSize = 10240
        )
        val recId = recordingDao.insertRecording(recording)
        assertTrue(recId > 0)

        val retrieved = recordingDao.getRecordingById(recId)
        assertNotNull(retrieved)
        assertEquals("test.m4a", retrieved?.fileName)
    }
}
