package com.uniteconomics.calculator

import com.uniteconomics.calculator.db.ProjectEntity
import androidx.compose.ui.unit.sp
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

    @Test
    fun testProjectEntityCloneNaming() {
        val original = ProjectEntity(id = 5L, name = "Handmade Bag")
        val duplicate = original.copy(
            id = 0L,
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        assertEquals("Handmade Bag (Copy)", duplicate.name)
        assertEquals(0L, duplicate.id)
    }

    @Test
    fun testProjectEntityCustomCreatedAtPreservation() {
        val pastTimestamp = 1600000000000L
        val inputs = CalculatorInputs()
        val result = CalculatorLogic.runFinancialModel(inputs)
        val entity = ProjectEntity.fromInputsAndResult(
            id = 10L,
            name = "Preserved Time Project",
            inputs = inputs,
            result = result,
            createdAt = pastTimestamp
        )
        assertEquals(pastTimestamp, entity.createdAt)
    }

    @Test
    fun testSaveAsNewEntityGeneratesZeroId() {
        val loadedProject = ProjectEntity(id = 42L, name = "Original Shampo")
        val inputs = CalculatorInputs(retailPrice = 45000.0)
        val result = CalculatorLogic.runFinancialModel(inputs)

        // Save as New -> targetId = 0L
        val saveAsNew = true
        val targetId = if (saveAsNew) 0L else loadedProject.id
        val newEntity = ProjectEntity.fromInputsAndResult(
            id = targetId,
            name = "New Perfume",
            inputs = inputs,
            result = result
        )

        assertEquals(0L, newEntity.id)
        assertEquals("New Perfume", newEntity.name)
    }

    @Test
    fun testUpdateExistingEntityPreservesId() {
        val loadedProject = ProjectEntity(id = 42L, name = "Original Shampo")
        val inputs = CalculatorInputs(retailPrice = 50000.0)
        val result = CalculatorLogic.runFinancialModel(inputs)

        // Update Existing -> targetId = loadedProject.id
        val saveAsNew = false
        val targetId = if (saveAsNew) 0L else loadedProject.id
        val updatedEntity = ProjectEntity.fromInputsAndResult(
            id = targetId,
            name = "Original Shampo (Updated)",
            inputs = inputs,
            result = result
        )

        assertEquals(42L, updatedEntity.id)
        assertEquals("Original Shampo (Updated)", updatedEntity.name)
    }

    @Test
    fun testInstrumentTrackingTypographyRule() {
        assertEquals((-0.6).sp, AppTypography.displayMedium.letterSpacing)
        assertEquals(2.0.sp, AppTypography.labelLarge.letterSpacing)
        assertEquals(2.0.sp, AppTypography.labelSmall.letterSpacing)
    }
}
