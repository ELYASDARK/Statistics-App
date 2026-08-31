package com.uniteconomics.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Dashboard screen composable featuring Executive KPI tiles, Live Canvas Valuation Line Chart,
 * Neumorphic Canvas Revenue Growth Bar Chart, Capital Burn Summary, and Strategy Recommendation.
 */
@Composable
fun DashboardScreen(
    result: CalculationResult,
    inputs: CalculatorInputs,
    onInputsChange: ((CalculatorInputs) -> Unit)? = null,
    netTimeframe: TimeframeOption,
    onNetTimeframeChange: (TimeframeOption) -> Unit,
    isKurdish: Boolean = true,
    onOpenDictionaryForTerm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    fun formatAmt(num: Double, isDec: Boolean = false): String {
        if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
        val formatter = if (isDec) DECIMAL_FORMAT else INTEGER_FORMAT
        return synchronized(formatter) { formatter.format(num) }
    }

    val showDiagnosticAdvice = result.isLoss || result.lossType == LossType.LOW_MARGIN

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthSizeClass = WindowWidthSizeClass.fromWidth(maxWidth)
        val isWideScreen = widthSizeClass != WindowWidthSizeClass.Compact

        if (isWideScreen) {
            val horizontalPadding = if (widthSizeClass == WindowWidthSizeClass.Expanded) 24.dp else 16.dp
            val columnSpacing = if (widthSizeClass == WindowWidthSizeClass.Expanded) 24.dp else 16.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .padding(horizontal = horizontalPadding, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(columnSpacing)
            ) {
                // Left Column: Header, Executive KPI Grid & Burn Summary
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item(key = "header") {
                        HeaderSection(isKurdish = isKurdish, neuColors = neuColors)
                    }
                    item(key = "kpi_tiles") {
                        KpiTilesGrid(
                            result = result,
                            inputs = inputs,
                            netTimeframe = netTimeframe,
                            onNetTimeframeChange = onNetTimeframeChange,
                            isKurdish = isKurdish,
                            onOpenDictionaryForTerm = onOpenDictionaryForTerm,
                            neuColors = neuColors,
                            formatAmt = ::formatAmt
                        )
                    }
                    item(key = "burn_summary") {
                        BurnSummaryCard(
                            result = result,
                            isKurdish = isKurdish,
                            onOpenDictionaryForTerm = onOpenDictionaryForTerm,
                            neuColors = neuColors,
                            formatAmt = ::formatAmt
                        )
                    }
                    if (showDiagnosticAdvice) {
                        item(key = "diagnostic_advice") {
                            DiagnosticAdviceCard(
                                inputs = inputs,
                                result = result,
                                onApplyInputs = onInputsChange,
                                isKurdish = isKurdish,
                                neuColors = neuColors
                            )
                        }
                    }
                }

                // Right Column: Canvas Valuation Line Chart & Revenue Growth Bar Chart
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item(key = "valuation_chart") {
                        ValuationLineChart(
                            result = result,
                            isKurdish = isKurdish
                        )
                    }
                    item(key = "revenue_growth_chart") {
                        RevenueGrowthBarChart(
                            result = result,
                            isKurdish = isKurdish
                        )
                    }
                }
            }
        } else {
            // Compact Phone: Single Column Vertical Flow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderSection(isKurdish = isKurdish, neuColors = neuColors)

                KpiTilesGrid(
                    result = result,
                    inputs = inputs,
                    netTimeframe = netTimeframe,
                    onNetTimeframeChange = onNetTimeframeChange,
                    isKurdish = isKurdish,
                    onOpenDictionaryForTerm = onOpenDictionaryForTerm,
                    neuColors = neuColors,
                    formatAmt = ::formatAmt
                )

                ValuationLineChart(
                    result = result,
                    isKurdish = isKurdish
                )

                RevenueGrowthBarChart(
                    result = result,
                    isKurdish = isKurdish
                )

                BurnSummaryCard(
                    result = result,
                    isKurdish = isKurdish,
                    onOpenDictionaryForTerm = onOpenDictionaryForTerm,
                    neuColors = neuColors,
                    formatAmt = ::formatAmt
                )

                if (showDiagnosticAdvice) {
                    DiagnosticAdviceCard(
                        inputs = inputs,
                        result = result,
                        onApplyInputs = onInputsChange,
                        isKurdish = isKurdish,
                        neuColors = neuColors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(isKurdish: Boolean, neuColors: NeumorphicColors) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isKurdish) "داشبۆردی بەڕێوەبردن" else "Executive Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    color = neuColors.textMain,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = if (isKurdish) "پوختەی KPI و هێڵە داراییەکان" else "Key Performance Indicators & Valuation",
                    style = MaterialTheme.typography.bodySmall,
                    color = neuColors.textMuted
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = neuColors.border)
    }
}

