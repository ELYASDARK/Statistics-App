package com.uniteconomics.calculator

import androidx.compose.ui.graphics.Color

/**
 * Tactile Neumorphic Color Tokens matching stitch_elegant_web_interface
 * (luminous_fintech/DESIGN.md & business_analysis_tactile_trends).
 */

// Light Theme Color Tokens
val NeuLightBg = Color(0xFFE0E5EC)
val NeuLightSurface = Color(0xFFE0E5EC)
val NeuLightShadowDark = Color(0xFFA3B1C6).copy(alpha = 0.45f)
val NeuLightShadowLight = Color(0xFFFFFFFF).copy(alpha = 0.15f)
val NeuLightPrimary = Color(0xFF2563EB)
val NeuLightSuccess = Color(0xFF059669)
val NeuLightWarning = Color(0xFFD97706)
val NeuLightInfo = Color(0xFF0EA5E9)
val NeuLightPurple = Color(0xFF7C3AED)
val NeuLightLoss = Color(0xFFDC2626)
val NeuLightTextMain = Color(0xFF171C21)
val NeuLightTextMuted = Color(0xFF718096)
val NeuLightBorder = Color(0x20A3B1C6)
val NeuLightCardBg = Color(0xFFE0E5EC)

// Dark Theme Color Tokens
val NeuDarkBg = Color(0xFF171C21)
val NeuDarkSurface = Color(0xFF1E232B)
val NeuDarkShadowDark = Color(0xFF0C0F13)
val NeuDarkShadowLight = Color(0xFF242B35)
val NeuDarkPrimary = Color(0xFF60A5FA)
val NeuDarkSuccess = Color(0xFF34D399)
val NeuDarkWarning = Color(0xFFFBBF24)
val NeuDarkInfo = Color(0xFF38BDF8)
val NeuDarkPurple = Color(0xFFA78BFA)
val NeuDarkLoss = Color(0xFFF87171)
val NeuDarkTextMain = Color(0xFFECF1F8)
val NeuDarkTextMuted = Color(0xFF94A3B8)
val NeuDarkBorder = Color(0x20242B35)
val NeuDarkCardBg = Color(0xFF1E232B)

/**
 * Data class encapsulating complete Neumorphic theme palette.
 */
data class NeumorphicColors(
    val isDark: Boolean = false,
    val background: Color,
    val surface: Color,
    val shadowDark: Color,
    val shadowLight: Color,
    val primary: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val purple: Color,
    val loss: Color,
    val textMain: Color,
    val textMuted: Color,
    val border: Color,
    val cardBg: Color
)

val LightNeumorphicColors = NeumorphicColors(
    isDark = false,
    background = NeuLightBg,
    surface = NeuLightSurface,
    shadowDark = NeuLightShadowDark,
    shadowLight = NeuLightShadowLight,
    primary = NeuLightPrimary,
    success = NeuLightSuccess,
    warning = NeuLightWarning,
    info = NeuLightInfo,
    purple = NeuLightPurple,
    loss = NeuLightLoss,
    textMain = NeuLightTextMain,
    textMuted = NeuLightTextMuted,
    border = NeuLightBorder,
    cardBg = NeuLightCardBg
)

val DarkNeumorphicColors = NeumorphicColors(
    isDark = true,
    background = NeuDarkBg,
    surface = NeuDarkSurface,
    shadowDark = NeuDarkShadowDark,
    shadowLight = NeuDarkShadowLight,
    primary = NeuDarkPrimary,
    success = NeuDarkSuccess,
    warning = NeuDarkWarning,
    info = NeuDarkInfo,
    purple = NeuDarkPurple,
    loss = NeuDarkLoss,
    textMain = NeuDarkTextMain,
    textMuted = NeuDarkTextMuted,
    border = NeuDarkBorder,
    cardBg = NeuDarkCardBg
)
