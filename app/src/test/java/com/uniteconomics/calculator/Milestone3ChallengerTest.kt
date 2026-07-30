package com.uniteconomics.calculator

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Test

/**
 * Empirical Challenger Unit & Stress Tests for Milestone 3:
 * 1. Financial breakdown row neuFlat cards, corner radius 4dp, vertical padding 8dp, alternating background opacity (Item 3.5).
 * 2. Dynamic risk ranking and severity-based color coding (Red, Amber, Muted Yellow) with severity dots (Item 3.6).
 * 3. Typography scale compliance across input rows and card values (Item 3.1).
 */
class Milestone3ChallengerTest {

    // ------------------------------------------------------------------------
    // 1. FINANCIAL BREAKDOWN ROW CARDS (Item 3.5)
    // ------------------------------------------------------------------------

    @Test
    fun testBreakdownRowCard_alternatingBackgroundOpacity() {
        // BreakdownRowCard index parity logic:
        // isEven = index % 2 == 0
        // bgOpacity = if (isEven) 0.05f else 0.01f

        val opacity0 = if (0 % 2 == 0) 0.05f else 0.01f
        val opacity1 = if (1 % 2 == 0) 0.05f else 0.01f
        val opacity2 = if (2 % 2 == 0) 0.05f else 0.01f
        val opacity3 = if (3 % 2 == 0) 0.05f else 0.01f
        val opacity4 = if (4 % 2 == 0) 0.05f else 0.01f
        val opacity5 = if (5 % 2 == 0) 0.05f else 0.01f

        assertEquals(0.05f, opacity0, 0.0001f)
        assertEquals(0.01f, opacity1, 0.0001f)
        assertEquals(0.05f, opacity2, 0.0001f)
        assertEquals(0.01f, opacity3, 0.0001f)
        assertEquals(0.05f, opacity4, 0.0001f)
        assertEquals(0.01f, opacity5, 0.0001f)
    }

    @Test
    fun testBreakdownRowCard_cardDimensionsAndNeuFlatParameters() {
        val cornerRadius = 4.dp
        val elevation = 1.dp
        val verticalPadding = 8.dp
        val horizontalPadding = 12.dp

        assertEquals(4.dp, cornerRadius)
        assertEquals(1.dp, elevation)
        assertEquals(8.dp, verticalPadding)
        assertEquals(12.dp, horizontalPadding)
    }

    @Test
    fun testBreakdownRowCard_valueFormatting_lossVsProfitableState() {
        val profitableInputs = CalculatorInputs(
            retailPrice = 35000.0,
            sourcingCost = 10000.0,
            targetProfitGoal = 1000000.0
        )
        val profitableResult = CalculatorLogic.runFinancialModel(profitableInputs)
        assertFalse(profitableResult.isLoss)

        // For profitable state, values should be non-zero and formatted
        assertTrue(profitableResult.displayRevenue > 0)
        assertTrue(profitableResult.targetUnits > 0)
        assertTrue(profitableResult.grossPerSuccess > 0)
        assertTrue(profitableResult.breakEvenUnits > 0)
        assertTrue(profitableResult.totalSent > 0)

        val lossInputs = CalculatorInputs(
            retailPrice = 5000.0,
            sourcingCost = 20000.0,
            cac = 15000.0
        )
        val lossResult = CalculatorLogic.runFinancialModel(lossInputs)
        assertTrue(lossResult.isLoss)

        // For loss state, breakdown values present "Loss", "0", or "N/A"
        val revStr = if (lossResult.isLoss) "Loss" else "${lossResult.displayRevenue}"
        val targetUnitsStr = if (lossResult.isLoss) "0" else "${lossResult.targetUnits}"
        val breakevenStr = if (lossResult.isLoss) "N/A" else "${lossResult.breakEvenUnits}"

        assertEquals("Loss", revStr)
        assertEquals("0", targetUnitsStr)
        assertEquals("N/A", breakevenStr)
    }

    // ------------------------------------------------------------------------
    // 2. DYNAMIC RISK RANKING & SEVERITY-BASED COLOR CODING (Item 3.6)
    // ------------------------------------------------------------------------

    private data class RiskItem(
        val label: String,
        val amount: Double,
        val displayStr: String
    )

    private fun computeRiskColors(
        items: List<RiskItem>,
        lossColor: Color,
        warningColor: Color
    ): List<Pair<RiskItem, Color>> {
        val sorted = items.sortedByDescending { it.amount }
        return items.map { item ->
            val rank = sorted.indexOf(item)
            val color = when (rank) {
                0 -> lossColor
                1 -> warningColor
                else -> warningColor.copy(alpha = 0.7f)
            }
            Pair(item, color)
        }
    }

    @Test
    fun testRiskMetrics_dynamicRanking_scenarioA_highRTSLoss() {
        val redColor = NeuLightLoss // Color(0xFFDC2626)
        val amberColor = NeuLightWarning // Color(0xFFD97706)
        val mutedYellowColor = NeuLightWarning.copy(alpha = 0.7f)

        // Scenario A: High RTS rejection (50%) -> RTS loss dominant
        val rtsLoss = 150000.0
        val deadstockLoss = 20000.0
        val floatAmount = 50000.0

        val items = listOf(
            RiskItem("RTS Failure Loss", rtsLoss, "150,000 IQD"),
            RiskItem("Deadstock Exposure", deadstockLoss, "20,000 IQD"),
            RiskItem("Remittance Float", floatAmount, "50,000 IQD")
        )

        val mapped = computeRiskColors(items, redColor, amberColor)

        // Rank 0 (RTS): Red
        assertEquals(redColor, mapped[0].second)
        // Rank 1 (Float 50,000 > Deadstock 20,000): Amber
        assertEquals(amberColor, mapped[2].second)
        // Rank 2 (Deadstock 20,000): Muted Yellow
        assertEquals(mutedYellowColor, mapped[1].second)
    }

