package com.uniteconomics.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

import androidx.compose.foundation.lazy.LazyColumn

private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
private val DECIMAL_FORMAT = DecimalFormat("#,##0.0", US_SYMBOLS)
private val INTEGER_FORMAT = DecimalFormat("#,##0", US_SYMBOLS)

/**
 * Analysis & Machine screen composable providing 4 organized input section cards with bi-directional sliders,
 * detailed financial breakdown cards, and risk metrics sensitivity analysis.
 */
@Composable
fun AnalysisScreen(
    inputs: CalculatorInputs,
    result: CalculationResult,
    onInputsChange: (CalculatorInputs) -> Unit,
    revTimeframe: TimeframeOption,
    onRevTimeframeChange: (TimeframeOption) -> Unit,
    isKurdish: Boolean = true,
    onOpenDictionaryForTerm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    fun formatAmt(num: Double, isDec: Boolean = false): String {
        if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
        return if (isDec) DECIMAL_FORMAT.format(num) else INTEGER_FORMAT.format(num)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen Title Header
        item(key = "title_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isKurdish) "مەکینەی شیکاری و پارامەتەرەکان" else "Analysis & Inputs Engine",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textMain
                    )
                    Text(
                        text = if (isKurdish) "دەستکاری ١٩ پارامەتەر بە سلایدەر" else "Tune 19 financial variables in real-time",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = neuColors.textMuted
                    )
                }
            }
        }

        // Section 1: Revenue & Product Cost
        item(key = "section_1") {
            SectionCard(
                headerText = if (isKurdish) "داهات و تێچووی بەرهەم" else "Revenue & Product Cost",
                isKurdish = isKurdish
            ) {
                SliderInputRow(
                    label = if (isKurdish) "نرخی فرۆشتن بە کڕیار" else "Retail Price",
                    value = inputs.effRetailPrice,
                    onValueChange = { newR -> onInputsChange(inputs.copy(retailPrice = newR, retail = newR)) },
                    valueRange = 1000f..500000f,
                    step = 500.0,
                    unit = "IQD",
                    isMarked = true,
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "نرخی فرۆشتن بە کڕیار" else "Retail Price") }
                )

                SliderInputRow(
                    label = if (isKurdish) "تێچووی گەیشتنی بەرهەم" else "Sourcing Cost",
                    subLabel = if (isKurdish) "نرخی دابینکەر + گواستنەوە" else "Supplier Price + Shipping",
                    value = inputs.effSourcingCost,
                    onValueChange = { newS -> onInputsChange(inputs.copy(sourcingCost = newS, sourcing = newS)) },
                    valueRange = 500f..200000f,
                    step = 250.0,
                    unit = "IQD",
                    isMarked = true,
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "تێچووی گەیشتنی بەرهەم" else "Sourcing Cost") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" else "Items / Order (AOV)",
                    value = inputs.effAovMultiplier,
                    onValueChange = { newAov -> onInputsChange(inputs.copy(aovMultiplier = newAov, aov = newAov)) },
                    valueRange = 0.5f..10f,
                    step = 0.1,
                    unit = if (isKurdish) "دانە" else "Units",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" else "Average Order Value (AOV)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "داشکاندنی پێدراو (%)" else "Discount Rate (%)",
                    value = inputs.effDiscounts,
                    onValueChange = { newDisc -> onInputsChange(inputs.copy(discounts = newDisc, discount = newDisc)) },
                    valueRange = 0f..50f,
                    step = 1.0,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "داشکاندنی پێدراو (%)" else "Discounts (%)") }
                )
            }
        }

        // Section 2: Marketing & Customer Acquisition
        item(key = "section_2") {
            SectionCard(
                headerText = if (isKurdish) "بەبازاڕکردن و بەدەستهێنانی کڕیار" else "Marketing & Acquisition (CAC)",
                isKurdish = isKurdish
            ) {
                SliderInputRow(
                    label = if (isKurdish) "تێچووی بەدەستهێنانی کڕیار (CAC)" else "Customer Acquisition Cost (CAC)",
                    value = inputs.cac,
                    onValueChange = { newCac -> onInputsChange(inputs.copy(cac = newCac)) },
                    valueRange = 500f..50000f,
                    step = 250.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "تێچووی بەدەستهێنانی کڕیار (CAC)" else "Customer Acquisition Cost (CAC)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "کرێی دەروازەی پارەدان (%)" else "Ad Payment Gateway Fee (%)",
                    value = inputs.adFee,
                    onValueChange = { newAd -> onInputsChange(inputs.copy(adFee = newAd)) },
                    valueRange = 0f..20f,
                    step = 0.5,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "کرێی دەروازەی پارەدان (%)" else "Payment Gateway Fee (%)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ڕێژەی دووبارە کڕینەوە (LTV)" else "Lifetime Re-order Rate (LTV)",
                    value = inputs.effLtvMultiplier,
                    onValueChange = { newLtv -> onInputsChange(inputs.copy(ltvMultiplier = newLtv, ltv = newLtv)) },
                    valueRange = 1.0f..5.0f,
                    step = 0.1,
                    unit = "x",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی دووبارە کڕینەوە (LTV)" else "Repeat Purchase Rate (LTV)") }
                )
            }
        }

        // Section 3: Shipping & Risks
        item(key = "section_3") {
            SectionCard(
                headerText = if (isKurdish) "گواستنەوە و مەترسییەکان" else "Logistics, RTS & Risk Losses",
                isKurdish = isKurdish
            ) {
                SliderInputRow(
                    label = if (isKurdish) "کرێی گواستنەوە" else "Courier Delivery Fee",
                    value = inputs.effShippingCost,
                    onValueChange = { newShip -> onInputsChange(inputs.copy(shippingCost = newShip, shipping = newShip)) },
                    valueRange = 0f..25000f,
                    step = 250.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "کرێی گواستنەوە" else "Shipping Cost") }
                )

                SliderInputRow(
                    label = if (isKurdish) "تێچووی پێچانەوە و گەیاندن" else "Packaging & Handling Ops",
                    value = inputs.effOpsCost,
                    onValueChange = { newOps -> onInputsChange(inputs.copy(opsCost = newOps, ops = newOps)) },
                    valueRange = 0f..10000f,
                    step = 100.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "تێچووی پێچانەوە و گەیاندن" else "Packaging & Handling") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" else "RTS Rejection Rate (%)",
                    value = inputs.effRejectionRate,
                    onValueChange = { newRej -> onInputsChange(inputs.copy(rejectionRate = newRej, reject = newRej)) },
                    valueRange = 0f..80f,
                    step = 0.5,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" else "Rejection Rate (RTS %)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "سزای دارایی داواکارییە ڕەتکراوەکان" else "RTS Return Penalty Fee",
                    value = inputs.effRtsShippingFee,
                    onValueChange = { newRts -> onInputsChange(inputs.copy(rtsShippingFee = newRts, returnFeeRTS = newRts)) },
                    valueRange = 0f..25000f,
                    step = 250.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "سزای دارایی داواکارییە ڕەتکراوەکان" else "RTS Penalty Fee") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ڕێژەی پارە گەڕاندنەوە" else "Customer Refund Rate (%)",
                    value = inputs.effRefundRate,
                    onValueChange = { newRef -> onInputsChange(inputs.copy(refundRate = newRef, refund = newRef)) },
                    valueRange = 0f..30f,
                    step = 0.5,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی پارە گەڕاندنەوە" else "Refund Rate (%)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "سزای پارە گەڕاندنەوەی دوای گەیاندن" else "Post-Delivery Refund Penalty",
                    value = inputs.refundPenalty,
                    onValueChange = { newRefPen -> onInputsChange(inputs.copy(refundPenalty = newRefPen)) },
                    valueRange = 0f..25000f,
                    step = 250.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "سزای پارە گەڕاندنەوەی دوای گەیاندن" else "Post-Delivery Refund Penalty") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ڕێژەی زیان/لەناوچوون" else "Transit Damage Rate (%)",
                    value = inputs.effDamageRate,
                    onValueChange = { newDam -> onInputsChange(inputs.copy(damageRate = newDam, damage = newDam)) },
                    valueRange = 0f..30f,
                    step = 0.5,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی زیان/لەناوچوون" else "Damage/Loss Rate (%)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ڕێژەی کاڵای نەفرۆشراو" else "Deadstock Loss Rate (%)",
                    value = inputs.effDeadstockRate,
                    onValueChange = { newDead -> onInputsChange(inputs.copy(deadstockRate = newDead, deadstock = newDead)) },
                    valueRange = 0f..30f,
                    step = 0.5,
                    unit = "%",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی کاڵای نەفرۆشراو" else "Deadstock Rate (%)") }
                )
            }
        }

        // Section 4: Business Structure & Goals
        item(key = "section_4") {
            SectionCard(
                headerText = if (isKurdish) "پێکهاتەی کار و ئامانجەکان" else "Overhead & Campaign Goals",
                isKurdish = isKurdish
            ) {
                SliderInputRow(
                    label = if (isKurdish) "خەرجییە جێگیرەکانی مانگانە" else "Fixed Monthly Overhead (Rent/Salaries)",
                    value = inputs.effFixedMonthlyExpenses,
                    onValueChange = { newFix -> onInputsChange(inputs.copy(fixedMonthlyExpenses = newFix, fixedMonthly = newFix)) },
                    valueRange = 0f..10000000f,
                    step = 10000.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "خەرجییە جێگیرەکانی مانگانە" else "Fixed Monthly Overhead (Rent/Salaries)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ئامانجی قازانجی پوختە" else "Target Profit Goal",
                    value = inputs.effTargetProfitGoal,
                    onValueChange = { newGoal -> onInputsChange(inputs.copy(targetProfitGoal = newGoal, goal = newGoal)) },
                    valueRange = 0f..50000000f,
                    step = 50000.0,
                    unit = "IQD",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ئامانجی قازانجی پوختە" else "Net Profit Target") }
                )

                SliderInputRow(
                    label = if (isKurdish) "ماوەی کات (بە ڕۆژ)" else "Campaign Horizon (Days)",
                    value = inputs.effProjectDurationDays,
                    onValueChange = { newDays -> onInputsChange(inputs.copy(projectDurationDays = newDays, days = newDays)) },
                    valueRange = 1f..365f,
                    step = 1.0,
                    unit = if (isKurdish) "ڕۆژ" else "Days",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ماوەی کات (بە ڕۆژ)" else "Time Horizon (Days)") }
                )

                SliderInputRow(
                    label = if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" else "Remittance Payout Delay (Days)",
                    value = inputs.effCapitalRemittanceFrequency,
                    onValueChange = { newRem -> onInputsChange(inputs.copy(capitalRemittanceFrequency = newRem, remit = newRem)) },
                    valueRange = 1f..60f,
                    step = 1.0,
                    unit = if (isKurdish) "ڕۆژ" else "Days",
                    isKurdish = isKurdish,
                    onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" else "Remittance Payout Delay (Days)") }
                )
            }
        }

        // Detailed Financial Breakdown Card with Neumorphic Rows (Task 3.5)
        item(key = "breakdown") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 20.dp,
                        elevation = 4.dp
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Receipt,
                                contentDescription = "Breakdown",
                                tint = neuColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isKurdish) "شیکردنەوەی دارایی گشتی" else "Detailed Financial Breakdown",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMain
                            )
                        }

                        // Revenue Timeframe Selector Dropdown
                        RevTimeframeDropdown(
                            selectedOption = revTimeframe,
                            onOptionSelected = onRevTimeframeChange,
                            isKurdish = isKurdish
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = neuColors.border)

                    BreakdownRowCard(
                        index = 0,
                        label = if (isKurdish) "کۆی داهات" else "Total Revenue",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.displayRevenue)} IQD",
                        valueColor = neuColors.warning
                    )

                    BreakdownRowCard(
                        index = 1,
                        label = if (isKurdish) "ئامانجی داواکارییە سەرکەوتووەکان" else "Target Successful Orders",
                        valueStr = if (result.isLoss) "0" else "${formatAmt(result.targetUnits.toDouble())} ${if (isKurdish) "دانە" else "Orders"}"
                    )

                    BreakdownRowCard(
                        index = 2,
                        label = if (isKurdish) "قازانجی گشتی لە هەر داواکارییەک" else "Gross Profit / Order",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.grossPerSuccess)} IQD"
                    )

                    BreakdownRowCard(
                        index = 3,
                        label = if (isKurdish) "خاڵی یەکسانبوونەوە (داواکاریکان)" else "Breakeven Orders",
                        valueStr = if (result.isLoss) "N/A" else "${formatAmt(result.breakEvenUnits.toDouble())} ${if (isKurdish) "دانە" else "Orders"}"
                    )

                    BreakdownRowCard(
                        index = 4,
                        label = if (isKurdish) "کۆی گشتی داواکارییە نێردراوەکان" else "Total Dispatched Orders",
                        valueStr = if (result.isLoss) "0" else "${formatAmt(result.totalSent.toDouble())} ${if (isKurdish) "دانە" else "Dispatches"}"
                    )

                    BreakdownRowCard(
                        index = 5,
                        label = if (isKurdish) "کۆی گشتی خەرجییەکانی پڕۆژە" else "Total Project Expenses",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.totalExpenses)} IQD",
                        valueColor = neuColors.purple
                    )
                }
            }
        }

        // Risk Metrics Sensitivity Analysis Card with Severity-Based Color Semantics & Neumorphic Rows (Task 3.6)
        item(key = "risk_sensitivity") {
            val rtsLossAmount = remember(inputs, result) {
                val totalSent = result.totalSent.toDouble()
                val rejectDec = inputs.effRejectionRate / 100.0
                totalSent * (inputs.effShippingCost + inputs.effOpsCost + inputs.effRtsShippingFee) * rejectDec
            }
            val deadstockLossAmount = remember(inputs, result) {
                inputs.effDeadstockRate * result.totalExpenses / 100.0
            }
            val floatAmount = remember(result) {
                result.workingCapitalFloat
            }

            // Severity Color Coding based on metric magnitude rank
            val riskEntries = remember(rtsLossAmount, deadstockLossAmount, floatAmount, isKurdish, inputs) {
                val items = listOf(
                    Triple(if (isKurdish) "تێچووی زیانی گەیاندنەکانی RTS" else "Estimated RTS Failure Loss", rtsLossAmount, "${formatAmt(rtsLossAmount)} IQD"),
                    Triple(if (isKurdish) "خەمڵاندنی زیانی کاڵای نەفرۆشراو" else "Deadstock Capital Exposure", deadstockLossAmount, "${formatAmt(deadstockLossAmount)} IQD"),
                    Triple(if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (${inputs.effCapitalRemittanceFrequency.toInt()} ڕۆژ)" else "Remittance Delay Float Impact", floatAmount, "${formatAmt(floatAmount)} IQD")
                )
                val sorted = items.sortedByDescending { it.second }
                items.map { item ->
                    val rank = sorted.indexOf(item)
                    val color = when (rank) {
                        0 -> neuColors.loss
                        1 -> neuColors.warning
                        else -> neuColors.warning.copy(alpha = 0.7f)
                    }
                    Triple(item.first, item.third, color)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 20.dp,
                        elevation = 4.dp
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(neuColors.loss)
                        )
                        Text(
                            text = if (isKurdish) "مەترسی و زیانە شاراوەکان" else "Risk & Sensitivity Metrics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textMain
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = neuColors.border)

                    riskEntries.forEach { (label, valueStr, severityColor) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 4.dp,
                                    elevation = 1.dp
                                )
                                .background(
                                    color = severityColor.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(severityColor)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = neuColors.textMuted
                                    )
                                }

                                Text(
                                    text = valueStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = severityColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    headerText: String,
    isKurdish: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 20.dp,
                elevation = 4.dp
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = headerText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = neuColors.primary
            )
            HorizontalDivider(thickness = 1.dp, color = neuColors.border)
            content()
        }
    }
}

