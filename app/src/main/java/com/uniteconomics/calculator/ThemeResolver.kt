package com.uniteconomics.calculator

const val NeuLightBgHex = "#E0E5EC"
const val NeuDarkBgHex = "#171C21"

/**
 * Utility for resolving hex color codes and status bar background colors
 * based on active theme configuration.
 */
object ThemeResolver {
    fun getStatusBarColor(theme: String): String {
        return if (theme.equals("light", ignoreCase = true)) {
            NeuLightBgHex
        } else {
            NeuDarkBgHex
        }
    }
}
