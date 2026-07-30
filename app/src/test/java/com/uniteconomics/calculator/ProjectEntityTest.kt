package com.uniteconomics.calculator

import com.calculator.app.data.CalculatorInputs
import com.calculator.app.data.CalculatorLogic
import com.uniteconomics.calculator.db.ProjectEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProjectEntityTest {

    @Test
    fun testProjectEntityConversionFromInputsAndResult() {
        val inputs = CalculatorInputs(
            retailPrice = 40000.0,
            sourcingCost = 12000.0,
            fixedMonthlyExpenses = 500000.0
        )

        val result = CalculatorLogic.runFinancialModel(inputs)

        val entity = ProjectEntity.fromInputsAndResult(
            id = 1L,
            name = "Test Leather Shoes Project",
            inputs = inputs,
            result = result
        )

        assertEquals("Test Leather Shoes Project", entity.name)
        assertEquals(40000.0, entity.retailPrice, 0.01)
        assertEquals(12000.0, entity.sourcingCost, 0.01)
        assertEquals(500000.0, entity.fixedMonthlyExpenses, 0.01)
        assertEquals(result.netProfitTotal, entity.netProfitTotal, 0.01)
        assertEquals(result.revenue, entity.revenue, 0.01)
        assertFalse(entity.isLoss)

        // Convert back to CalculatorInputs
        val restoredInputs = entity.toCalculatorInputs()
        assertEquals(40000.0, restoredInputs.effRetailPrice, 0.01)
        assertEquals(12000.0, restoredInputs.effSourcingCost, 0.01)
        assertEquals(500000.0, restoredInputs.effFixedMonthlyExpenses, 0.01)

        val restoredResult = CalculatorLogic.runFinancialModel(restoredInputs)
        assertEquals(result.netProfitTotal, restoredResult.netProfitTotal, 0.01)
    }
}