@Composable
private fun BreakdownRowCard(
    index: Int,
    label: String,
    valueStr: String,
    valueColor: Color? = null
) {
    val neuColors = LocalNeumorphicColors.current
    val isEven = index % 2 == 0
    val bgOpacity = if (isEven) 0.05f else 0.01f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 4.dp,
                elevation = 1.dp
            )
            .background(
                color = neuColors.primary.copy(alpha = bgOpacity),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = neuColors.textMuted
            )
            Text(
                text = valueStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor ?: neuColors.textMain
            )
        }
    }
}

@Composable
private fun RevTimeframeDropdown(
    selectedOption: TimeframeOption,
    onOptionSelected: (TimeframeOption) -> Unit,
    isKurdish: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val neuColors = LocalNeumorphicColors.current

    Box {
        Box(
            modifier = Modifier
                .neuConvex(
                    lightShadowColor = neuColors.shadowLight,
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    lightGradientColor = neuColors.warning.copy(alpha = 0.15f),
                    darkGradientColor = neuColors.warning.copy(alpha = 0.05f),
                    cornerRadius = 8.dp,
                    elevation = 2.dp
                )
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isKurdish) selectedOption.toKurdishLabel() else selectedOption.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.warning
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(neuColors.surface, shape = RoundedCornerShape(12.dp))
                .neuFlat(
                    lightShadowColor = neuColors.shadowLight,
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    cornerRadius = 12.dp,
                    elevation = 6.dp
                )
        ) {
            TimeframeOption.entries.forEach { option ->
                val isSelected = option == selectedOption
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isKurdish) option.toKurdishLabel() else option.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) neuColors.primary else neuColors.textMain
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier = if (isSelected) {
                        Modifier.neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 8.dp,
                            elevation = 2.dp
                        )
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}
