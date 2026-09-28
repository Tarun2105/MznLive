package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TricolorSaffron,
    onPrimary = Color.White,
    primaryContainer = TricolorSaffronLight,
    onPrimaryContainer = TricolorSaffronDark,
    secondary = TricolorGreen,
    onSecondary = Color.White,
    secondaryContainer = TricolorGreenLight,
    onSecondaryContainer = TricolorGreenDark,
    tertiary = TricolorNavy,
    onTertiary = Color.White,
    tertiaryContainer = TricolorNavyLight,
    onTertiaryContainer = TricolorNavy,
    background = MznLightBg,
    onBackground = MznLightTextPrimary,
    surface = MznLightSurface,
    onSurface = MznLightTextPrimary,
    surfaceVariant = MznLightSurfaceVariant,
    onSurfaceVariant = MznLightTextSecondary,
    outline = MznLightCardBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = TricolorSaffron,
    onPrimary = Color.White,
    primaryContainer = TricolorSaffronDark,
    onPrimaryContainer = TricolorSaffronLight,
    secondary = TricolorGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF14532D),
    onSecondaryContainer = Color(0xFFBBF7D0),
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF0F172A),
    background = MznDarkBg,
    onBackground = MznDarkTextPrimary,
    surface = MznDarkSurface,
    onSurface = MznDarkTextPrimary,
    surfaceVariant = MznDarkSurfaceVariant,
    onSurfaceVariant = MznDarkTextSecondary,
    outline = MznDarkCardBorder
)

@Composable
fun MznliveTheme(
    darkTheme: Boolean = false, // Default to light theme as requested: "light decent colores with tricolor combination"
    dynamicColor: Boolean = false, // Default false so the custom tricolor palette is consistently showcased
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
