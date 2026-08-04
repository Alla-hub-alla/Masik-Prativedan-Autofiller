package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekContainerBlue,
    secondary = SleekAccentPurple,
    tertiary = SleekGreenBadge,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = SleekNavyDark,
    onSecondary = SleekPurpleDark,
    onBackground = SleekBackground,
    onSurface = SleekBackground,
    primaryContainer = SleekNavyDark,
    onPrimaryContainer = SleekContainerBlue
)

private val LightColorScheme = lightColorScheme(
    primary = SleekBluePrimary,
    secondary = SleekNavyDark,
    tertiary = SleekAccentPurple,
    background = SleekBackground,
    surface = SleekSurface,
    onPrimary = SleekSurface,
    onSecondary = SleekSurface,
    onBackground = SleekTextPrimary,
    onSurface = SleekTextPrimary,
    primaryContainer = SleekContainerBlue,
    onPrimaryContainer = SleekNavyDark,
    surfaceVariant = SleekContainerBlueSoft,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
