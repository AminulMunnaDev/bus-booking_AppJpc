package com.example.bus_booking.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val BusGoLightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Slate600,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = Amber500,
    onTertiary = Slate900,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber700,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Red600,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red700
)

private val BusGoDarkColorScheme = darkColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Slate500,
    onSecondary = Color.White,
    secondaryContainer = Slate600,
    onSecondaryContainer = Slate100,
    tertiary = Amber500,
    onTertiary = Slate900,
    tertiaryContainer = Amber700,
    onTertiaryContainer = Amber100,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate600,
    onSurfaceVariant = Slate200,
    outline = Slate500,
    outlineVariant = Slate600,
    error = Red600,
    onError = Color.White,
    errorContainer = Red700,
    onErrorContainer = Red100
)

private val BusGoTextSelectionColors = TextSelectionColors(
    handleColor = Green600,
    backgroundColor = Green100
)

@Composable
fun BusbookingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> BusGoDarkColorScheme
        else -> BusGoLightColorScheme
    }

    CompositionLocalProvider(LocalTextSelectionColors provides BusGoTextSelectionColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}