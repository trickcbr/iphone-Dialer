package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.AppDatabase
import com.example.repository.AppThemeMode
import com.example.repository.CallHistoryRepository
import com.example.repository.ContactRepository
import com.example.repository.RecordingRepository
import com.example.repository.SettingsRepository
import com.example.telecom.CallManager
import com.example.ui.call.ActiveCallScreen
import com.example.ui.contacts.AddEditContactDialog
import com.example.ui.contacts.ContactsScreen
import com.example.ui.dialer.DialerScreen
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.recents.RecentsScreen
import com.example.ui.recordings.RecordingsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.IosBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CallViewModel
import com.example.viewmodel.ContactsViewModel
import com.example.viewmodel.DialerViewModel
import com.example.viewmodel.RecentsViewModel
import com.example.viewmodel.RecordingsViewModel

enum class DialerTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FAVORITES("Favorites", Icons.Filled.Star, Icons.Outlined.StarOutline),
    RECENTS("Recents", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    CONTACTS("Contacts", Icons.Filled.People, Icons.Outlined.People),
    KEYPAD("Keypad", Icons.Filled.Dialpad, Icons.Outlined.Dialpad),
    RECORDINGS("Recordings", Icons.Filled.Mic, Icons.Outlined.Mic),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var contactRepository: ContactRepository
    private lateinit var callHistoryRepository: CallHistoryRepository
    private lateinit var recordingRepository: RecordingRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var callManager: CallManager

    private var initialDialNumber by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        contactRepository = ContactRepository(database.contactDao(), applicationContext)
        callHistoryRepository = CallHistoryRepository(database.callLogDao())
        recordingRepository = RecordingRepository(database.recordingDao(), applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        callManager = CallManager.getInstance(
            applicationContext,
            callHistoryRepository,
            recordingRepository
        )

        handleIncomingIntent(intent)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(
                    contactRepository = contactRepository,
                    callHistoryRepository = callHistoryRepository,
                    recordingRepository = recordingRepository,
                    settingsRepository = settingsRepository,
                    callManager = callManager,
                    initialDialNumber = initialDialNumber
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if ((action == Intent.ACTION_DIAL || action == Intent.ACTION_VIEW) && data != null) {
            val scheme = data.scheme
            if (scheme == "tel") {
                initialDialNumber = data.schemeSpecificPart
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    contactRepository: ContactRepository,
    callHistoryRepository: CallHistoryRepository,
    recordingRepository: RecordingRepository,
    settingsRepository: SettingsRepository,
    callManager: CallManager,
    initialDialNumber: String? = null
) {
    var currentTab by remember { mutableStateOf(DialerTab.KEYPAD) }
    var showAddContactFromDialer by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext

    val dialerViewModel = remember {
        DialerViewModel(contactRepository, settingsRepository, callManager, context)
    }
    val contactsViewModel = remember { ContactsViewModel(contactRepository) }
    val recentsViewModel = remember { RecentsViewModel(callHistoryRepository, recordingRepository) }
    val recordingsViewModel = remember {
        RecordingsViewModel(recordingRepository, context)
    }
    val callViewModel = remember { CallViewModel(callManager) }

    val activeCall by callViewModel.activeCall.collectAsStateWithLifecycle()

    // Request necessary permissions on start
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] == true) {
            contactsViewModel.syncWithSystem()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    LaunchedEffect(initialDialNumber) {
        initialDialNumber?.let { number ->
            dialerViewModel.setNumber(number)
            currentTab = DialerTab.KEYPAD
        }
    }

    // Handle back button on sub tabs
    BackHandler(enabled = currentTab != DialerTab.KEYPAD) {
        currentTab = DialerTab.KEYPAD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (activeCall == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    DialerTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IosBlue,
                                selectedTextColor = IosBlue,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Tab Content
            when (currentTab) {
                DialerTab.FAVORITES -> {
                    FavoritesScreen(
                        viewModel = contactsViewModel,
                        onCallClick = { phone, name ->
                            dialerViewModel.makeCall(phone, name)
                        }
                    )
                }

                DialerTab.RECENTS -> {
                    RecentsScreen(
                        viewModel = recentsViewModel,
                        onCallClick = { phone ->
                            dialerViewModel.makeCall(phone)
                        },
                        onPlayRecording = { rec ->
                            recordingsViewModel.playRecording(rec)
                            currentTab = DialerTab.RECORDINGS
                        },
                        onShareRecording = { rec ->
                            recordingsViewModel.shareRecording(rec)
                        }
                    )
                }

                DialerTab.CONTACTS -> {
                    ContactsScreen(
                        viewModel = contactsViewModel,
                        onCallClick = { phone, name ->
                            dialerViewModel.makeCall(phone, name)
                        }
                    )
                }

                DialerTab.KEYPAD -> {
                    DialerScreen(
                        viewModel = dialerViewModel,
                        onAddContact = { number ->
                            showAddContactFromDialer = number
                        }
                    )
                }

                DialerTab.RECORDINGS -> {
                    RecordingsScreen(
                        viewModel = recordingsViewModel
                    )
                }

                DialerTab.SETTINGS -> {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        onClearHistory = {
                            recentsViewModel.clearAllHistory()
                        }
                    )
                }
            }

            // Quick add contact dialog from dialer screen
            showAddContactFromDialer?.let { number ->
                AddEditContactDialog(
                    prefilledPhone = number,
                    onDismiss = { showAddContactFromDialer = null },
                    onSave = { _, name, phone, email, company, notes, isFavorite ->
                        contactsViewModel.saveContact(
                            id = 0L,
                            name = name,
                            phone = phone,
                            email = email,
                            company = company,
                            notes = notes,
                            isFavorite = isFavorite
                        )
                        showAddContactFromDialer = null
                    }
                )
            }

            // Active In-Call Screen Overlay (full screen with slide transition)
            AnimatedVisibility(
                visible = activeCall != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ActiveCallScreen(
                    viewModel = callViewModel,
                    onAddCallContactPicker = {
                        // Quick contact selector placeholder during call
                    }
                )
            }
        }
    }
}
