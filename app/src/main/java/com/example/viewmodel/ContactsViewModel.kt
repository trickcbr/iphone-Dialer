package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Contact
import com.example.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val contacts: StateFlow<List<Contact>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                contactRepository.allContacts
            } else {
                contactRepository.searchContacts(query.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Contact>> = contactRepository.favoriteContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            contactRepository.ensureDefaultContacts()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            contactRepository.toggleFavorite(contact.id, !contact.isFavorite)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = _selectedContact.value?.copy(isFavorite = !contact.isFavorite)
            }
        }
    }

    fun saveContact(
        id: Long = 0,
        name: String,
        phone: String,
        email: String,
        company: String,
        notes: String,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val contact = Contact(
                id = id,
                name = name.trim(),
                phoneNumber = phone.trim(),
                email = email.trim(),
                company = company.trim(),
                notes = notes.trim(),
                isFavorite = isFavorite
            )
            if (id == 0L) {
                val newId = contactRepository.insertContact(contact)
                _selectedContact.value = contact.copy(id = newId)
            } else {
                contactRepository.updateContact(contact)
                _selectedContact.value = contact
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = null
            }
        }
    }

    fun syncWithSystem() {
        viewModelScope.launch {
            try {
                val count = contactRepository.syncSystemContacts()
                _syncMessage.value = if (count > 0) "Synced $count contacts" else "No new contacts found"
            } catch (e: Exception) {
                _syncMessage.value = "Unable to sync: ${e.localizedMessage}"
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
}
