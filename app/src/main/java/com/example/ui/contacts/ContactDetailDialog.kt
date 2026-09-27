package com.example.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow

@Composable
fun ContactDetailDialog(
    contact: Contact,
    onDismiss: () -> Unit,
    onCallClick: (String, String) -> Unit,
    onEditClick: (Contact) -> Unit,
    onDeleteClick: (Contact) -> Unit,
    onToggleFavorite: (Contact) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ContactAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 72.dp,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = contact.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (contact.company.isNotEmpty()) {
                    Text(
                        text = contact.company,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Action Circles row: Call, Message, Mail, Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ContactActionCircle(
                        icon = Icons.Default.Call,
                        label = "call",
                        tint = IosGreen,
                        onClick = {
                            onCallClick(contact.phoneNumber, contact.name)
                            onDismiss()
                        }
                    )
                    ContactActionCircle(
                        icon = Icons.Default.Message,
                        label = "message",
                        tint = IosBlue,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phoneNumber}"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                    if (contact.email.isNotEmpty()) {
                        ContactActionCircle(
                            icon = Icons.Default.Email,
                            label = "mail",
                            tint = IosBlue,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email}"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                    ContactActionCircle(
                        icon = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        label = "favorite",
                        tint = if (contact.isFavorite) IosYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { onToggleFavorite(contact) }
                    )
                }

                // Phone details card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            onCallClick(contact.phoneNumber, contact.name)
                            onDismiss()
                        }
                        .padding(14.dp)
                ) {
                    Text(
                        text = contact.label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = contact.phoneNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = IosBlue
                    )
                }

                // Email details card if present
                if (contact.email.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Email",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contact.email,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Notes card if present
                if (contact.notes.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Notes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contact.notes,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onEditClick(contact)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("edit_contact_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IosBlue)
                }

                IconButton(
                    onClick = {
                        onDeleteClick(contact)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("delete_contact_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IosRed)
                }

                Button(
                    onClick = {
                        onCallClick(contact.phoneNumber, contact.name)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("contact_call_button")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Call")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ContactActionCircle(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
