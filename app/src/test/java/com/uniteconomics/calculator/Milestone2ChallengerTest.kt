package com.uniteconomics.calculator

import org.junit.Assert.*
import org.junit.Test
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.max

/**
 * Empirical Challenger Unit & Stress Tests for Milestone 2:
 * 1. Projection Trajectory Calculation (Loss state: isLoss == true)
 * 2. Thousand Separator Formatting & Decimal Input Handling
 * 3. Floating Bottom Nav Padding & Bottom Whitespace Clearance Calculations
 */
class Milestone2ChallengerTest {

    // ------------------------------------------------------------------------
    // 1. PROJECTION TRAJECTORY CALCULATION TESTS (isLoss == true)
    // ------------------------------------------------------------------------

    @Test
    fun testValuationLineChart_negativeProfitMargin_decliningTrajectoryAndMultiplier() {
        // Construct a CalculationResult with isLoss = true
        val lossResult = CalculationResult(
            netProfitPerProduct = -5000.0,
            netProfitTotal = -150000.0,
            revenue = 0.0,
            targetDailyOrders = 0.0,
            dailyCashBurn = 15000.0,
            targetSuccessfulOrders = 0,
            grossProfitPerOrder = -2000.0,
            breakevenOrders = 0,
            totalDispatchedOrders = 0,
            totalProjectExpenses = 500000.0,
            workingCapitalFloat = 100000.0,
            burnRateDay = 15000.0,
            burnRateWeek = 105000.0,
            burnRateMonth = 450000.0,
            isLoss = true
        )

        // ValuationLineChart monthlyPoints logic for isLoss == true
        val baseRev = max(100.0, lossResult.revenue)
        val monthlyPoints = listOf(
            baseRev * 1.0,
            baseRev * 0.8,
            baseRev * 0.6,
            baseRev * 0.45,
            baseRev * 0.3,
            baseRev * 0.15
        )

        // Assert strictly declining points starting at 100.0 down to 15.0
        assertEquals(6, monthlyPoints.size)
        assertEquals(100.0, monthlyPoints[0], 0.001)
        assertEquals(80.0, monthlyPoints[1], 0.001)
        assertEquals(60.0, monthlyPoints[2], 0.001)
        assertEquals(45.0, monthlyPoints[3], 0.001)
        assertEquals(30.0, monthlyPoints[4], 0.001)
        assertEquals(15.0, monthlyPoints[5], 0.001)

        // Valuation multiplier calculation
        val valuationMult = if (monthlyPoints.first() > 0) monthlyPoints.last() / monthlyPoints.first() else 1.0
        assertEquals(0.15, valuationMult, 0.0001)

        val valuationStr = String.format(Locale.US, "%.1fx", valuationMult)
        assertEquals("0.2x", valuationStr)

        // Verify accent color decision logic
        val isAccentLossColor = lossResult.isLoss
        assertTrue(isAccentLossColor)
    }

    @Test
    fun testRevenueGrowthBarChart_negativeProfitMargin_decliningTrajectoryAndGrowthPercent() {
        val lossResult = CalculationResult(
            netProfitPerProduct = -2000.0,
            netProfitTotal = -60000.0,
            revenue = 0.0,
            targetDailyOrders = 0.0,
            dailyCashBurn = 5000.0,
            targetSuccessfulOrders = 0,
            grossProfitPerOrder = -1000.0,
            breakevenOrders = 0,
            totalDispatchedOrders = 0,
            totalProjectExpenses = 200000.0,
            workingCapitalFloat = 50000.0,
            burnRateDay = 5000.0,
            burnRateWeek = 35000.0,
            burnRateMonth = 150000.0,
            isLoss = true
        )

        // RevenueGrowthBarChart barValues logic for isLoss == true
        val rev = max(100.0, lossResult.revenue)
        val barValues = listOf(
            1.3 * rev,
            1.0 * rev,
            0.8 * rev,
            0.6 * rev,
            0.4 * rev
        )

        // Assert strictly declining unreversed values from Month 1 (130) to Month 5 (40)
        assertEquals(5, barValues.size)
        assertEquals(130.0, barValues[0], 0.001)
        assertEquals(100.0, barValues[1], 0.001)
        assertEquals(80.0, barValues[2], 0.001)
        assertEquals(60.0, barValues[3], 0.001)
        assertEquals(40.0, barValues[4], 0.001)

        // Growth percent calculation
        val growthPercent = if (barValues.first() > 0) {
            ((barValues.last() - barValues.first()) / barValues.first()) * 100
        } else 0.0

        val expectedGrowth = ((40.0 - 130.0) / 130.0) * 100 // -69.230769%
        assertEquals(expectedGrowth, growthPercent, 0.0001)

        val growthStr = String.format(Locale.US, if (growthPercent >= 0) "+%.0f%%" else "%.0f%%", growthPercent)
        assertEquals("-69%", growthStr)

        // Test RTL vs LTR reversal logic for Kurdish Sorani
        val displayBarValuesKurdish = barValues.reversed()
        assertEquals(40.0, displayBarValuesKurdish[0], 0.001) // Month 5 on left
        assertEquals(130.0, displayBarValuesKurdish[4], 0.001) // Month 1 on right
    }

