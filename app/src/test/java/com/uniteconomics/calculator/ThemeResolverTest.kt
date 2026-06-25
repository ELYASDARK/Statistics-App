package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ThemeResolverTest(
    private val theme: String,
    private val expectedColor: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "theme: {0} -> expectedColor: {1}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("light", "#f5f5f7"),
                arrayOf("dark", "#0a0a0f"),
                arrayOf("unknown_theme", "#0a0a0f")
            )
        }
    }

    @Test
    fun getStatusBarColor_resolvesThemeToCorrectColor() {
        val color = ThemeResolver.getStatusBarColor(theme)
        assertEquals(expectedColor, color)
    }
}
