package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val AvatarColors = listOf(
    Pair(Color(0xFF8E8E93), Color(0xFF636366)),
    Pair(Color(0xFFFF9500), Color(0xFFFF5E3A)),
    Pair(Color(0xFF5856D6), Color(0xFF3634A3)),
    Pair(Color(0xFF34C759), Color(0xFF248A3D)),
    Pair(Color(0xFF007AFF), Color(0xFF0051A8)),
    Pair(Color(0xFFAF52DE), Color(0xFF7B33A8)),
    Pair(Color(0xFFFF2D55), Color(0xFFD6183F))
)

@Composable
fun ContactAvatar(
    name: String,
    photoUri: String? = null,
    size: Dp = 48.dp,
    fontSize: TextUnit = 18.sp,
    modifier: Modifier = Modifier
) {
    if (!photoUri.isNullOrEmpty()) {
        AsyncImage(
            model = photoUri,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        val initials = extractInitials(name)
        val colorIndex = (name.hashCode().let { if (it < 0) -it else it }) % AvatarColors.size
        val (c1, c2) = AvatarColors[colorIndex]

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(c1, c2))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun extractInitials(name: String): String {
    val clean = name.trim()
    if (clean.isEmpty()) return "?"
    val parts = clean.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> "?"
    }
}