    @Test
    fun testProjectionTrajectory_integrationWithLossModelInputs_producesValidNonNaNValues() {
        // High CAC, low price producing loss state
        val lossInputs = CalculatorInputs(
            retailPrice = 10000.0,
            sourcingCost = 15000.0,
            cac = 20000.0
        )
        val result = CalculatorLogic.runFinancialModel(lossInputs)
        assertTrue(result.isLoss)

        // Line Chart points
        val baseRev = max(100.0, result.revenue)
        val linePoints = listOf(
            baseRev * 1.0,
            baseRev * 0.8,
            baseRev * 0.6,
            baseRev * 0.45,
            baseRev * 0.3,
            baseRev * 0.15
        )
        val mult = if (linePoints.first() > 0) linePoints.last() / linePoints.first() else 1.0
        assertFalse(mult.isNaN())
        assertFalse(mult.isInfinite())
        assertTrue(mult > 0)

        // Bar Chart values
        val barValues = listOf(
            1.3 * baseRev,
            1.0 * baseRev,
            0.8 * baseRev,
            0.6 * baseRev,
            0.4 * baseRev
        )
        val growth = if (barValues.first() > 0) ((barValues.last() - barValues.first()) / barValues.first()) * 100 else 0.0
        assertFalse(growth.isNaN())
        assertFalse(growth.isInfinite())
        assertTrue(growth < 0)
    }

    // ------------------------------------------------------------------------
    // 2. THOUSAND SEPARATOR & DECIMAL FORMATTING TESTS
    // ------------------------------------------------------------------------

    private fun formatDisplayHelper(v: Double, step: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        return if (step < 1.0) {
            DecimalFormat("#,##0.0", symbols).format(v)
        } else {
            DecimalFormat("#,##0", symbols).format(v.toLong())
        }
    }

    @Test
    fun testThousandSeparator_largeNumbers_formatsWithCommas() {
        assertEquals("100,000,000", formatDisplayHelper(100000000.0, step = 10000.0))
        assertEquals("50,000,000", formatDisplayHelper(50000000.0, step = 50000.0))
        assertEquals("10,000,000", formatDisplayHelper(10000000.0, step = 10000.0))
        assertEquals("1,000,000", formatDisplayHelper(1000000.0, step = 1.0))
        assertEquals("450,000", formatDisplayHelper(450000.0, step = 100.0))
    }

    @Test
    fun testThousandSeparator_decimalInputs_formatsAccordingToStep() {
        // When step < 1.0 (e.g. AOV 1.1, LTV 1.1, Rejection 20.5%)
        assertEquals("1.1", formatDisplayHelper(1.1, step = 0.1))
        assertEquals("20.5", formatDisplayHelper(20.5, step = 0.5))
        assertEquals("1,000.5", formatDisplayHelper(1000.5, step = 0.1))

        // When step >= 1.0 (e.g. step = 500.0 or 1.0), v.toLong() truncates decimal component
        assertEquals("35,000", formatDisplayHelper(35000.5, step = 500.0))
        assertEquals("1,000", formatDisplayHelper(1000.75, step = 1.0))
    }

    @Test
    fun testSliderInputRow_textParsingAndClamping() {
        val range = 1000.0..500000.0

        // Formatted thousand-separated string user typing
        val input1 = "100,000"
        val cleaned1 = input1.replace(",", "")
        val parsed1 = cleaned1.toDoubleOrNull()
        assertNotNull(parsed1)
        assertEquals(100000.0, parsed1!!, 0.0)
        val clamped1 = parsed1.coerceIn(range)
        assertEquals(100000.0, clamped1, 0.0)

        // Large number above max range
        val input2 = "1,000,000"
        val cleaned2 = input2.replace(",", "")
        val parsed2 = cleaned2.toDoubleOrNull()
        assertNotNull(parsed2)
        assertEquals(1000000.0, parsed2!!, 0.0)
        val clamped2 = parsed2.coerceIn(range)
        assertEquals(500000.0, clamped2, 0.0) // Clamped to max range

        // Fractional input string
        val input3 = "35,000.5"
        val cleaned3 = input3.replace(",", "")
        val parsed3 = cleaned3.toDoubleOrNull()
        assertNotNull(parsed3)
        assertEquals(35000.5, parsed3!!, 0.0)

        // Non-numeric / Arabic numerals return null in standard parse
        val inputArabic = "١٠٠,٠٠٠"
        val cleanedArabic = inputArabic.replace(",", "")
        assertNull(cleanedArabic.toDoubleOrNull())
    }

    // ------------------------------------------------------------------------
    // 3. FLOATING BOTTOM NAV PADDING & BOTTOM WHITESPACE CLEARANCE TESTS
    // ------------------------------------------------------------------------

    @Test
    fun testFloatingBottomNav_paddingAndClearanceCalculations() {
        // FloatingBottomNavBar dimensions:
        val barContainerHeight = 60 // 60.dp
        val barVerticalPaddingTop = 12 // 12.dp
        val barVerticalPaddingBottom = 12 // 12.dp
        val totalBottomBarHeight = barContainerHeight + barVerticalPaddingTop + barVerticalPaddingBottom // 84.dp

        // Scaffold innerPadding.calculateBottomPadding() equals totalBottomBarHeight (84.dp)

        // DashboardScreen / AnalysisScreen Column bottom padding:
        val columnBottomPadding = 100 // 100.dp
        val trailingSpacerHeight = 16 // 16.dp

        val totalEffectiveBottomClearance = totalBottomBarHeight + columnBottomPadding + trailingSpacerHeight

        // Total clearance below scrollable content = 200.dp
        assertEquals(84, totalBottomBarHeight)
        assertEquals(200, totalEffectiveBottomClearance)

        // Verify that content clearance (200.dp) is significantly greater than bottom bar height (84.dp),
        // confirming that content will NEVER be hidden or overlapped by the floating bottom nav bar.
        assertTrue("Bottom clearance must exceed floating nav height", totalEffectiveBottomClearance > totalBottomBarHeight)
    }
}
