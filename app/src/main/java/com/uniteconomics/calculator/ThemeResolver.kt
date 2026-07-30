package com.uniteconomics.calculator

object ThemeResolver {
    fun getStatusBarColor(theme: String): String {
        return if (theme == "light") {
            "#f5f5f7"
        } else {
            "#0a0a0f"
        }
    }
}
