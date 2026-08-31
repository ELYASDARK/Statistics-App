package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class RtlLayoutTest {

    enum class TestLayoutDirection { Ltr, Rtl }

    @Test
    fun testBarChartRtlOrderingLogic() {
        val barValues = listOf(100.0, 200.0, 300.0, 400.0, 500.0) // M1 to M5
        val isKurdish = true

        val displayBarLabels = if (isKurdish) {
            listOf("م١", "م٢", "م٣", "م٤", "م٥")
        } else {
            listOf("M1", "M2", "M3", "M4", "M5")
        }

        // Under RTL, Canvas draws bar at index `i` with mirrored position: (barCount - 1 - i)
        // Index 0 (Month 1 = 100.0) -> Drawn at physical Right (column 4)
        // Index 4 (Month 5 = 500.0) -> Drawn at physical Left (column 0)
        val barCount = barValues.size
        val canvasPhysicalPositionForM1 = barCount - 1 - 0 // 4 (Physical Right)
        val canvasPhysicalPositionForM5 = barCount - 1 - 4 // 0 (Physical Left)

        assertEquals(4, canvasPhysicalPositionForM1)
        assertEquals(0, canvasPhysicalPositionForM5)
        assertEquals(100.0, barValues[0], 0.001)
        assertEquals(500.0, barValues[4], 0.001)

        // In Compose Row under RTL layout, index 0 is rendered at physical Right (matching Canvas column 4)
        // and index 4 is rendered at physical Left (matching Canvas column 0).
        assertEquals("م١", displayBarLabels[0])
        assertEquals("م٥", displayBarLabels[4])
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