@Composable
private fun KpiTilesGrid(
    result: CalculationResult,
    inputs: CalculatorInputs,
    netTimeframe: TimeframeOption,
    onNetTimeframeChange: (TimeframeOption) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit,
    neuColors: NeumorphicColors,
    formatAmt: (Double, Boolean) -> String
) {
    val isGrossLoss = result.lossType == LossType.GROSS_LOSS
    val isNetLoss = result.isLoss

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile 1: Net / Product
            val netProductStr = if (isGrossLoss || result.netProfitPerProduct < 0) {
                "\u2066-${formatAmt(abs(result.netProfitPerProduct), false)} IQD\u2069"
            } else {
                "${formatAmt(result.netProfitPerProduct, false)} IQD"
            }

            KpiTile(
                title = if (isKurdish) "قازانج / بەرهەم" else "Net / Product",
                valueStr = netProductStr,
                subTitle = if (isGrossLoss) (if (isKurdish) "زیانی گشتی یەکە" else "Unit Gross Deficit") else (if (isKurdish) "قازانجی پوختەی یەکە" else "Net Profit / Unit"),
                isLoss = result.netProfitPerProduct < 0 || isGrossLoss,
                accentColor = neuColors.success,
                term = if (isKurdish) "قازانجی پوختەی هەر بەرهەمێک" else "Net Profit Per Product",
                onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "قازانجی پوختەی هەر بەرهەمێک" else "Net Profit Per Product") },
                isKurdish = isKurdish,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Tile 2: Net Profit
            val netProfitStr = if (isNetLoss) {
                val suffix = when (netTimeframe) {
                    TimeframeOption.DAILY -> if (isKurdish) "/ڕۆژ" else "/day"
                    TimeframeOption.WEEKLY -> if (isKurdish) "/هەفتە" else "/wk"
                    TimeframeOption.MONTHLY -> if (isKurdish) "/مانگ" else "/mo"
                    TimeframeOption.TOTAL -> ""
                }
                "\u2066-${formatAmt(abs(result.displayNetProfit), false)} IQD$suffix\u2069"
            } else {
                val suffix = when (netTimeframe) {
                    TimeframeOption.DAILY -> if (isKurdish) "/ڕۆژ" else "/day"
                    TimeframeOption.WEEKLY -> if (isKurdish) "/هەفتە" else "/wk"
                    TimeframeOption.MONTHLY -> if (isKurdish) "/مانگ" else "/mo"
                    TimeframeOption.TOTAL -> ""
                }
                "${formatAmt(result.displayNetProfit, false)} IQD$suffix"
            }

            KpiTile(
                title = if (isKurdish) "کۆی قازانج" else "Net Profit",
                valueStr = netProfitStr,
                subTitle = if (isNetLoss) (if (isKurdish) "کورتهێنانی چاوەڕوانکراو" else "Projected Deficit") else (if (isKurdish) "کۆی قازانجی چاوەڕوانکراو" else "Projected Net Profit"),
                isLoss = isNetLoss,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tile 3: Daily Target
            val (dailyTargetStr, dailyTargetSub) = when {
                isGrossLoss -> Pair(
                    if (isKurdish) "ناگات" else "Unachievable",
                    if (isKurdish) "سەرەتا نرخی یەکە چاک بکە" else "Fix unit price first"
                )
                result.lossType == LossType.OVERHEAD_DEFICIT -> {
                    val dailyBe = if (inputs.projectDurationDays > 0) result.breakevenOrders.toDouble() / inputs.projectDurationDays else 0.0
                    Pair(
                        "${formatAmt(dailyBe, true)} ${if (isKurdish) "داواکاری" else "Orders"}",
                        if (isKurdish) "ئامانجی یەکسانبوونەوە" else "Break-even Target"
                    )
                }
                else -> Pair(
                    "${formatAmt(result.dailyTargetOrders, true)} ${if (isKurdish) "داواکاری" else "Orders"}",
                    if (isKurdish) "ئامانجی فرۆشتن" else "Target Velocity"
                )
            }

            KpiTile(
                title = if (isKurdish) "ئامانجی ڕۆژانە" else "Daily Target",
                valueStr = dailyTargetStr,
                subTitle = dailyTargetSub,
                isLoss = isGrossLoss,
                accentColor = if (result.lossType == LossType.OVERHEAD_DEFICIT) neuColors.warning else neuColors.info,
                term = if (isKurdish) "ئامانجی داواکارییەکانی ڕۆژانە" else "Target Daily Orders",
                onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ئامانجی داواکارییەکانی ڕۆژانە" else "Target Daily Orders") },
                isKurdish = isKurdish,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Tile 4: Daily Burn
            KpiTile(
                title = if (isKurdish) "خەرجی ڕۆژانە" else "Daily Burn",
                valueStr = "${formatAmt(result.dailyBurn, false)} IQD",
                subTitle = if (isGrossLoss) (if (isKurdish) "خەرجی جێگیری بنەڕەت" else "Baseline Fixed Burn") else (if (isKurdish) "سوتانی ڕۆژانە" else "Burn Rate"),
                isLoss = false,
                accentColor = neuColors.warning,
                term = if (isKurdish) "خەرجییە ڕۆژانەییەکان" else "Daily Cash Burn",
                onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "خەرجییە ڕۆژانەییەکان" else "Daily Cash Burn") },
                isKurdish = isKurdish,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun BurnSummaryCard(
    result: CalculationResult,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit,
    neuColors: NeumorphicColors,
    formatAmt: (Double, Boolean) -> String
) {
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
                    color = neuColors.textMain,
                    modifier = Modifier.semantics { heading() }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = neuColors.border)

            BurnMetricRow(
                label = if (isKurdish) "خەرجییەکانی ڕۆژانە" else "Daily Run Rate",
                valueStr = "${formatAmt(result.burnRateDay, false)} IQD",
                isKurdish = isKurdish
            )
            BurnMetricRow(
                label = if (isKurdish) "خەرجییەکانی هەفتانە" else "Weekly Run Rate",
                valueStr = "${formatAmt(result.burnRateWeek, false)} IQD",
                isKurdish = isKurdish
            )
            BurnMetricRow(
                label = if (isKurdish) "خەرجییەکانی مانگانە" else "Monthly Run Rate",
                valueStr = "${formatAmt(result.burnRateMonth, false)} IQD",
                isKurdish = isKurdish
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = if (isKurdish) "سەرمایەی کارپێکردنی پێویست (Float)" else "Working Capital Float",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.purple
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(role = Role.Button) {
                                    onOpenDictionaryForTerm(if (isKurdish) "سەرمایەی کارپێکردنی پێویست" else "Working Capital Float")
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .neuConvex(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        lightGradientColor = neuColors.purple.copy(alpha = 0.25f),
                                        darkGradientColor = neuColors.purple.copy(alpha = 0.08f),
                                        cornerRadius = 11.dp,
                                        elevation = 1.dp
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = if (isKurdish) "زانیاری لەسەر سەرمایەی کارپێکردن" else "Working Capital Float Info",
                                    tint = neuColors.purple,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${formatAmt(result.workingCapitalFloat, false)} IQD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.purple
                    )
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

    // Dynamically scale font size based on string length to guarantee 0% clipping
    val valueFontSize = when {
        valueStr.length > 16 -> 15.sp
        valueStr.length > 12 -> 17.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) { }
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
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Header Row: Resilient Title + Compact Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMuted,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

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
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { onInfoClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(22.dp)
                                .neuConvex(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    lightGradientColor = neuColors.shadowLight.copy(alpha = 0.4f),
                                    darkGradientColor = neuColors.shadowDark.copy(alpha = 0.2f),
                                    cornerRadius = 11.dp,
                                    elevation = 1.dp
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = if (isKurdish) "زانیاری لەسەر $title" else "Information about $title",
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Value & Subtitle Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = valueStr,
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (isLoss) neuColors.loss else accentColor,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!subTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = neuColors.textMuted,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(28.dp)
                .neuConvex(
                    lightShadowColor = neuColors.shadowLight,
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    lightGradientColor = neuColors.primary.copy(alpha = 0.15f),
                    darkGradientColor = neuColors.primary.copy(alpha = 0.05f),
                    cornerRadius = 6.dp,
                    elevation = 2.dp
                )
                .clip(RoundedCornerShape(6.dp))
                .clickable(role = Role.Button) { expanded = true }
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isKurdish) selectedOption.toKurdishLabel() else selectedOption.name,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.em,
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
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { },
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

@Composable
private fun DiagnosticAdviceCard(
    inputs: CalculatorInputs,
    result: CalculationResult,
    onApplyInputs: ((CalculatorInputs) -> Unit)? = null,
    isKurdish: Boolean,
    neuColors: NeumorphicColors
) {
    val diag = result.diagnostics
    val indicatorColor = when (diag.lossType) {
        LossType.GROSS_LOSS -> neuColors.loss
        LossType.OVERHEAD_DEFICIT -> neuColors.warning
        LossType.LOW_MARGIN -> neuColors.warning
        LossType.HEALTHY -> neuColors.success
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = if (diag.lossType == LossType.GROSS_LOSS || diag.lossType == LossType.OVERHEAD_DEFICIT) {
                        if (isKurdish) "دەستنیشانکردنی هۆکار و دەرفەتەکانی قازانج" else "Loss Diagnostics & 1-Tap Levers"
                    } else {
                        if (isKurdish) "ئامۆژگاری و باشترکردنی قازانج" else "Profit Optimization Insights"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMain,
                    modifier = Modifier.semantics { heading() }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = neuColors.border)

            // Diagnostic Summary Text
            val summaryText = when (diag.lossType) {
                LossType.GROSS_LOSS -> if (isKurdish)
                    "یەکەکانی بەرهەم بە زیان دەفرۆشرێن (${INTEGER_FORMAT.format(diag.pureUnitMargin)} IQD مەودای پوخت). پێویستە نرخی فرۆشتن بەرز بکرێتەوە یان خەرجی ڕیکلام و گواستنەوە سنووردار بکرێت."
                else
                    "Negative unit economics detected (${INTEGER_FORMAT.format(diag.pureUnitMargin)} IQD unit margin). Selling additional units increases losses. Raise retail price or cap acquisition costs."

                LossType.OVERHEAD_DEFICIT -> if (isKurdish)
                    "مەودای یەکە قازانجبەخشە، بەڵام قەبارەی فرۆشتن بەشی خەرجییە جێگیرەکان ناکات. خاڵی یەکسانبوونەوە: ${result.breakevenOrders} داواکاری."
                else
                    "Positive unit economics, but current volume does not cover fixed overhead. Break-even requirement: ${result.breakevenOrders} total orders."

                LossType.LOW_MARGIN -> if (isKurdish)
                    "مەودای قازانجی پوختە ناسکە (<10٪)؛ پڕۆژەکە مەترسی زیانی هەیە لە کاتی گۆڕانی نرخی ڕیکلام یان ڕەتکردنەوە."
                else
                    "Thin net margin (<10%); fragile to slight spikes in CAC or RTS courier returns."

                LossType.HEALTHY -> if (isKurdish)
                    "پێکهاتەی دارایی تەندروستە و مەودای قازانج لە ئاستێکی باش دایە."
                else
                    "Unit economics and margin structure are healthy."
            }

            Text(
                text = summaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = neuColors.textMuted
            )

            // 1-Tap Actionable Simulation Levers
            if (diag.lossType == LossType.GROSS_LOSS || diag.lossType == LossType.LOW_MARGIN || diag.lossType == LossType.OVERHEAD_DEFICIT) {
                Text(
                    text = if (isKurdish) "کردارە خێراکان بۆ گۆڕین بۆ قازانج:" else "1-Tap Optimization Levers:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMain
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Lever 1: Break-even Selling Price
                    if (diag.breakEvenRetailPrice > inputs.retailPrice) {
                        OptimizationActionChip(
                            title = if (isKurdish) "بەرزکردنەوەی نرخ بۆ خاڵی یەکسانبوونەوە" else "Raise to Break-even Price",
                            actionLabel = "${INTEGER_FORMAT.format(diag.breakEvenRetailPrice)} IQD",
                            deltaLabel = "+${INTEGER_FORMAT.format(diag.breakEvenRetailPrice - inputs.retailPrice)} IQD",
                            onClick = {
                                onApplyInputs?.invoke(inputs.copy(retailPrice = diag.breakEvenRetailPrice))
                            },
                            neuColors = neuColors,
                            isKurdish = isKurdish
                        )
                    }

                    // Lever 2: Target 15% Net Margin Price
                    if (diag.target15MarginPrice > inputs.retailPrice) {
                        OptimizationActionChip(
                            title = if (isKurdish) "نرخی ئامانج بۆ ١٥٪ قازانجی سافی" else "Target 15% Net Margin Price",
                            actionLabel = "${INTEGER_FORMAT.format(diag.target15MarginPrice)} IQD",
                            deltaLabel = "+${INTEGER_FORMAT.format(diag.target15MarginPrice - inputs.retailPrice)} IQD",
                            onClick = {
                                onApplyInputs?.invoke(inputs.copy(retailPrice = diag.target15MarginPrice))
                            },
                            neuColors = neuColors,
                            isKurdish = isKurdish
                        )
                    }

                    // Lever 3: Max Allowable CAC
                    if (diag.maxAllowableCac in 500.0..inputs.cac) {
                        OptimizationActionChip(
                            title = if (isKurdish) "داخستنی تێچووی CAC بۆ ئاستی یەکسانبوونەوە" else "Cap CAC to Break-even Level",
                            actionLabel = "${INTEGER_FORMAT.format(diag.maxAllowableCac)} IQD",
                            deltaLabel = "-${INTEGER_FORMAT.format(inputs.cac - diag.maxAllowableCac)} IQD",
                            onClick = {
                                onApplyInputs?.invoke(inputs.copy(cac = diag.maxAllowableCac))
                            },
                            neuColors = neuColors,
                            isKurdish = isKurdish
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimizationActionChip(
    title: String,
    actionLabel: String,
    deltaLabel: String,
    onClick: () -> Unit,
    neuColors: NeumorphicColors,
    isKurdish: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuConvex(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                lightGradientColor = neuColors.primary.copy(alpha = 0.12f),
                darkGradientColor = neuColors.primary.copy(alpha = 0.04f),
                cornerRadius = 10.dp,
                elevation = 2.dp
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMain
                )
                Text(
                    text = deltaLabel,
                    fontSize = 10.sp,
                    color = neuColors.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 6.dp,
                        elevation = 2.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.primary
                )
            }
        }
    }
}

