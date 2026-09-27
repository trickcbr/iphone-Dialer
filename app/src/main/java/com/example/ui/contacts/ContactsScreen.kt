package com.example.ui.contacts

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Contact
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosYellow
import com.example.viewmodel.ContactsViewModel

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onCallClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncWithSystem()
        }
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    // Group contacts alphabetically by first letter
    val grouped = remember(contacts) {
        contacts.groupBy { it.name.trim().firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Title & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contacts",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        },
                        modifier = Modifier.testTag("sync_contacts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync System Contacts",
                            tint = IosBlue
                        )
                    }

                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_contact_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                            tint = IosBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_contacts_input")
            )

            // Contact list
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching contacts" else "No Contacts",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (initial, contactsUnderLetter) ->
                        item(key = "header_$initial") {
                            Text(
                                text = initial.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        items(contactsUnderLetter, key = { it.id }) { contact ->
                            ContactRowItem(
                                contact = contact,
                                onClick = { viewModel.selectContact(contact) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 64.dp)
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }

    // Detail dialog
    selectedContact?.let { contact ->
        ContactDetailDialog(
            contact = contact,
            onDismiss = { viewModel.selectContact(null) },
            onCallClick = onCallClick,
            onEditClick = { contactToEdit = it },
            onDeleteClick = { viewModel.deleteContact(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) }
        )
    }

    // Add dialog
    if (showAddDialog) {
        AddEditContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { _, name, phone, email, company, notes, isFavorite ->
                viewModel.saveContact(
                    id = 0L,
                    name = name,
                    phone = phone,
                    email = email,
                    company = company,
                    notes = notes,
                    isFavorite = isFavorite
                )
            }
        )
    }

    // Edit dialog
    contactToEdit?.let { contact ->
        AddEditContactDialog(
            initialContact = contact,
            onDismiss = { contactToEdit = null },
            onSave = { id, name, phone, email, company, notes, isFavorite ->
                viewModel.saveContact(
                    id = id,
                    name = name,
                    phone = phone,
                    email = email,
                    company = company,
                    notes = notes,
                    isFavorite = isFavorite
                )
                contactToEdit = null
            }
        )
    }
}

@Composable
private fun ContactRowItem(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("contact_item_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = contact.name,
            photoUri = contact.photoUri,
            size = 38.dp,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (contact.company.isNotEmpty()) {
                Text(
                    text = contact.company,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (contact.isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite",
                tint = IosYellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
