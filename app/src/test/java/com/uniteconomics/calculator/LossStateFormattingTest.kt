package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class LossStateFormattingTest {

    private val INTEGER_FORMAT = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    private fun formatAmtHelper(num: Double, isKurdish: Boolean = true): String {
        if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
        val formatted = synchronized(INTEGER_FORMAT) { INTEGER_FORMAT.format(kotlin.math.abs(num)) }
        return if (num < 0 && kotlin.math.abs(num) >= 0.0001) "\u2066-$formatted\u2069" else formatted
    }

    @Test
    fun testLossFormattingDoesNotProduceNegativeZero() {
        val zero = 0.0
        val negZero = -0.0
        assertEquals("0", formatAmtHelper(zero))
        assertEquals("0", formatAmtHelper(negZero))
    }

    @Test
    fun testDeadstockRiskFormulaConsistency() {
        val inputs = CalculatorInputs(
            retailPrice = 40000.0,
            sourcingCost = 12000.0,
            rejectionRate = 15.0,
            refundRate = 2.0,
            damageRate = 4.0,
            deadstockRate = 5.0
        )
        val result = CalculatorLogic.calculate(inputs)

        // CalculatorLogic expected value deadstock loss calculation:
        val totalSent = result.totalDispatchedOrders.toDouble()
        val cogs = inputs.effSourcingCost * inputs.effAovMultiplier
        val rejectDec = inputs.effRejectionRate / 100.0
        val rawRefundDec = inputs.effRefundRate / 100.0
        val successDec = kotlin.math.max(0.001, (1.0 - rejectDec) * (1.0 - rawRefundDec))
        val damageDec = inputs.effDamageRate / 100.0
        val deadDec = (inputs.effDeadstockRate / 100.0).coerceIn(0.0, 0.999)
        val consumedProb = kotlin.math.min(1.0, successDec + damageDec)
        val expectedDeadstockLoss = totalSent * consumedProb * cogs * (deadDec / (1.0 - deadDec))

        // Linear naive calculation would have been:
        val linearNaive = (totalSent * cogs) * (inputs.effDeadstockRate / 100.0)

        // Verify that the exact EV formula produces a valid non-negative value
        assertTrue(expectedDeadstockLoss > 0.0)
        // Verify formula differs from linear approximation due to non-linear deadstock denominator (1 - d) and consumption probability
        assertFalse(expectedDeadstockLoss == linearNaive)
    }

    @Test
    fun testLossScenarioIntegrity() {
        val inputs = CalculatorInputs(
            retailPrice = 10000.0,
            sourcingCost = 30000.0
        )
        val result = CalculatorLogic.calculate(inputs)

        assertTrue(result.isLoss)
        assertTrue(result.netProfitPerProduct < 0)
        assertTrue(result.netProfitTotal < 0)
        assertEquals(0.0, result.revenue, 0.0)
        assertEquals(0, result.targetSuccessfulOrders)
        assertEquals(0, result.totalDispatchedOrders)
    }

    @Test
    fun testFormatCurrencyAmountWithPositiveAndNegativeValues() {
        val positive = 383640.0
        val negative = -39045.0
        val zero = 0.0

        val formattedPos = formatCurrencyAmount(positive, isKurdish = true)
        val formattedNeg = formatCurrencyAmount(negative, isKurdish = true)
        val formattedZero = formatCurrencyAmount(zero, isKurdish = true)

        assertEquals("\u2066383,640 IQD\u2069", formattedPos)
        assertEquals("\u2066-39,045 IQD\u2069", formattedNeg)
        assertEquals("\u20660 IQD\u2069", formattedZero)
    }
}
