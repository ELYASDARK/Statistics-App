package com.uniteconomics.calculator

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Extended Colors data class to support success/warning/purple semantic roles matching the web CSS
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val purple: Color,
    val onPurple: Color,
    val textMuted: Color,
    val border: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = Color(0xFF4ADE80),
        onSuccess = Color(0xFF0A0A0F),
        successContainer = Color(0x404ADE80),
        onSuccessContainer = Color(0xFF4ADE80),
        warning = Color(0xFFF59E0B),
        onWarning = Color(0xFF0A0A0F),
        warningContainer = Color(0x26F59E0B),
        onWarningContainer = Color(0xFFF59E0B),
        info = Color(0xFF60A5FA),
        onInfo = Color(0xFF0A0A0F),
        purple = Color(0xFFC084FC),
        onPurple = Color(0xFF0A0A0F),
        textMuted = Color(0xFF8A8A9A),
        border = Color(0x10FFFFFF)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF0A0A0F),
    primaryContainer = Color(0x264ADE80),
    onPrimaryContainer = Color(0xFF4ADE80),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0A0A0F),
    tertiary = Color(0xFFC084FC),
    onTertiary = Color(0xFF0A0A0F),
    background = Color(0xFF0A0A0F),
    onBackground = Color(0xFFF0F0F5),
    surface = Color(0xFF1A1A24),
    onSurface = Color(0xFFF0F0F5),
    surfaceVariant = Color(0xFF22222E),
    onSurfaceVariant = Color(0xFF8A8A9A),
    error = Color(0xFFF87171),
    onError = Color(0xFF0A0A0F),
    errorContainer = Color(0x26F87171),
    onErrorContainer = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF16A34A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x1A16A34A),
    onPrimaryContainer = Color(0xFF16A34A),
    secondary = Color(0xFF2563EB),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF9333EA),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5f5f7),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF0F0F2),
    onSurfaceVariant = Color(0xFF6B6B80),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0x1ADC2626),
    onErrorContainer = Color(0xFFDC2626)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = Color(0xFF4ADE80),
            onSuccess = Color(0xFF0A0A0F),
            successContainer = Color(0x1F4ADE80),
            onSuccessContainer = Color(0xFF4ADE80),
            warning = Color(0xFFF59E0B),
            onWarning = Color(0xFF0A0A0F),
            warningContainer = Color(0x1FF59E0B),
            onWarningContainer = Color(0xFFF59E0B),
            info = Color(0xFF60A5FA),
            onInfo = Color(0xFF0A0A0F),
            purple = Color(0xFFC084FC),
            onPurple = Color(0xFF0A0A0F),
            textMuted = Color(0xFF8A8A9A),
            border = Color(0x10FFFFFF)
        )
    } else {
        ExtendedColors(
            success = Color(0xFF16A34A),
            onSuccess = Color(0xFFFFFFFF),
            successContainer = Color(0x1416A34A),
            onSuccessContainer = Color(0xFF16A34A),
            warning = Color(0xFFD97706),
            onWarning = Color(0xFFFFFFFF),
            warningContainer = Color(0x14D97706),
            onWarningContainer = Color(0xFFD97706),
            info = Color(0xFF2563EB),
            onInfo = Color(0xFFFFFFFF),
            purple = Color(0xFF9333EA),
            onPurple = Color(0xFFFFFFFF),
            textMuted = Color(0xFF6B6B80),
            border = Color(0x14000000)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = if (darkTheme) Color(0xFF0A0A0F) else Color(0xFFF5F5F7)
            @Suppress("DEPRECATION")
            window.statusBarColor = statusBarColor.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = statusBarColor.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
