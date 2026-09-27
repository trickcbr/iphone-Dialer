package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DIAL_PAD_KEYS
import com.example.model.DialKey
import com.example.ui.theme.DarkKeypadButton
import com.example.ui.theme.DarkKeypadButtonPressed
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightKeypadButton
import com.example.ui.theme.LightKeypadButtonPressed
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary

@Composable
fun DialKeypad(
    modifier: Modifier = Modifier,
    buttonSize: Dp = 76.dp,
    spacing: Dp = 16.dp,
    onDigitClick: (String) -> Unit,
    onLongPressZero: () -> Unit = {}
) {
    val keys = DIAL_PAD_KEYS
    val rows = keys.chunked(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { rowKeys ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowKeys.forEach { key ->
                    KeypadButton(
                        key = key,
                        size = buttonSize,
                        onClick = { onDigitClick(key.digit) },
                        onLongClick = {
                            if (key.digit == "0") {
                                onLongPressZero()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadButton(
    key: DialKey,
    size: Dp = 76.dp,
    isDark: Boolean = isSystemInDarkTheme(),
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        label = "keypad_button_scale"
    )

    val normalBg = if (isDark) DarkKeypadButton else LightKeypadButton
    val pressedBg = if (isDark) DarkKeypadButtonPressed else LightKeypadButtonPressed
    val bgColor = if (isPressed) pressedBg else normalBg

    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val subtextColor = if (isDark) DarkTextSecondary else LightTextSecondary

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("keypad_button_${key.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val isSymbol = key.digit == "*" || key.digit == "#"
            Text(
                text = key.digit,
                fontSize = if (isSymbol) 34.sp else 30.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                lineHeight = 32.sp
            )
            if (key.letters.isNotEmpty()) {
                Text(
                    text = key.letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtextColor,
                    letterSpacing = 1.2.sp
                )
            } else if (key.digit == "0") {
                Text(
                    text = "+",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = subtextColor
                )
            } else if (!isSymbol) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
