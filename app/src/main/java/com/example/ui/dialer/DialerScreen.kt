package com.example.ui.dialer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Contact
import com.example.ui.components.ContactAvatar
import com.example.ui.components.DialKeypad
import com.example.ui.theme.IosGreen
import com.example.viewmodel.DialerViewModel

@Composable
fun DialerScreen(
    viewModel: DialerViewModel,
    onAddContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialedNumber by viewModel.dialedNumber.collectAsStateWithLifecycle()
    val matchedContacts by viewModel.matchedContacts.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Number display and suggestion
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Suggested matching contact pill
            AnimatedVisibility(
                visible = matchedContacts.isNotEmpty() && dialedNumber.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val matched = matchedContacts.firstOrNull()
                if (matched != null) {
                    SuggestedContactCard(
                        contact = matched,
                        onClick = { viewModel.makeCall(matched.phoneNumber, matched.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dialed number display
            Text(
                text = dialedNumber.ifEmpty { " " },
                fontSize = if (dialedNumber.length > 12) 26.sp else 34.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialed_number_text")
            )

            // Add Number Button
            AnimatedVisibility(
                visible = dialedNumber.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Add Number",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddContact(dialedNumber) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("add_number_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dial Keypad
        DialKeypad(
            buttonSize = 74.dp,
            spacing = 14.dp,
            onDigitClick = { viewModel.onDigitPressed(it) },
            onLongPressZero = { viewModel.onLongPressZero() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom action row: [Quick Add] [Call Button] [Backspace]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left spacer or Quick Add Contact
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialedNumber.isNotEmpty()) {
                    IconButton(
                        onClick = { onAddContact(dialedNumber) },
                        modifier = Modifier.testTag("quick_add_contact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Contact",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Green Call Button
            CallActionButton(
                onClick = { viewModel.makeCall() },
                modifier = Modifier.testTag("call_action_button")
            )

            // Backspace with tap and long press
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialedNumber.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { viewModel.onBackspace() },
                                    onLongPress = { viewModel.onClearAll() }
                                )
                            }
                            .testTag("dialer_backspace_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(if (isPressed) Color(0xFF279944) else IosGreen)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun SuggestedContactCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("suggested_contact_card")
    ) {
        ContactAvatar(
            name = contact.name,
            photoUri = contact.photoUri,
            size = 32.dp,
            fontSize = 13.sp
        )
        Column {
            Text(
                text = contact.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
