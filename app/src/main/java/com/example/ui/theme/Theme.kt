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

private val DarkColorScheme = darkColorScheme(
    primary = MediTealDarkPrimary,
    onPrimary = Color(0xFF003831),
    primaryContainer = MediTealDarkContainer,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = MediSkyDarkSecondary,
    onSecondary = Color(0xFF003554),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = MediEmeraldDarkTertiary,
    onTertiary = Color(0xFF003920),
    background = MediBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = MediSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MediSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = MediOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = MediTealDark,
    onPrimary = Color.White,
    primaryContainer = MediTealContainer,
    onPrimaryContainer = MediOnTealContainer,
    secondary = MediSkySecondary,
    onSecondary = Color.White,
    secondaryContainer = MediSkyContainer,
    onSecondaryContainer = MediOnSkyContainer,
    tertiary = MediEmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = MediEmeraldContainer,
    onTertiaryContainer = MediOnEmeraldContainer,
    background = MediBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = MediSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MediSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = MediOutlineLight
)

@Composable
fun MediBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded MediBridge teal aesthetic by default
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MediBridgeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