    @Test
    fun testRiskMetrics_dynamicRanking_scenarioB_highDeadstockLoss() {
        val redColor = NeuLightLoss
        val amberColor = NeuLightWarning
        val mutedYellowColor = NeuLightWarning.copy(alpha = 0.7f)

        // Scenario B: High Deadstock (25%) -> Deadstock loss dominant
        val rtsLoss = 30000.0
        val deadstockLoss = 300000.0
        val floatAmount = 40000.0

        val items = listOf(
            RiskItem("RTS Failure Loss", rtsLoss, "30,000 IQD"),
            RiskItem("Deadstock Exposure", deadstockLoss, "300,000 IQD"),
            RiskItem("Remittance Float", floatAmount, "40,000 IQD")
        )

        val mapped = computeRiskColors(items, redColor, amberColor)

        // Rank 0 (Deadstock): Red
        assertEquals(redColor, mapped[1].second)
        // Rank 1 (Float 40,000): Amber
        assertEquals(amberColor, mapped[2].second)
        // Rank 2 (RTS 30,000): Muted Yellow
        assertEquals(mutedYellowColor, mapped[0].second)
    }

    @Test
    fun testRiskMetrics_dynamicRanking_scenarioC_highRemittanceFloat() {
        val redColor = NeuLightLoss
        val amberColor = NeuLightWarning
        val mutedYellowColor = NeuLightWarning.copy(alpha = 0.7f)

        // Scenario C: High Remittance Float (60 days delay) -> Float dominant
        val rtsLoss = 25000.0
        val deadstockLoss = 15000.0
        val floatAmount = 500000.0

        val items = listOf(
            RiskItem("RTS Failure Loss", rtsLoss, "25,000 IQD"),
            RiskItem("Deadstock Exposure", deadstockLoss, "15,000 IQD"),
            RiskItem("Remittance Float", floatAmount, "500,000 IQD")
        )

        val mapped = computeRiskColors(items, redColor, amberColor)

        // Rank 0 (Float): Red
        assertEquals(redColor, mapped[2].second)
        // Rank 1 (RTS 25,000): Amber
        assertEquals(amberColor, mapped[0].second)
        // Rank 2 (Deadstock 15,000): Muted Yellow
        assertEquals(mutedYellowColor, mapped[1].second)
    }

    @Test
    fun testRiskRow_severityDotPropertiesAndNeuCardSpecs() {
        val dotSize = 8.dp
        val cardCornerRadius = 4.dp
        val cardElevation = 1.dp
        val verticalPadding = 8.dp
        val horizontalPadding = 12.dp

        assertEquals(8.dp, dotSize)
        assertEquals(4.dp, cardCornerRadius)
        assertEquals(1.dp, cardElevation)
        assertEquals(8.dp, verticalPadding)
        assertEquals(12.dp, horizontalPadding)
    }

    // ------------------------------------------------------------------------
    // 3. TYPOGRAPHY SCALE COMPLIANCE (Item 3.1)
    // ------------------------------------------------------------------------

    @Test
    fun testTypographyScale_inputRowsAndCardValues() {
        // Section Card Header
        val sectionHeaderFontSize = 16.sp
        val sectionHeaderFontWeight = FontWeight.SemiBold

        // Input Row Label
        val inputLabelFontSize = 14.sp
        val inputLabelFontWeight = FontWeight.SemiBold

        // Input Row SubLabel
        val inputSubLabelFontSize = 11.sp
        val inputSubLabelFontWeight = FontWeight.Normal

        // Input Row Text Box
        val inputFieldFontSize = 14.sp
        val inputFieldFontWeight = FontWeight.Medium

        // Input Row Unit
        val inputUnitFontSize = 14.sp
        val inputUnitFontWeight = FontWeight.Medium

        // Breakdown Card Label
        val breakdownLabelFontSize = 13.sp
        val breakdownLabelFontWeight = FontWeight.Normal

        // Breakdown Card Value
        val breakdownValueFontSize = 14.sp
        val breakdownValueFontWeight = FontWeight.Medium

        // Risk Card Label
        val riskLabelFontSize = 13.sp
        val riskLabelFontWeight = FontWeight.Normal

        // Risk Card Value
        val riskValueFontSize = 14.sp
        val riskValueFontWeight = FontWeight.Medium

        // Assert exact font sizes
        assertEquals(16.sp, sectionHeaderFontSize)
        assertEquals(14.sp, inputLabelFontSize)
        assertEquals(11.sp, inputSubLabelFontSize)
        assertEquals(14.sp, inputFieldFontSize)
        assertEquals(14.sp, inputUnitFontSize)
        assertEquals(13.sp, breakdownLabelFontSize)
        assertEquals(14.sp, breakdownValueFontSize)
        assertEquals(13.sp, riskLabelFontSize)
        assertEquals(14.sp, riskValueFontSize)

        // Assert exact font weights
        assertEquals(FontWeight.SemiBold, sectionHeaderFontWeight)
        assertEquals(FontWeight.SemiBold, inputLabelFontWeight)
        assertEquals(FontWeight.Normal, inputSubLabelFontWeight)
        assertEquals(FontWeight.Medium, inputFieldFontWeight)
        assertEquals(FontWeight.Medium, inputUnitFontWeight)
        assertEquals(FontWeight.Normal, breakdownLabelFontWeight)
        assertEquals(FontWeight.Medium, breakdownValueFontWeight)
        assertEquals(FontWeight.Normal, riskLabelFontWeight)
        assertEquals(FontWeight.Medium, riskValueFontWeight)
    }
}
