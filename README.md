# iPhone-style Dialer for Android

A clean, premium Android dialer application inspired by the simplicity and aesthetics of modern iOS design, built with Kotlin, Jetpack Compose, Material 3, Room Database, and Android Telecom integration.

---

## Features

1. **Phone / Keypad Screen**
   - Authentic circular 3×4 numeric dialpad (0–9, \*, #) with alphabetic subtext (ABC, DEF...).
   - Long-press `0` to insert `+`.
   - Real-time DTMF audio feedback and subtle haptic vibration.
   - Dynamic contact lookup matching numbers and names as you type.
   - One-tap "Add Number" shortcut to create new contacts.
   - Backspace with tap and long-press to clear all digits.
   - Large green call action button.

2. **Recent Calls**
   - Filter by **All** or **Missed** calls.
   - Visual badges for incoming (blue), outgoing (green), and missed calls (red).
   - Timestamp and call duration.
   - Call audio recording indicator (`REC`) for recorded conversations.
   - Info modal (ⓘ) to inspect timestamps, duration, and listen to attached audio recordings.
   - Clear all call logs with safety confirmation.

3. **Contacts Management**
   - Alphabetically grouped contact list with fast section headers.
   - Instant search filter by name, company, or phone number.
   - Add, edit, and delete contacts with full field support (Name, Phone, Email, Company, Notes, Favorite).
   - Fast one-tap calling, messaging (SMS), and email from contact card.
   - System contact synchronization via Android Contacts Provider.

4. **Favorites**
   - Dedicated favorites screen displaying starred contacts in a clean card grid.
   - One-tap quick dialing.
   - Add / remove contacts from favorites.

5. **In-Call Screen**
   - Deep immersive dark aesthetic with contact avatar and name.
   - Live call duration timer (`00:00`).
   - 2×3 iOS-style glassmorphic control grid:
     - **Mute**: Toggles microphone input.
     - **Keypad**: Opens compact DTMF dialpad overlay to send DTMF tones during interactive voice response (IVR).
     - **Speaker**: Toggles audio speakerphone route.
     - **Add Call**: Conference / multi-call affordance.
     - **Hold**: Toggles call on-hold status.
     - **Record**: Toggles live audio recording with pulsing indicator (`● REC AUDIO`).
   - Prominent red circular End Call button.

6. **Audio Call Recording**
   - Dedicated **Recordings** tab to review, play, rename, and share recordings.
   - Built-in `AudioPlayerBar` with play/pause, interactive scrub slider, and duration counter.
   - Share recordings safely using Android `FileProvider`.
   - Deletion dialog with safety confirmation.
   - Storage usage meter in kilobytes / megabytes.

7. **Default Phone App (Telecom Role)**
   - Integrated with Android `RoleManager` (Android 10+) and `TelecomManager` fallback.
   - `InCallService` declared in `AndroidManifest.xml` with `BIND_INCALL_SERVICE` and `IN_CALL_SERVICE_UI`.
   - Setup card in Settings and Dialer banner to easily grant the default dialer role.

8. **Room Database Persistence**
   - Relational Room database storing:
     - `Contact` (Name, phone, email, notes, favorites).
     - `CallRecord` (Phone, contact name, type, duration, recording link).
     - `Recording` (File path, size, duration, timestamp).

9. **Settings & Customization**
   - Appearance theme switcher: System Default, Light Mode, Dark Mode.
   - Auto-record toggle.
   - Keypad audio DTMF tones and vibration haptic toggles.
   - Detailed permissions status checker.
   - Android call recording policy documentation and troubleshooting guide.

---

## Project Structure

```
app/
├── data/
├── database/
│   ├── AppDatabase.kt           # Room Database definition & singleton
│   ├── ContactDao.kt            # Contacts queries & search
│   ├── CallLogDao.kt            # Call history queries & filters
│   └── RecordingDao.kt          # Audio recordings queries & storage tracking
├── model/
│   ├── Contact.kt               # Contact entity
│   ├── CallRecord.kt            # CallRecord entity (Incoming, Outgoing, Missed)
│   ├── Recording.kt             # Recording entity
│   ├── CallState.kt             # ActiveCallInfo and CallStatus
│   └── DialKey.kt               # Keypad layout mapping
├── repository/
│   ├── ContactRepository.kt     # Contact caching, seeding, and system sync
│   ├── CallHistoryRepository.kt # History management & seeding
│   ├── RecordingRepository.kt   # Disk file management & FileProvider sharing
│   └── SettingsRepository.kt    # SharedPreferences (theme, auto-record, haptics)
├── telecom/
│   ├── AppInCallService.kt      # Android Telecom InCallService
│   ├── CallManager.kt           # Call lifecycle, audio routing, timers, DTMF
│   └── AudioRecorderHelper.kt   # MediaRecorder wrapper with safe fallbacks
├── viewmodel/
│   ├── DialerViewModel.kt       # Keypad logic & suggestions
│   ├── ContactsViewModel.kt     # Contact CRUD & system sync
│   ├── RecentsViewModel.kt      # Recents filtering & search
│   ├── RecordingsViewModel.kt   # Audio playback & file actions
│   └── CallViewModel.kt         # In-call controls & state
├── ui/
│   ├── theme/                   # Material 3 colors, typography, and themes
│   ├── components/              # DialKeypad, ContactAvatar, AudioPlayerBar
│   ├── dialer/                  # DialerScreen
│   ├── recents/                 # RecentsScreen & CallDetailDialog
│   ├── contacts/                # ContactsScreen & AddEditContactDialog
│   ├── favorites/               # FavoritesScreen
│   ├── call/                    # ActiveCallScreen
│   ├── recordings/              # RecordingsScreen
│   └── settings/                # SettingsScreen
└── MainActivity.kt              # Bottom navigation, permission flows, incoming intent handling
```

---

## Step-by-Step Instructions

### 1. Opening the Project in Android Studio
1. Launch **Android Studio** (Ladybug, Iguana, Hedgehog, or newer).
2. Select **File > Open...** (or click **Open** on the welcome window).
3. Navigate to this project folder root directory and click **OK**.
4. Allow Android Studio to index the project files.

### 2. Syncing Gradle
1. Click the **Sync Project with Gradle Files** button in the toolbar (the elephant icon with blue arrow), or choose **File > Sync Project with Gradle Files**.
2. Wait for Gradle to download dependencies and build the model.
3. Ensure JDK 17 or JDK 21 is selected under **Settings / Preferences > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**.

### 3. Building the APK
- **Debug APK**:
  - Run the Gradle task in the terminal:
    ```bash
    ./gradlew assembleDebug
    ```
  - Or via menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
  - The generated APK will be placed at:
    `app/build/outputs/apk/debug/app-debug.apk`

- **Release APK**:
  - Run:
    ```bash
    ./gradlew assembleRelease
    ```

### 4. Installing the APK on an Android Phone
1. Enable **Developer Options** and **USB Debugging** on your phone:
   - Go to **Settings > About Phone** and tap **Build Number** 7 times.
   - Go to **Settings > System > Developer Options** and enable **USB Debugging**.
2. Connect your phone via USB cable and allow debugging when prompted.
3. In Android Studio, select your physical phone in the device dropdown and click **Run 'app'** (green play triangle).
4. Alternatively, install via ADB command:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### 5. Setting the App as the Default Dialer
1. Open the app on your phone.
2. If you see the "Default Phone App" banner on the Keypad or in Settings:
   - Tap **"Set as Default Dialer"**.
   - A system dialog from Android's `RoleManager` will appear asking: *"Set iPhone-style Dialer as your default phone app?"*.
   - Tap **Set as default**.
3. Or manually in Android System Settings:
   - Go to **Settings > Apps > Default Apps > Phone app**.
   - Select **iPhone-style Dialer**.

### 6. Testing Calling
- **Placing a Call**:
  - Open the **Keypad** tab.
  - Type any phone number (e.g., `+1 555 123 4567`).
  - Tap the green **Call** button.
  - The app displays the in-call screen with live duration timer.
- **In-Call Actions**:
  - Tap **Mute** to toggle microphone input.
  - Tap **Speaker** to toggle speakerphone.
  - Tap **Keypad** to send DTMF tones to automated systems.
  - Tap **Record** to test live call recording.
  - Tap **End Call** to hang up and automatically log the call.
- **Checking Recents**:
  - Switch to the **Recents** tab to see your logged call with duration and timestamp.

### 7. Testing Recording on a Supported Device
1. During an active call, tap the **Record** button in the in-call controls grid.
2. Read and accept the legal consent dialog if prompted for the first time.
3. Grant the **Record Audio** permission when requested by Android.
4. The recording indicator (`● REC AUDIO`) pulses in the upper section of the call screen.
5. Tap **End Call** (or tap **Record** again) to finish.
6. Switch to the **Recordings** tab:
   - Tap the recording to play audio with the scrubber slider.
   - Use the **Share** button to send via WhatsApp, Drive, Gmail, or Bluetooth.
   - Use the **Rename** button to assign custom file names.
   - Use the **Delete** button to safely remove the file.

### 8. Troubleshooting Unsupported Call-Recording Situations

#### Why does Android restrict call recording?
- Starting in **Android 9 (Pie)** and strictly enforced in **Android 10+ (API 29–36)**, Google restricted third-party apps from directly capturing the two-way voice call stream (`MediaRecorder.AudioSource.VOICE_CALL`) for user privacy and wiretapping compliance.
- Third-party apps are permitted to record using `MediaRecorder.AudioSource.VOICE_COMMUNICATION` or `MediaRecorder.AudioSource.MIC`.

#### Troubleshooting Tips:
1. **One-sided audio (only your voice is heard)**:
   - On Android 10 and newer devices, the cellular downlink audio cannot be accessed directly without vendor system privileges.
   - **Solution**: Switch the call to **Speakerphone** mode. The microphone captures both sides of the conversation cleanly with high intelligibility.
2. **Audio recording permission denied**:
   - Go to phone **Settings > Apps > iPhone-style Dialer > Permissions**.
   - Ensure **Microphone** is set to **"Allow only while using the app"**.
3. **No SIM card or Emulator environment**:
   - The app has an interactive call simulation engine that fully works even on emulators or Wi-Fi-only tablets without a cellular carrier, allowing you to test in-call timers, keypad DTMF tones, and audio recording without a live SIM card.
4. **Battery Optimization killing recording**:
   - Go to phone **Settings > Apps > iPhone-style Dialer > Battery**.
   - Select **Unrestricted** to prevent the OS from killing background audio processes during long calls.

---

## License & Privacy Notice
Audio recordings and contact data are stored strictly on the local device within private application sandbox storage. No voice audio or personal contact data is transmitted to external servers.
