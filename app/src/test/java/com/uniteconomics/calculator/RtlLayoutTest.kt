package com.uniteconomics.calculator

import org.junit.Assert.*
import org.junit.Test

class RtlLayoutTest {

    enum class TestLayoutDirection { Ltr, Rtl }

    @Test
    fun testBarChartRtlOrderingLogic() {
        val barValues = listOf(100.0, 200.0, 300.0, 400.0, 500.0) // M1 to M5
        val isKurdish = true

        val displayBarValues = if (isKurdish) barValues.reversed() else barValues
        val displayBarLabels = if (isKurdish) {
            listOf("مانگی ٥", "مانگی ٤", "مانگی ٣", "مانگی ٢", "مانگی ١")
        } else {
            listOf("M1", "M2", "M3", "M4", "M5")
        }

        // Canvas draws displayBarValues from physical left index 0 to physical right index 4:
        // Physical index 0 (LEFT of Canvas) = displayBarValues[0] = 500.0 (Month 5)
        // Physical index 4 (RIGHT of Canvas) = displayBarValues[4] = 100.0 (Month 1)
        assertEquals(500.0, displayBarValues[0], 0.001) // Month 5 value at canvas LEFT
        assertEquals(100.0, displayBarValues[4], 0.001) // Month 1 value at canvas RIGHT

        // Compose Row under LayoutDirection.Rtl renders children starting at PHYSICAL RIGHT:
        // displayBarLabels[0] = "مانگی ٥" -> placed at PHYSICAL RIGHT by Row in RTL
        // displayBarLabels[4] = "مانگی ١" -> placed at PHYSICAL LEFT by Row in RTL
        val rowPhysicalRightLabel = displayBarLabels[0]
        val rowPhysicalLeftLabel = displayBarLabels[4]

        assertEquals("مانگی ٥", rowPhysicalRightLabel)
        assertEquals("مانگی ١", rowPhysicalLeftLabel)

        // Verify mismatch:
        assertFalse(rowPhysicalRightLabel == "مانگی ١")
        assertFalse(rowPhysicalLeftLabel == "مانگی ٥")
    }

    @Test
    fun testSliderTrackFillDirectionLogic() {
        val width = 100f
        val fraction = 0.3f
        val activeWidth = width * fraction

        val isRtlLtr = false
        val ltrTopLeftX = if (isRtlLtr) width - activeWidth else 0f
        assertEquals(0f, ltrTopLeftX, 0.001f)

        val isRtlKurdish = true
        val rtlTopLeftX = if (isRtlKurdish) width - activeWidth else 0f
        assertEquals(70f, rtlTopLeftX, 0.001f)
    }

    @Test
    fun testDictionaryDialogLayoutDirectionResolution() {
        val isKurdishTrue = true
        val isKurdishFalse = false

        val layoutDirKurdish = if (isKurdishTrue) TestLayoutDirection.Rtl else TestLayoutDirection.Ltr
        val layoutDirEnglish = if (isKurdishFalse) TestLayoutDirection.Rtl else TestLayoutDirection.Ltr

        assertEquals(TestLayoutDirection.Rtl, layoutDirKurdish)
        assertEquals(TestLayoutDirection.Ltr, layoutDirEnglish)
    }
}
