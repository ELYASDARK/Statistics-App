package com.uniteconomics.calculator

import com.uniteconomics.calculator.db.ProjectEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseConcurrencyTest {

    @Test
    fun testProjectEntityFromInputsAndResult() {
        val inputs = CalculatorInputs(
            retailPrice = 45000.0,
            sourcingCost = 15000.0,
            cac = 6000.0,
            rejectionRate = 18.0,
            deadstockRate = 3.0
        )
        val result = CalculatorLogic.calculate(inputs)

        val entity = ProjectEntity.fromInputsAndResult(
            id = 101L,
            name = "Summer Shoes Campaign",
            inputs = inputs,
            result = result
        )

        assertEquals(101L, entity.id)
        assertEquals("Summer Shoes Campaign", entity.name)
        assertEquals(45000.0, entity.retailPrice, 0.001)
        assertEquals(15000.0, entity.sourcingCost, 0.001)
        assertEquals(result.revenue, entity.revenue, 0.001)
        assertEquals(result.netProfitTotal, entity.netProfitTotal, 0.001)
        assertFalse(entity.isLoss)

        val restoredInputs = entity.toCalculatorInputs()
        assertEquals(inputs.retailPrice, restoredInputs.retailPrice, 0.001)
        assertEquals(inputs.sourcingCost, restoredInputs.sourcingCost, 0.001)
        assertEquals(inputs.cac, restoredInputs.cac, 0.001)
        assertEquals(inputs.rejectionRate, restoredInputs.rejectionRate, 0.001)
        assertEquals(inputs.deadstockRate, restoredInputs.deadstockRate, 0.001)
    }

    @Test
    fun testProjectEntityLossStateMapping() {
        val lossInputs = CalculatorInputs(
            retailPrice = 10000.0,
            sourcingCost = 25000.0,
            cac = 20000.0
        )
        val lossResult = CalculatorLogic.calculate(lossInputs)

        assertTrue(lossResult.isLoss)

        val entity = ProjectEntity.fromInputsAndResult(
            name = "Failing Campaign",
            inputs = lossInputs,
            result = lossResult
        )

        assertTrue(entity.isLoss)
        assertTrue(entity.netProfitTotal <= 0.0)
    }

    @Test
    fun testProjectEntityCloningIntegrity() {
        val entity = ProjectEntity(
            id = 55L,
            name = "Original Campaign",
            retailPrice = 38000.0
        )

        val cloned = entity.copy(
            id = 0L,
            name = "${entity.name} (Copy)",
            updatedAt = System.currentTimeMillis()
        )

        assertEquals(0L, cloned.id)
        assertEquals("Original Campaign (Copy)", cloned.name)
        assertEquals(38000.0, cloned.retailPrice, 0.001)
    }
}
