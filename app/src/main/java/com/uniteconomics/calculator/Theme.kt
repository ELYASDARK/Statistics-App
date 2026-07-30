package com.uniteconomics.calculator

import android.app.Activity
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : Modifier.Node() {}
    }

    override fun hashCode(): Int = -1

    override fun equals(other: Any?): Boolean = other === this
}

/**
 * Composition local providing Neumorphic design system color tokens.
 */
val LocalNeumorphicColors = staticCompositionLocalOf { LightNeumorphicColors }

/**
 * Composition local providing extended semantic colors (success, warning, info, purple, etc.).
 */
val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = NeuLightSuccess,
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0x1F059669),
        onSuccessContainer = NeuLightSuccess,
        warning = NeuLightWarning,
        onWarning = Color(0xFFFFFFFF),
        warningContainer = Color(0x1FD97706),
        onWarningContainer = NeuLightWarning,
        info = NeuLightInfo,
        onInfo = Color(0xFFFFFFFF),
        purple = NeuLightPurple,
        onPurple = Color(0xFFFFFFFF),
        textMuted = NeuLightTextMuted,
        border = NeuLightBorder
    )
}

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

private val DarkColorScheme = darkColorScheme(
    primary = NeuDarkPrimary,
    onPrimary = Color(0xFF0A0A0F),
    primaryContainer = Color(0x2660A5FA),
    onPrimaryContainer = NeuDarkPrimary,
    secondary = NeuDarkInfo,
    onSecondary = Color(0xFF0A0A0F),
    tertiary = NeuDarkPurple,
    onTertiary = Color(0xFF0A0A0F),
    background = Color(0xFF171C21),
    onBackground = NeuDarkTextMain,
    surface = Color(0xFF171C21),
    onSurface = NeuDarkTextMain,
    surfaceVariant = Color(0xFF1E232B),
    onSurfaceVariant = NeuDarkTextMuted,
    error = NeuDarkLoss,
    onError = Color(0xFF0A0A0F),
    errorContainer = Color(0x26F87171),
    onErrorContainer = NeuDarkLoss
)

private val LightColorScheme = lightColorScheme(
    primary = NeuLightPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x1A2563EB),
    onPrimaryContainer = NeuLightPrimary,
    secondary = NeuLightInfo,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = NeuLightPurple,
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFE0E5EC),
    onBackground = NeuLightTextMain,
    surface = Color(0xFFE0E5EC),
    onSurface = NeuLightTextMain,
    surfaceVariant = Color(0xFFE4E9F0),
    onSurfaceVariant = NeuLightTextMuted,
    error = NeuLightLoss,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0x1ADC2626),
    onErrorContainer = NeuLightLoss
)

val MaterialTheme.neumorphicColors: NeumorphicColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNeumorphicColors.current

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val neumorphicColors = if (darkTheme) DarkNeumorphicColors else LightNeumorphicColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = NeuDarkSuccess,
            onSuccess = Color(0xFF0A0A0F),
            successContainer = Color(0x1F34D399),
            onSuccessContainer = NeuDarkSuccess,
            warning = NeuDarkWarning,
            onWarning = Color(0xFF0A0A0F),
            warningContainer = Color(0x1FFBBF24),
            onWarningContainer = NeuDarkWarning,
            info = NeuDarkInfo,
            onInfo = Color(0xFF0A0A0F),
            purple = NeuDarkPurple,
            onPurple = Color(0xFF0A0A0F),
            textMuted = NeuDarkTextMuted,
            border = NeuDarkBorder
        )
    } else {
        ExtendedColors(
            success = NeuLightSuccess,
            onSuccess = Color(0xFFFFFFFF),
            successContainer = Color(0x14059669),
            onSuccessContainer = NeuLightSuccess,
            warning = NeuLightWarning,
            onWarning = Color(0xFFFFFFFF),
            warningContainer = Color(0x14D97706),
            onWarningContainer = NeuLightWarning,
            info = NeuLightInfo,
            onInfo = Color(0xFFFFFFFF),
            purple = NeuLightPurple,
            onPurple = Color(0xFFFFFFFF),
            textMuted = NeuLightTextMuted,
            border = NeuLightBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = if (darkTheme) Color(0xFF171C21) else Color(0xFFE0E5EC)
            @Suppress("DEPRECATION")
            window.statusBarColor = statusBarColor.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = statusBarColor.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalNeumorphicColors provides neumorphicColors,
        LocalExtendedColors provides extendedColors,
        LocalIndication provides NoIndication
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
