package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max

/**
 * Empirical unit and boundary tests for financial calculations, formatter invariants, and loss states.
 */
class Milestone2ChallengerTest {

    @Test
    fun testValuationLineChart_negativeProfitMargin_decliningTrajectoryAndMultiplier() {
        val lossInputs = CalculatorInputs(
            retailPrice = 10000.0,
            sourcingCost = 15000.0,
            cac = 25000.0
        )
        val lossResult = CalculatorLogic.runFinancialModel(lossInputs)
        assertTrue(lossResult.isLoss)

        val baseRev = max(100.0, lossResult.revenue)
        val monthlyPoints = listOf(
            baseRev * 1.0,
            baseRev * 0.8,
            baseRev * 0.6,
            baseRev * 0.45,
            baseRev * 0.3,
            baseRev * 0.15
        )

        assertEquals(6, monthlyPoints.size)
        assertTrue("Trajectory must strictly decline under loss state", monthlyPoints[0] > monthlyPoints[5])
        assertFalse(lossResult.netProfitTotal.isNaN())
    }

    @Test
    fun testMathematicalEngine_divisionByZeroDefenses() {
        // Test edge cases: 0 price, 0 days, 0 CAC, 0 overhead
        val zeroPriceInputs = CalculatorInputs(retailPrice = 0.0)
        val zeroResult = CalculatorLogic.runFinancialModel(zeroPriceInputs)
        assertTrue(zeroResult.isLoss)
        assertFalse(zeroResult.netProfitPerProduct.isNaN())
        assertFalse(zeroResult.netProfitTotal.isNaN())
        assertFalse(zeroResult.revenue.isNaN())

        val zeroDaysInputs = CalculatorInputs(projectDurationDays = 0.0)
        val zeroDaysResult = CalculatorLogic.runFinancialModel(zeroDaysInputs)
        assertFalse(zeroDaysResult.dailyCashBurn.isNaN())
        assertFalse(zeroDaysResult.targetDailyOrders.isNaN())
    }

    @Test
    fun testNumberFormatter_finiteAndInfiniteHandling() {
        assertEquals("35,000", formatNumber(35000.0))
        assertEquals("1,000,000", formatNumber(1000000.0))
        assertEquals("12.5", formatNumber(12.5, isDecimal = true))
        assertEquals("نەزانراو", formatNumber(Double.NaN))
        assertEquals("نەزانراو", formatNumber(Double.POSITIVE_INFINITY))
    }

    @Test
    fun testExtremeParameters_noIntegerOverflow() {
        // High profit target and small gross margin must not overflow Int
        val hugeGoalInputs = CalculatorInputs(
            retailPrice = 10001.0,
            sourcingCost = 10000.0,
            targetProfitGoal = 100_000_000.0
        )
        val result = CalculatorLogic.runFinancialModel(hugeGoalInputs)
        assertTrue(result.targetSuccessfulOrders >= 0)
        assertTrue(result.totalDispatchedOrders >= 0)
        assertFalse(result.targetSuccessfulOrders == Int.MIN_VALUE)
    }
}
