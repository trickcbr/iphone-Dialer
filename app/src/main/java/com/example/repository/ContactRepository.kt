package com.example.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.example.database.ContactDao
import com.example.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContactRepository(
    private val contactDao: ContactDao,
    private val context: Context
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<Contact>> = contactDao.getFavoriteContacts()

    fun searchContacts(query: String): Flow<List<Contact>> = contactDao.searchContacts(query)

    suspend fun getContactById(id: Long): Contact? = contactDao.getContactById(id)

    suspend fun getContactByPhone(phoneNumber: String): Contact? = contactDao.getContactByPhone(phoneNumber)

    suspend fun lookupMatches(digits: String): List<Contact> = contactDao.lookupMatches(digits)

    suspend fun insertContact(contact: Contact): Long = contactDao.insertContact(contact)

    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = contactDao.updateFavoriteStatus(id, isFavorite)

    suspend fun ensureDefaultContacts() = withContext(Dispatchers.IO) {
        if (contactDao.getContactCount() == 0) {
            val sampleContacts = listOf(
                Contact(name = "Alexander Wright", phoneNumber = "+1 (555) 234-5678", email = "alex.wright@example.com", company = "Design Studio", isFavorite = true),
                Contact(name = "Alice Morgan", phoneNumber = "+1 (555) 345-6789", email = "alice.m@techcorp.io", company = "TechCorp", isFavorite = true),
                Contact(name = "Benjamin Clark", phoneNumber = "+1 (555) 456-7890", email = "ben.clark@venture.co", company = "Venture Partners", isFavorite = false),
                Contact(name = "Chloe Davis", phoneNumber = "+1 (555) 567-8901", email = "chloe.davis@art.org", company = "Modern Gallery", isFavorite = false),
                Contact(name = "David Miller", phoneNumber = "+1 (555) 678-9012", email = "dmiller@cloudsys.com", company = "Cloud Systems", isFavorite = true),
                Contact(name = "Elena Rostova", phoneNumber = "+1 (555) 789-0123", email = "elena.r@musiclab.net", company = "Acoustic Labs", isFavorite = true),
                Contact(name = "Gabriel Hayes", phoneNumber = "+1 (555) 890-1234", email = "gabriel@hayeslegal.com", company = "Hayes Legal", isFavorite = false),
                Contact(name = "Hannah Abbott", phoneNumber = "+1 (555) 901-2345", email = "hannah.abbott@botanics.com", company = "Botanics Flora", isFavorite = false),
                Contact(name = "James Wilson", phoneNumber = "+1 (555) 012-3456", email = "j.wilson@globalfin.org", company = "Global Finance", isFavorite = true),
                Contact(name = "Lucas Martinez", phoneNumber = "+1 (555) 123-9876", email = "lucas.m@studioflow.com", company = "Studio Flow", isFavorite = false),
                Contact(name = "Olivia Parker", phoneNumber = "+1 (555) 234-8765", email = "olivia@creativelab.co", company = "Creative Lab", isFavorite = true),
                Contact(name = "Sophia Chen", phoneNumber = "+1 (555) 345-7654", email = "sophia.chen@biomed.edu", company = "BioMed Institute", isFavorite = false)
            )
            contactDao.insertAll(sampleContacts)
        }
    }

    suspend fun syncSystemContacts(): Int = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        var count = 0
        cursor?.use { c ->
            val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            val imported = mutableListOf<Contact>()
            while (c.moveToNext()) {
                val name = if (nameIndex >= 0) c.getString(nameIndex) ?: "Unknown" else "Unknown"
                val number = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                val photo = if (photoIndex >= 0) c.getString(photoIndex) else null

                if (number.isNotBlank()) {
                    imported.add(
                        Contact(
                            name = name,
                            phoneNumber = number,
                            photoUri = photo
                        )
                    )
                    count++
                }
            }
            if (imported.isNotEmpty()) {
                contactDao.insertAll(imported)
            }
        }
        count
    }
}
