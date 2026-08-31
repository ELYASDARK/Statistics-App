package com.uniteconomics.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Empirical unit tests for financial scenarios, risk ranking calculations, and project entity transformations.
 */
class Milestone3ChallengerTest {

    @Test
    fun testBreakdownRowCard_valueFormatting_lossVsProfitableState() {
        val profitableInputs = CalculatorInputs(
            retailPrice = 35000.0,
            sourcingCost = 10000.0,
            targetProfitGoal = 1000000.0
        )
        val profitableResult = CalculatorLogic.runFinancialModel(profitableInputs)
        assertFalse(profitableResult.isLoss)

        assertTrue(profitableResult.revenue > 0)
        assertTrue(profitableResult.targetSuccessfulOrders > 0)
        assertTrue(profitableResult.grossProfitPerOrder > 0)
        assertTrue(profitableResult.breakevenOrders > 0)
        assertTrue(profitableResult.totalDispatchedOrders > 0)

        val lossInputs = CalculatorInputs(
            retailPrice = 5000.0,
            sourcingCost = 20000.0,
            cac = 15000.0
        )
        val lossResult = CalculatorLogic.runFinancialModel(lossInputs)
        assertTrue(lossResult.isLoss)

        assertEquals(0.0, lossResult.revenue, 0.0)
        assertEquals(0, lossResult.targetSuccessfulOrders)
        assertEquals(0, lossResult.breakevenOrders)
    }

    @Test
    fun testRiskMetrics_dynamicRanking_scenarioA_highRTSLoss() {
        // High RTS rejection scenario
        val inputs = CalculatorInputs(
            retailPrice = 35000.0,
            sourcingCost = 10000.0,
            rejectionRate = 45.0,
            rtsShippingFee = 2500.0,
            shippingCost = 4000.0,
            opsCost = 1000.0,
            deadstockRate = 2.0
        )
        val result = CalculatorLogic.runFinancialModel(inputs)

        val totalSent = result.totalDispatchedOrders.toDouble()
        val rejectDec = inputs.rejectionRate / 100.0
        val rtsLoss = totalSent * (inputs.shippingCost + inputs.opsCost + inputs.rtsShippingFee) * rejectDec
        val deadstockLoss = (totalSent * inputs.sourcingCost * inputs.aovMultiplier) * (inputs.deadstockRate / 100.0)

        assertTrue("RTS loss must exceed deadstock loss under high RTS rate", rtsLoss > deadstockLoss)
    }

    @Test
    fun testProjectEntity_conversionRoundTrip() {
        val originalInputs = CalculatorInputs(
            retailPrice = 45000.0,
            sourcingCost = 12000.0,
            rejectionRate = 18.0
        )
        val result = CalculatorLogic.runFinancialModel(originalInputs)

        val entity = com.uniteconomics.calculator.db.ProjectEntity.fromInputsAndResult(
            name = "Test Product",
            inputs = originalInputs,
            result = result
        )

        val restoredInputs = entity.toCalculatorInputs()
        assertEquals(originalInputs.retailPrice, restoredInputs.retailPrice, 0.001)
        assertEquals(originalInputs.sourcingCost, restoredInputs.sourcingCost, 0.001)
        assertEquals(originalInputs.rejectionRate, restoredInputs.rejectionRate, 0.001)
    }

    @Test
    fun testDictionary_crossLanguageMappingAndDescriptions() {
        assertTrue(KurdishTerms.dictionary.isNotEmpty())
        assertTrue(EnglishTerms.dictionary.isNotEmpty())

        for ((_, kDesc) in KurdishTerms.dictionary) {
            assertNotEquals("ڕوونکردنەوە نەدۆزرایەوە.", kDesc)
            assertFalse(kDesc.isBlank())
        }

        for ((_, eDesc) in EnglishTerms.dictionary) {
            assertNotEquals("Explanation not found.", eDesc)
            assertFalse(eDesc.isBlank())
        }
    }

    @Test
    fun testGlossaryCategory_deterministicMapping() {
        assertEquals(GlossaryCategory.CAPITAL_RISK, categorizeGlossaryTerm("Fixed Monthly Overhead"))
        assertEquals(GlossaryCategory.CAPITAL_RISK, categorizeGlossaryTerm("Fixed Monthly Overhead (Rent/Salaries)"))
        assertEquals(GlossaryCategory.CAPITAL_RISK, categorizeGlossaryTerm("خەرجییە جێگیرەکانی مانگانە"))

        assertEquals(GlossaryCategory.LOGISTICS_OPERATIONS, categorizeGlossaryTerm("Deadstock Rate (%)"))
        assertEquals(GlossaryCategory.LOGISTICS_OPERATIONS, categorizeGlossaryTerm("ڕێژەی کاڵای نەفرۆشراو"))
        assertEquals(GlossaryCategory.LOGISTICS_OPERATIONS, categorizeGlossaryTerm("Total Dispatched Orders"))
        assertEquals(GlossaryCategory.LOGISTICS_OPERATIONS, categorizeGlossaryTerm("کۆی گشتی داواکارییە نێردراوەکان"))

        assertEquals(GlossaryCategory.PROFIT_REVENUE, categorizeGlossaryTerm("Total Net Profit"))
        assertEquals(GlossaryCategory.PROFIT_REVENUE, categorizeGlossaryTerm("Revenue"))
        assertEquals(GlossaryCategory.MARKETING_ACQUISITION, categorizeGlossaryTerm("Customer Acquisition Cost (CAC)"))
        assertEquals(GlossaryCategory.MARKETING_ACQUISITION, categorizeGlossaryTerm("Discounts (%)"))
    }

    @Test
    fun testNormalizeKurdishSearch() {
        assertEquals("12345", normalizeKurdishSearch("١٢٣٤٥"))
        assertEquals("12345", normalizeKurdishSearch("۱۲۳۴۵"))
        assertEquals("قازانج", normalizeKurdishSearch("قازانجـ"))
        assertEquals("داهات", normalizeKurdishSearch("دَاهَاتْ"))
    }

    @Test
    fun testBuildIndexedGlossary_completeness() {
        val kurdishGlossary = buildIndexedGlossary(isKurdish = true)
        val englishGlossary = buildIndexedGlossary(isKurdish = false)

        assertTrue(kurdishGlossary.size >= 30)
        assertTrue(englishGlossary.size >= 30)

        for (item in kurdishGlossary) {
            assertTrue(item.term.isNotBlank())
            assertTrue(item.description.isNotBlank())
            assertNotNull(item.category)
        }

        for (item in englishGlossary) {
            assertTrue(item.term.isNotBlank())
            assertTrue(item.description.isNotBlank())
            assertNotNull(item.category)
        }
    }
}

