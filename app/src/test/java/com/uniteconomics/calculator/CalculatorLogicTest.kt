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
        assertEquals(8397.79, result.netProfitPerProduct, 0.01)
        assertEquals(1007734.67, result.netProfitTotal, 1.0)
        assertEquals(140000.0, result.revenue, 0.01)
        assertEquals(4.0, result.targetDailyOrders, 0.001)
        assertEquals(106469.78, result.dailyCashBurn, 100.0)
        assertEquals(120, result.targetSuccessfulOrders)
        assertEquals(12147.79, result.grossProfitPerOrder, 0.01)
        assertEquals(38, result.breakevenOrders)
        assertEquals(152, result.totalDispatchedOrders)
        assertEquals(3194093.51, result.totalProjectExpenses, 1.0)
        assertEquals(652030.73, result.workingCapitalFloat, 5000.0)
    }

    @Test
    fun testLossScenario() {
        val inputs = CalculatorInputs(cac = 50000.0)
        val result = CalculatorLogic.runFinancialModel(inputs)

        assertTrue(result.isLoss)
        assertEquals(0.0, result.netProfitPerProduct, 0.0)
        assertEquals(0.0, result.netProfitTotal, 0.0)
        assertEquals(0.0, result.dailyTargetOrders, 0.0)
        assertEquals(0.0, result.dailyCashBurn, 0.0)
        assertEquals(0.0, result.revenue, 0.0)
        assertEquals(0, result.targetSuccessfulOrders)
        assertEquals(0, result.breakevenOrders)
        assertEquals(0, result.totalDispatchedOrders)
        assertEquals(0.0, result.totalProjectExpenses, 0.0)
        assertEquals(0.0, result.workingCapitalFloat, 0.0)
        assertEquals(0.0, result.burnRateDay, 0.0)
    }

    @Test
    fun testTimeframeOptionScaling() {
        val inputs = CalculatorInputs(netTimeframe = TimeframeOption.DAILY)
        val result = CalculatorLogic.runFinancialModel(inputs)
        assertEquals(1007734.67 / 30.0, result.netProfitTotal, 1.0)
    }
}
