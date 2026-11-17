package com.example.nuevaapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val InduztekColorScheme = darkColorScheme(
    primary = InduztekPrimary,
    onPrimary = InduztekBackground,
    secondary = InduztekSecondary,
    onSecondary = InduztekBackground,
    background = InduztekBackground,
    onBackground = InduztekTextLight,
    surface = InduztekSurface,
    onSurface = InduztekTextLight,
    surfaceVariant = InduztekSurfaceVariant,
    onSurfaceVariant = InduztekTextMuted,
    error = InduztekError,
    onError = InduztekTextLight
)

@Composable
fun NuevaAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> InduztekColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
