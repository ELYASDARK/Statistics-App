package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ChartMathTest {

    // --- ValuationLineChart Dynamic Multiplier Formula Tests ---
    private fun computeValuationMultiplier(points: List<Double>): Double {
        return if (points.isNotEmpty() && points.first() > 0) {
            points.last() / points.first()
        } else {
            1.0
        }
    }

    @Test
    fun valuationMultiplier_standardPoints_calculatesCorrectRatio() {
        val points = listOf(400.0, 650.0, 900.0, 1200.0, 1600.0, 2100.0)
        val mult = computeValuationMultiplier(points)
        assertEquals(5.25, mult, 0.001)
        val formatted = String.format(Locale.US, "%.1fx", mult)
        assertEquals("5.3x", formatted)
    }

    @Test
    fun valuationMultiplier_zeroFirstPoint_returnsFallbackMultiplierWithoutDivisionByZero() {
        val points = listOf(0.0, 100.0, 200.0)
        val mult = computeValuationMultiplier(points)
        assertEquals(1.0, mult, 0.0)
        assertFalse(mult.isInfinite())
        assertFalse(mult.isNaN())
    }

    @Test
    fun valuationMultiplier_negativeFirstPoint_returnsFallbackMultiplier() {
        val points = listOf(-50.0, 0.0, 100.0)
        val mult = computeValuationMultiplier(points)
        assertEquals(1.0, mult, 0.0)
    }

    @Test
    fun valuationMultiplier_singlePointData_returnsOne() {
        val points = listOf(150.0)
        val mult = computeValuationMultiplier(points)
        assertEquals(1.0, mult, 0.0)
    }

    @Test
    fun valuationMultiplier_fractionalAndEdgeCaseValues_handlesCorrectly() {
        val pointsSmall = listOf(0.0001, 0.0005)
        assertEquals(5.0, computeValuationMultiplier(pointsSmall), 0.0001)

        val pointsLarge = listOf(1e9, 2.5e9)
        assertEquals(2.5, computeValuationMultiplier(pointsLarge), 0.0001)
    }

    @Test
    fun valuationMultiplier_integrationWithCalculationResult_doesNotCrash() {
        val resultZero = CalculatorLogic.runFinancialModel(CalculatorInputs(cac = 50000.0))
        val baseRev = kotlin.math.max(100.0, resultZero.revenue)
        val baseProfit = kotlin.math.max(0.0, resultZero.netProfitTotal)
        val monthlyPoints = listOf(
            0.4 * baseRev + 0.3 * baseProfit,
            0.65 * baseRev + 0.5 * baseProfit,
            0.9 * baseRev + 0.85 * baseProfit,
            1.2 * baseRev + 1.15 * baseProfit,
            1.6 * baseRev + 1.55 * baseProfit,
            2.1 * baseRev + 2.0 * baseProfit
        )
        val mult = computeValuationMultiplier(monthlyPoints)
        assertTrue(mult > 0)
        assertFalse(mult.isInfinite())
        assertFalse(mult.isNaN())
    }

    // --- RevenueGrowthBarChart Dynamic Growth Formula Tests ---
    private fun computeGrowthPercent(bars: List<Double>): Double {
        return if (bars.isNotEmpty() && bars.first() > 0) {
            ((bars.last() - bars.first()) / bars.first()) * 100
        } else {
            0.0
        }
    }

    @Test
    fun growthPercent_standardBars_calculatesCorrectPercentage() {
        val bars = listOf(45.0, 65.0, 85.0, 100.0, 130.0)
        val growth = computeGrowthPercent(bars)
        val expected = ((130.0 - 45.0) / 45.0) * 100
        assertEquals(expected, growth, 0.001)

        val growthStr = String.format(Locale.US, "+%.0f%%", growth)
        assertEquals("+189%", growthStr)
    }

    @Test
    fun growthPercent_zeroStartingValue_returnsZeroWithoutDivisionByZero() {
        val bars = listOf(0.0, 50.0, 100.0)
        val growth = computeGrowthPercent(bars)
        assertEquals(0.0, growth, 0.0)
        assertFalse(growth.isInfinite())
        assertFalse(growth.isNaN())
    }

    @Test
    fun growthPercent_negativeStartingValue_returnsZeroWithoutDivisionByZero() {
        val bars = listOf(-100.0, -50.0, 0.0)
        val growth = computeGrowthPercent(bars)
        assertEquals(0.0, growth, 0.0)
    }

    @Test
    fun growthPercent_negativeMarginOrDecline_handlesNegativeGrowth() {
        val bars = listOf(100.0, 80.0, 50.0)
        val growth = computeGrowthPercent(bars)
        assertEquals(-50.0, growth, 0.001)
    }

    @Test
    fun growthPercent_fractionalInputs_calculatesAccurately() {
        val bars = listOf(0.1234, 0.1851, 0.2468)
        val growth = computeGrowthPercent(bars)
        assertEquals(100.0, growth, 0.01)
    }

    @Test
    fun growthPercent_integrationWithCalculationResult_doesNotCrash() {
        val resultZero = CalculatorLogic.runFinancialModel(CalculatorInputs(cac = 50000.0))
        val rev = kotlin.math.max(100.0, resultZero.revenue)
        val barValues = listOf(
            0.45 * rev,
            0.65 * rev,
            0.85 * rev,
            1.0 * rev,
            1.3 * rev
        )
        val growth = computeGrowthPercent(barValues)
        assertTrue(growth > 0)
        assertFalse(growth.isInfinite())
        assertFalse(growth.isNaN())
    }

    // --- Multi-Timeframe (Daily, Weekly, Monthly) Dataset Tests ---
    @Test
    fun multiTimeframe_dailyWeeklyMonthlyPoints_profitAndLoss() {
        val profitResult = CalculatorLogic.runFinancialModel(CalculatorInputs())
        assertFalse(profitResult.isLoss)

        // Profit - Daily has 7 points
        val dailyRev = profitResult.revenue / 30.0
        val dailyProfit = profitResult.netProfitTotal / 30.0
        assertTrue(dailyRev > 0)
        assertTrue(dailyProfit > 0)

        // Loss - Weekly has 4 points
        val lossResult = CalculatorLogic.runFinancialModel(CalculatorInputs(cac = 50000.0))
        assertTrue(lossResult.isLoss)
        val weeklyBurn = lossResult.burnRateWeek
        assertEquals(105000.0, weeklyBurn, 0.01)
        val dailyBurn = lossResult.dailyCashBurn
        assertEquals(15000.0, dailyBurn, 0.01)
    }
}
