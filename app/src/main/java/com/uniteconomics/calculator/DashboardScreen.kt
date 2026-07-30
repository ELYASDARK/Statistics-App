package com.uniteconomics.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.max
import java.util.Locale

import androidx.compose.foundation.lazy.LazyColumn

private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
private val DECIMAL_FORMAT = DecimalFormat("#,##0.0", US_SYMBOLS)
private val INTEGER_FORMAT = DecimalFormat("#,##0", US_SYMBOLS)

/**
 * Dashboard screen composable featuring Executive KPI tiles, Live Canvas Valuation Line Chart,
 * Neumorphic Canvas Revenue Growth Bar Chart, Capital Burn Summary, and Strategy Recommendation.
 */
@Composable
fun DashboardScreen(
    result: CalculationResult,
    inputs: CalculatorInputs,
    netTimeframe: TimeframeOption,
    onNetTimeframeChange: (TimeframeOption) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isKurdish) "داشبۆردی بەڕێوەبردن" else "Executive Dashboard",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain
                        )
                        Text(
                            text = if (isKurdish) "پوختەی KPI و هێڵە داراییەکان" else "Key Performance Indicators & Valuation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = neuColors.textMuted
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = neuColors.border)
            }
        }

        // 1. Executive KPI Tiles Grid (2x2 equal width & height)
        item(key = "kpi_tiles") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Net Profit Per Product & Net Total Profit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Net Profit Per Product (Success Accent)
                    KpiTile(
                        title = if (isKurdish) "قازانجی/بەرهەم" else "Net / Product",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.netProfitPerProduct)} IQD",
                        subTitle = if (isKurdish) "قازانجی پوختەی یەکە" else "Net Profit / Unit",
                        isLoss = result.isLoss,
                        accentColor = neuColors.success,
                        term = "قازانجی پوختەی هەر بەرهەمێک",
                        onInfoClick = { onOpenDictionaryForTerm("قازانجی پوختەی هەر بەرهەمێک") },
                        isKurdish = isKurdish,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Card 2: Net Total Profit with Timeframe Selector (Standardized "?" Help Icon + Suffix Text)
                    KpiTile(
                        title = if (isKurdish) "کۆی قازانج" else "Net Profit",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else {
                            val suffix = when (netTimeframe) {
                                TimeframeOption.DAILY -> if (isKurdish) "/ڕۆژ" else "/day"
                                TimeframeOption.WEEKLY -> if (isKurdish) "/هەفتە" else "/wk"
                                TimeframeOption.MONTHLY -> if (isKurdish) "/مانگ" else "/mo"
                                TimeframeOption.TOTAL -> ""
                            }
                            "${formatAmt(result.netProfitTotal)} IQD$suffix"
                        },
                        subTitle = if (isKurdish) "کۆی قازانجی چاوەڕوانکراو" else "Projected Net Profit",
                        isLoss = result.isLoss,
                        accentColor = neuColors.success,
                        term = if (isKurdish) "کۆی قازانجی پوختە" else "Total Net Profit",
                        onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "کۆی قازانجی پوختە" else "Total Net Profit") },
                        isKurdish = isKurdish,
                        headerAction = {
                            TimeframeDropdownButton(
                                selectedOption = netTimeframe,
                                onOptionSelected = onNetTimeframeChange,
                                isKurdish = isKurdish
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // Row 2: Daily Target Orders & Daily Cash Burn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 3: Daily Target Orders
                    KpiTile(
                        title = if (isKurdish) "ئامانجی ڕۆژانە" else "Daily Target",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.dailyTargetOrders, isDec = true)} ${if (isKurdish) "داواکاری" else "Orders"}",
                        subTitle = if (isKurdish) "ئامانجی فرۆشتن" else "Target Velocity",
                        isLoss = result.isLoss,
                        accentColor = neuColors.info,
                        term = "ئامانجی داواکارییەکانی ڕۆژانە",
                        onInfoClick = { onOpenDictionaryForTerm("ئامانجی داواکارییەکانی ڕۆژانە") },
                        isKurdish = isKurdish,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Card 4: Daily Cash Burn
                    KpiTile(
                        title = if (isKurdish) "خەرجی ڕۆژانە" else "Daily Burn",
                        valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.dailyBurn)} IQD",
                        subTitle = if (isKurdish) "سوتانی ڕۆژانە" else "Burn Rate",
                        isLoss = result.isLoss,
                        accentColor = neuColors.warning,
                        term = "خەرجییە ڕۆژانەییەکان",
                        onInfoClick = { onOpenDictionaryForTerm("خەرجییە ڕۆژانەییەکان") },
                        isKurdish = isKurdish,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // 2. Live Canvas Valuation Line Chart
        item(key = "valuation_chart") {
            ValuationLineChart(
                result = result,
                isKurdish = isKurdish
            )
        }

        // 3. Neumorphic Canvas Revenue Growth Bar Chart
        item(key = "revenue_chart") {
            RevenueGrowthBarChart(
                result = result,
                isKurdish = isKurdish
            )
        }

        // 4. Capital Burn Summary Card
        item(key = "burn_summary") {
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
                                .background(neuColors.warning)
                        )
                        Text(
                            text = if (isKurdish) "پوختەی خەرجکردن و سەرمایە" else "Capital & Burn Rate Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textMain
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = neuColors.border)

                    BurnMetricRow(
                        label = if (isKurdish) "خەرجییەکانی ڕۆژانە" else "Daily Run Rate",
                        valueStr = "${formatAmt(result.burnRateDay)} IQD",
                        isKurdish = isKurdish
                    )
                    BurnMetricRow(
                        label = if (isKurdish) "خەرجییەکانی هەفتانە" else "Weekly Run Rate",
                        valueStr = "${formatAmt(result.burnRateWeek)} IQD",
                        isKurdish = isKurdish
                    )
                    BurnMetricRow(
                        label = if (isKurdish) "خەرجییەکانی مانگانە" else "Monthly Run Rate",
                        valueStr = "${formatAmt(result.burnRateMonth)} IQD",
                        isKurdish = isKurdish
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Working Capital Float Card styled with neuConvex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                lightGradientColor = neuColors.purple.copy(alpha = 0.12f),
                                darkGradientColor = neuColors.purple.copy(alpha = 0.03f),
                                cornerRadius = 16.dp,
                                elevation = 4.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) "سەرمایەی کارپێکردنی پێویست (Float)" else "Working Capital Float",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.purple
                            )
                            Text(
                                text = "${formatAmt(result.workingCapitalFloat)} IQD",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.purple
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiTile(
    title: String,
    valueStr: String,
    subTitle: String? = null,
    isLoss: Boolean,
    accentColor: Color,
    term: String,
    onInfoClick: () -> Unit,
    isKurdish: Boolean,
    headerAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 18.dp,
                elevation = 4.dp
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (headerAction != null) {
                        headerAction()
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                lightGradientColor = neuColors.shadowLight.copy(alpha = 0.4f),
                                darkGradientColor = neuColors.shadowDark.copy(alpha = 0.2f),
                                cornerRadius = 10.dp,
                                elevation = 1.dp
                            )
                            .clickable { onInfoClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Term Info",
                            tint = neuColors.textMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = valueStr,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLoss) neuColors.loss else accentColor,
                textAlign = if (isKurdish) TextAlign.Left else TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (!subTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = neuColors.textMuted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TimeframeDropdownButton(
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
                    lightGradientColor = neuColors.primary.copy(alpha = 0.15f),
                    darkGradientColor = neuColors.primary.copy(alpha = 0.05f),
                    cornerRadius = 8.dp,
                    elevation = 2.dp
                )
                .clickable { expanded = true }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isKurdish) selectedOption.toKurdishLabel() else selectedOption.name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.primary
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
                            fontSize = 11.sp,
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

@Composable
private fun BurnMetricRow(
    label: String,
    valueStr: String,
    isKurdish: Boolean
) {
    val neuColors = LocalNeumorphicColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = neuColors.textMuted
        )
        Text(
            text = valueStr,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = neuColors.textMain
        )
    }
}
