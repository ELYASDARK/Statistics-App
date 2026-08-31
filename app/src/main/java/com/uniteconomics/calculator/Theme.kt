package com.uniteconomics.calculator

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.LocalIndication
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
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * Design system spacing tokens.
 */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 40.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

/**
 * Material 3 WindowWidthSizeClass representation for adaptivity.
 */
enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromWidth(width: Dp): WindowWidthSizeClass = when {
            width < 600.dp -> Compact
            width < 840.dp -> Medium
            else -> Expanded
        }
    }
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
        onSuccess = NeuLightOnPrimary,
        successContainer = NeuLightSuccessContainer,
        onSuccessContainer = NeuLightSuccess,
        warning = NeuLightWarning,
        onWarning = NeuLightOnPrimary,
        warningContainer = NeuLightWarningContainer,
        onWarningContainer = NeuLightWarning,
        info = NeuLightInfo,
        onInfo = NeuLightOnPrimary,
        purple = NeuLightPurple,
        onPurple = NeuLightOnPrimary,
        textMuted = NeuLightTextMuted,
        border = NeuLightBorder
    )
}

@Immutable
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
    onPrimary = NeuDarkOnPrimary,
    primaryContainer = NeuDarkPrimaryContainer,
    onPrimaryContainer = NeuDarkPrimary,
    secondary = NeuDarkInfo,
    onSecondary = NeuDarkOnPrimary,
    tertiary = NeuDarkPurple,
    onTertiary = NeuDarkOnPrimary,
    background = NeuDarkBg,
    onBackground = NeuDarkTextMain,
    surface = NeuDarkBg,
    onSurface = NeuDarkTextMain,
    surfaceVariant = NeuDarkSurfaceVariant,
    onSurfaceVariant = NeuDarkTextMuted,
    error = NeuDarkLoss,
    onError = NeuDarkOnPrimary,
    errorContainer = NeuDarkErrorContainer,
    onErrorContainer = NeuDarkLoss
)

private val LightColorScheme = lightColorScheme(
    primary = NeuLightPrimary,
    onPrimary = NeuLightOnPrimary,
    primaryContainer = NeuLightPrimaryContainer,
    onPrimaryContainer = NeuLightPrimary,
    secondary = NeuLightInfo,
    onSecondary = NeuLightOnPrimary,
    tertiary = NeuLightPurple,
    onTertiary = NeuLightOnPrimary,
    background = NeuLightBg,
    onBackground = NeuLightTextMain,
    surface = NeuLightBg,
    onSurface = NeuLightTextMain,
    surfaceVariant = NeuLightSurfaceVariant,
    onSurfaceVariant = NeuLightTextMuted,
    error = NeuLightLoss,
    onError = NeuLightOnPrimary,
    errorContainer = NeuLightErrorContainer,
    onErrorContainer = NeuLightLoss
)

val AppTypography = androidx.compose.material3.Typography(
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 2.0.sp
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 2.0.sp
    )
)

val MaterialTheme.neumorphicColors: NeumorphicColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNeumorphicColors.current

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val neumorphicColors = if (darkTheme) DarkNeumorphicColors else LightNeumorphicColors
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = NeuDarkSuccess,
            onSuccess = NeuDarkOnPrimary,
            successContainer = NeuDarkSuccessContainer,
            onSuccessContainer = NeuDarkSuccess,
            warning = NeuDarkWarning,
            onWarning = NeuDarkOnPrimary,
            warningContainer = NeuDarkWarningContainer,
            onWarningContainer = NeuDarkWarning,
            info = NeuDarkInfo,
            onInfo = NeuDarkOnPrimary,
            purple = NeuDarkPurple,
            onPurple = NeuDarkOnPrimary,
            textMuted = NeuDarkTextMuted,
            border = NeuDarkBorder
        )
    } else {
        ExtendedColors(
            success = NeuLightSuccess,
            onSuccess = NeuLightOnPrimary,
            successContainer = NeuLightSuccessContainer,
            onSuccessContainer = NeuLightSuccess,
            warning = NeuLightWarning,
            onWarning = NeuLightOnPrimary,
            warningContainer = NeuLightWarningContainer,
            onWarningContainer = NeuLightWarning,
            info = NeuLightInfo,
            onInfo = NeuLightOnPrimary,
            purple = NeuLightPurple,
            onPurple = NeuLightOnPrimary,
            textMuted = NeuLightTextMuted,
            border = NeuLightBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalNeumorphicColors provides neumorphicColors,
        LocalExtendedColors provides extendedColors,
        LocalSpacing provides Spacing(),
        LocalIndication provides androidx.compose.material3.ripple(color = neumorphicColors.primary.copy(alpha = 0.12f))
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
