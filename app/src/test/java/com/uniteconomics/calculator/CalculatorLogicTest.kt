package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorLogicTest {

    @Test
    fun testDefaultInputs() {
        val inputs = CalculatorInputs()
        val result = CalculatorLogic.runFinancialModel(inputs)

        assertFalse(result.isLoss)
        assertEquals(8410.84, result.netProfitPerProduct, 0.01)
        assertEquals(1000890.23, result.netProfitTotal, 1.0)
        assertEquals(4165000.0, result.revenue, 0.01)
        assertEquals(3.966, result.targetDailyOrders, 0.001)
        assertEquals(105920.40, result.dailyCashBurn, 1.0)
        assertEquals(119, result.targetSuccessfulOrders)
        assertEquals(12192.35, result.grossProfitPerOrder, 0.01)
        assertEquals(37, result.breakevenOrders)
        assertEquals(151, result.totalDispatchedOrders)
        assertEquals(3177611.89, result.totalProjectExpenses, 1.0)
        assertEquals(648714.39, result.workingCapitalFloat, 1.0)
    }

    @Test
    fun testLossScenario() {
        val inputs = CalculatorInputs(cac = 50000.0)
        val result = CalculatorLogic.runFinancialModel(inputs)

        assertTrue(result.isLoss)
        assertEquals(result.grossProfitPerOrder, result.netProfitPerProduct, 0.01)
        assertEquals(-450000.0, result.netProfitTotal, 0.01)
        assertEquals(0.0, result.dailyTargetOrders, 0.0)
        assertEquals(15000.0, result.dailyCashBurn, 0.01)
        assertEquals(0.0, result.revenue, 0.0)
        assertEquals(0, result.targetSuccessfulOrders)
        assertEquals(0, result.breakevenOrders)
        assertEquals(0, result.totalDispatchedOrders)
        assertEquals(450000.0, result.totalProjectExpenses, 0.01)
        assertEquals(105000.0, result.workingCapitalFloat, 0.01)
        assertEquals(15000.0, result.burnRateDay, 0.01)
        assertEquals(LossType.GROSS_LOSS, result.diagnostics.lossType)
        assertTrue(result.diagnostics.breakEvenRetailPrice > inputs.retailPrice)
    }

    @Test
    fun testTimeframeOptionScaling() {
        val dailyInputs = CalculatorInputs(netTimeframe = TimeframeOption.DAILY)
        val dailyResult = CalculatorLogic.runFinancialModel(dailyInputs)
        val totalInputs = CalculatorInputs(netTimeframe = TimeframeOption.TOTAL)
        val totalResult = CalculatorLogic.runFinancialModel(totalInputs)
        assertEquals(totalResult.netProfitTotal / 30.0, dailyResult.netProfitTotal, 0.01)
    }

    @Test
    fun testLossScenarioTimeframeScaling() {
        val dailyLossInputs = CalculatorInputs(cac = 50000.0, netTimeframe = TimeframeOption.DAILY)
        val dailyLossResult = CalculatorLogic.runFinancialModel(dailyLossInputs)
        assertEquals(-15000.0, dailyLossResult.netProfitTotal, 0.01)

        val totalLossInputs = CalculatorInputs(cac = 50000.0, netTimeframe = TimeframeOption.TOTAL)
        val totalLossResult = CalculatorLogic.runFinancialModel(totalLossInputs)
        assertEquals(-450000.0, totalLossResult.netProfitTotal, 0.01)
    }
}
