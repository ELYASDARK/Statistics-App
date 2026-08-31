package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class InputNormalizationTest {

    @Test
    fun testEasternArabicDigitsNormalization() {
        val easternArabic = "٣٥٠٠٠"
        val normalized = normalizeNumericInput(easternArabic)
        assertEquals("35000", normalized)
        assertEquals(35000.0, normalized.toDouble(), 0.001)
    }

    @Test
    fun testPersianDigitsNormalization() {
        val persian = "۱۲۵۰۰"
        val normalized = normalizeNumericInput(persian)
        assertEquals("12500", normalized)
        assertEquals(12500.0, normalized.toDouble(), 0.001)
    }

    @Test
    fun testArabicDecimalCommaNormalization() {
        val withDecimal = "٣٥٠٠٠٫٥"
        val normalized = normalizeNumericInput(withDecimal)
        assertEquals("35000.5", normalized)
        assertEquals(35000.5, normalized.toDouble(), 0.001)
    }

    @Test
    fun testMixedCommasAndThousandSeparators() {
        val input = "١،٢٥٠٬٠٠٠٫٥٠"
        val normalized = normalizeNumericInput(input)
        assertEquals("1250000.50", normalized)
        assertEquals(1250000.5, normalized.toDouble(), 0.001)
    }

    @Test
    fun testStandardAsciiInputWithCommas() {
        val input = "35,000.50"
        val normalized = normalizeNumericInput(input)
        assertEquals("35000.50", normalized)
        assertEquals(35000.5, normalized.toDouble(), 0.001)
    }

    @Test
    fun testKurdishSearchNormalization() {
        assertEquals("ریکخستن", normalizeKurdishSearch("ڕێکخستن"))
        assertEquals("کالاکان", normalizeKurdishSearch("كالاكان"))
        assertEquals("کولان", normalizeKurdishSearch("کۆڵان"))
        assertEquals("ااسان", normalizeKurdishSearch("ئاـسان"))
        assertEquals(normalizeKurdishSearch("ئاسان"), normalizeKurdishSearch("ئاـسان"))
    }

    @Test
    fun testFormatKurdishDigits() {
        assertEquals("١٢٣٤٥", formatKurdishDigits(12345))
        assertEquals("٠", formatKurdishDigits(0))
        assertEquals("٩٨٧٦٥٤٣٢١", formatKurdishDigits(987654321))
    }

    @Test
    fun testTrojanSourceAndBidiStrippingInSearch() {
        // Text contaminated with Trojan Source and BiDi controls: \u202E, \u2066, \u200B
        val contaminated = "ڕێك\u202Eخست\u2066ن\u200B"
        assertEquals("ریکخستن", normalizeKurdishSearch(contaminated))
    }

    @Test
    fun testArabicCommaAsThousandSeparatorInNumericInput() {
        // Arabic comma '،' (\u060C) used as thousands separator
        val inputWithArabicComma = "٣٥،٠٠٠"
        val normalized = normalizeNumericInput(inputWithArabicComma)
        assertEquals("35000", normalized)
        assertEquals(35000.0, normalized.toDouble(), 0.001)
    }

    @Test
    fun testFormatBidiCurrency() {
        val positive = formatBidiCurrency("35,000", "IQD", isNegative = false)
        assertEquals("\u206635,000 IQD\u2069", positive)

        val negative = formatBidiCurrency("5,000", "IQD", isNegative = true)
        assertEquals("\u2066-5,000 IQD\u2069", negative)
    }
}
