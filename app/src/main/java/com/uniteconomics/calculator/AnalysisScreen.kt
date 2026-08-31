package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Analysis & Machine screen composable providing organized input section cards with bi-directional sliders,
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
        val formatter = if (isDec) DECIMAL_FORMAT else INTEGER_FORMAT
        return synchronized(formatter) { formatter.format(num) }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val widthSizeClass = WindowWidthSizeClass.fromWidth(maxWidth)
        val isWideScreen = widthSizeClass != WindowWidthSizeClass.Compact
        val horizontalPadding = if (widthSizeClass == WindowWidthSizeClass.Expanded) 24.dp else 16.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Pinned Neumorphic Mini-KPI Header Bar
            MiniKpiHeaderBar(
                result = result,
                isKurdish = isKurdish,
                neuColors = neuColors,
                formatAmt = ::formatAmt,
                modifier = Modifier
                    .fillMaxWidth()
                    .displayCutoutPadding()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 4.dp)
            )

            if (isWideScreen) {
                val columnSpacing = if (widthSizeClass == WindowWidthSizeClass.Expanded) 24.dp else 16.dp
                val leftWeight = if (widthSizeClass == WindowWidthSizeClass.Expanded) 1.1f else 1f
                val rightWeight = if (widthSizeClass == WindowWidthSizeClass.Expanded) 0.9f else 1f

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .displayCutoutPadding()
                        .padding(horizontal = horizontalPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(columnSpacing)
                ) {
                    // Left Column: All Parameter Input Section Cards
                    LazyColumn(
                        modifier = Modifier
                            .weight(leftWeight)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item(key = "title_header") {
                            AnalysisHeader(
                                isKurdish = isKurdish,
                                neuColors = neuColors,
                                onResetDefaults = {
                                    onInputsChange(
                                        CalculatorInputs(
                                            netTimeframe = inputs.netTimeframe,
                                            revTimeframe = inputs.revTimeframe
                                        )
                                    )
                                }
                            )
                        }
                        item(key = "section_1") {
                            Section1Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                        }
                        item(key = "section_2") {
                            Section2Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                        }
                        item(key = "section_3a") {
                            Section3ACard(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                        }
                        item(key = "section_3b") {
                            Section3BCard(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                        }
                        item(key = "section_4") {
                            Section4Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                        }
                    }

                    // Right Column: Financial Breakdown & Risk Sensitivity Cards
                    LazyColumn(
                        modifier = Modifier
                            .weight(rightWeight)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item(key = "breakdown") {
                            BreakdownCard(
                                result = result,
                                revTimeframe = revTimeframe,
                                onRevTimeframeChange = onRevTimeframeChange,
                                isKurdish = isKurdish,
                                neuColors = neuColors,
                                formatAmt = ::formatAmt
                            )
                        }
                        item(key = "risk_sensitivity") {
                            RiskSensitivityCard(
                                inputs = inputs,
                                result = result,
                                isKurdish = isKurdish,
                                neuColors = neuColors,
                                formatAmt = ::formatAmt
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .displayCutoutPadding(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item(key = "title_header") {
                        AnalysisHeader(
                            isKurdish = isKurdish,
                            neuColors = neuColors,
                            onResetDefaults = {
                                onInputsChange(
                                    CalculatorInputs(
                                        netTimeframe = inputs.netTimeframe,
                                        revTimeframe = inputs.revTimeframe
                                    )
                                )
                            }
                        )
                    }
                    item(key = "section_1") {
                        Section1Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                    }
                    item(key = "section_2") {
                        Section2Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                    }
                    item(key = "section_3a") {
                        Section3ACard(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                    }
                    item(key = "section_3b") {
                        Section3BCard(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                    }
                    item(key = "section_4") {
                        Section4Card(inputs = inputs, onInputsChange = onInputsChange, isKurdish = isKurdish, onOpenDictionaryForTerm = onOpenDictionaryForTerm)
                    }
                    item(key = "breakdown") {
                        BreakdownCard(
                            result = result,
                            revTimeframe = revTimeframe,
                            onRevTimeframeChange = onRevTimeframeChange,
                            isKurdish = isKurdish,
                            neuColors = neuColors,
                            formatAmt = ::formatAmt
                        )
                    }
                    item(key = "risk_sensitivity") {
                        RiskSensitivityCard(
                            inputs = inputs,
                            result = result,
                            isKurdish = isKurdish,
                            neuColors = neuColors,
                            formatAmt = ::formatAmt
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniKpiHeaderBar(
    result: CalculationResult,
    isKurdish: Boolean,
    neuColors: NeumorphicColors,
    formatAmt: (Double, Boolean) -> String,
    modifier: Modifier = Modifier
) {
    val netProfitStr = if (result.isLoss) {
        "\u2066-${formatAmt(abs(result.netProfitTotal), false)}\u2069 IQD"
    } else {
        "${formatAmt(result.netProfitTotal, false)} IQD"
    }
    val dailyTargetStr = if (result.isLoss) {
        if (isKurdish) "ناتوانرێت بپێکرێت (زیان)" else "Unachievable (Loss)"
    } else {
        "${formatAmt(result.dailyTargetOrders, true)} ${if (isKurdish) "داواکاری" else "Orders"}"
    }

    Box(
        modifier = modifier
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 14.dp,
                elevation = 3.dp
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Net Profit Item
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (result.isLoss) neuColors.loss else neuColors.success)
                )
                Column {
                    Text(
                        text = if (isKurdish) "قازانجی پوختە" else "Net Profit",
                        fontSize = 11.sp,
                        color = neuColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = netProfitStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isLoss) neuColors.loss else neuColors.success
                    )
                }
            }

            // Target Daily Orders Item
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (result.isLoss) neuColors.loss else neuColors.info)
                )
                Column {
                    Text(
                        text = if (isKurdish) "ئامانجی ڕۆژانە" else "Target Daily Orders",
                        fontSize = 11.sp,
                        color = neuColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dailyTargetStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isLoss) neuColors.loss else neuColors.textMain
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisHeader(
    isKurdish: Boolean,
    neuColors: NeumorphicColors,
    onResetDefaults: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = if (isKurdish) "مەکینەی شیکاری و پارامەتەرەکان" else "Analysis & Inputs Engine",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = neuColors.textMain,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = if (isKurdish) "دەستکاری ١٩ پارامەتەر بە سلایدەر" else "Tune 19 financial variables in real-time",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = neuColors.textMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .then(
                    if (isPressed) {
                        Modifier.neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 10.dp,
                            elevation = 2.dp
                        )
                    } else {
                        Modifier.neuFlat(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 10.dp,
                            elevation = 3.dp
                        )
                    }
                )
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    role = Role.Button,
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = neuColors.primary)
                ) { showResetConfirmation = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = if (isKurdish) "گەڕاندنەوە بۆ باری بنەڕەتی" else "Reset to Defaults",
                    tint = neuColors.textMain,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isKurdish) "باری بنەڕەتی" else "Reset",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMain
                )
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = {
                Text(
                    text = if (isKurdish) "گەڕاندنەوە بۆ باری بنەڕەتی" else "Reset to Defaults",
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMain
                )
            },
            text = {
                Text(
                    text = if (isKurdish)
                        "دڵنیایت لە گەڕاندنەوەی سەرجەم ١٩ پارامەتەرەکە بۆ باری بنەڕەتی؟ ئەم کارە هەموو دەستکارییەکان دەسڕێتەوە."
                    else
                        "Are you sure you want to reset all 19 parameters to their default values? This action cannot be undone.",
                    color = neuColors.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetConfirmation = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = if (isKurdish) "بەڵێ، بگەڕێنەوە" else "Reset",
                        color = neuColors.loss,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmation = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = if (isKurdish) "پاشگەزبوونەوە" else "Cancel",
                        color = neuColors.textMuted
                    )
                }
            },
            containerColor = neuColors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun Section1Card(
    inputs: CalculatorInputs,
    onInputsChange: (CalculatorInputs) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit
) {
    SectionCard(
        headerText = if (isKurdish) "داهات و تێچووی بەرهەم" else "Revenue & Product Cost",
        isKurdish = isKurdish
    ) {
        SliderInputRow(
            label = if (isKurdish) "نرخی فرۆشتن بە کڕیار" else "Retail Price",
            value = inputs.effRetailPrice,
            onValueChange = { newR -> onInputsChange(inputs.copy(retailPrice = newR)) },
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
            onValueChange = { newS -> onInputsChange(inputs.copy(sourcingCost = newS)) },
            valueRange = 500f..200000f,
            step = 250.0,
            unit = "IQD",
            isMarked = true,
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "تێچووی گەیشتنی بەرهەم" else "Sourcing Cost") }
        )

        SliderInputRow(
            label = if (isKurdish) "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" else "Basket Size / Items per Order (UPT)",
            value = inputs.effAovMultiplier,
            onValueChange = { newAov -> onInputsChange(inputs.copy(aovMultiplier = newAov)) },
            valueRange = 0.5f..10f,
            step = 0.1,
            unit = if (isKurdish) "دانە" else "Units",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" else "Basket Size / Items per Order (UPT)") }
        )

        SliderInputRow(
            label = if (isKurdish) "داشکاندنی پێدراو (%)" else "Discount Rate (%)",
            value = inputs.effDiscounts,
            onValueChange = { newDisc -> onInputsChange(inputs.copy(discounts = newDisc)) },
            valueRange = 0f..50f,
            step = 1.0,
            unit = "%",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "داشکاندنی پێدراو (%)" else "Discounts (%)") }
        )
    }
}

@Composable
private fun Section2Card(
    inputs: CalculatorInputs,
    onInputsChange: (CalculatorInputs) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit
) {
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
            onValueChange = { newLtv -> onInputsChange(inputs.copy(ltvMultiplier = newLtv)) },
            valueRange = 1.0f..5.0f,
            step = 0.1,
            unit = "x",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی دووبارە کڕینەوە (LTV)" else "Repeat Purchase Rate (LTV)") }
        )
    }
}

@Composable
private fun Section3ACard(
    inputs: CalculatorInputs,
    onInputsChange: (CalculatorInputs) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit
) {
    SectionCard(
        headerText = if (isKurdish) "گواستنەوە و جێبەجێکردن" else "Logistics & Fulfillment",
        isKurdish = isKurdish
    ) {
        SliderInputRow(
            label = if (isKurdish) "کرێی گواستنەوە" else "Courier Delivery Fee",
            value = inputs.effShippingCost,
            onValueChange = { newShip -> onInputsChange(inputs.copy(shippingCost = newShip)) },
            valueRange = 0f..25000f,
            step = 250.0,
            unit = "IQD",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "کرێی گواستنەوە" else "Shipping Cost") }
        )

        SliderInputRow(
            label = if (isKurdish) "تێچووی پێچانەوە و گەیاندن" else "Packaging & Handling Ops",
            value = inputs.effOpsCost,
            onValueChange = { newOps -> onInputsChange(inputs.copy(opsCost = newOps)) },
            valueRange = 0f..10000f,
            step = 100.0,
            unit = "IQD",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "تێچووی پێچانەوە و گەیاندن" else "Packaging & Handling") }
        )

        SliderInputRow(
            label = if (isKurdish) "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" else "RTS Rejection Rate (%)",
            value = inputs.effRejectionRate,
            onValueChange = { newRej -> onInputsChange(inputs.copy(rejectionRate = newRej)) },
            valueRange = 0f..80f,
            step = 0.5,
            unit = "%",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" else "Rejection Rate (RTS %)") }
        )

        SliderInputRow(
            label = if (isKurdish) "سزای دارایی داواکارییە ڕەتکراوەکان" else "RTS Return Penalty Fee",
            value = inputs.effRtsShippingFee,
            onValueChange = { newRts -> onInputsChange(inputs.copy(rtsShippingFee = newRts)) },
            valueRange = 0f..25000f,
            step = 250.0,
            unit = "IQD",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "سزای دارایی داواکارییە ڕەتکراوەکان" else "RTS Penalty Fee") }
        )
    }
}

@Composable
private fun Section3BCard(
    inputs: CalculatorInputs,
    onInputsChange: (CalculatorInputs) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit
) {
    SectionCard(
        headerText = if (isKurdish) "مەترسییەکانی کۆگا و زیانەکان" else "Inventory Risk & Loss Rates",
        isKurdish = isKurdish
    ) {
        SliderInputRow(
            label = if (isKurdish) "ڕێژەی پارە گەڕاندنەوە" else "Customer Refund Rate (%)",
            value = inputs.effRefundRate,
            onValueChange = { newRef -> onInputsChange(inputs.copy(refundRate = newRef)) },
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
            onValueChange = { newDam -> onInputsChange(inputs.copy(damageRate = newDam)) },
            valueRange = 0f..30f,
            step = 0.5,
            unit = "%",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی زیان/لەناوچوون" else "Damage/Loss Rate (%)") }
        )

        SliderInputRow(
            label = if (isKurdish) "ڕێژەی کاڵای نەفرۆشراو" else "Deadstock Loss Rate (%)",
            value = inputs.effDeadstockRate,
            onValueChange = { newDead -> onInputsChange(inputs.copy(deadstockRate = newDead)) },
            valueRange = 0f..30f,
            step = 0.5,
            unit = "%",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ڕێژەی کاڵای نەفرۆشراو" else "Deadstock Rate (%)") }
        )
    }
}

@Composable
private fun Section4Card(
    inputs: CalculatorInputs,
    onInputsChange: (CalculatorInputs) -> Unit,
    isKurdish: Boolean,
    onOpenDictionaryForTerm: (String) -> Unit
) {
    SectionCard(
        headerText = if (isKurdish) "پێکهاتەی کار و ئامانجەکان" else "Overhead & Campaign Goals",
        isKurdish = isKurdish
    ) {
        SliderInputRow(
            label = if (isKurdish) "خەرجییە جێگیرەکانی مانگانە" else "Fixed Monthly Overhead (Rent/Salaries)",
            value = inputs.effFixedMonthlyExpenses,
            onValueChange = { newFix -> onInputsChange(inputs.copy(fixedMonthlyExpenses = newFix)) },
            valueRange = 0f..10000000f,
            step = 10000.0,
            unit = "IQD",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "خەرجییە جێگیرەکانی مانگانە" else "Fixed Monthly Overhead (Rent/Salaries)") }
        )

        SliderInputRow(
            label = if (isKurdish) "ئامانجی قازانجی پوختە" else "Target Profit Goal",
            value = inputs.effTargetProfitGoal,
            onValueChange = { newGoal -> onInputsChange(inputs.copy(targetProfitGoal = newGoal)) },
            valueRange = 0f..50000000f,
            step = 50000.0,
            unit = "IQD",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ئامانجی قازانجی پوختە" else "Net Profit Target") }
        )

        SliderInputRow(
            label = if (isKurdish) "ماوەی کات (بە ڕۆژ)" else "Campaign Horizon (Days)",
            value = inputs.effProjectDurationDays,
            onValueChange = { newDays -> onInputsChange(inputs.copy(projectDurationDays = newDays)) },
            valueRange = 1f..365f,
            step = 1.0,
            unit = if (isKurdish) "ڕۆژ" else "Days",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "ماوەی کات (بە ڕۆژ)" else "Time Horizon (Days)") }
        )

        SliderInputRow(
            label = if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" else "Remittance Payout Delay (Days)",
            value = inputs.effCapitalRemittanceFrequency,
            onValueChange = { newRem -> onInputsChange(inputs.copy(capitalRemittanceFrequency = newRem)) },
            valueRange = 1f..60f,
            step = 1.0,
            unit = if (isKurdish) "ڕۆژ" else "Days",
            isKurdish = isKurdish,
            onInfoClick = { onOpenDictionaryForTerm(if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" else "Remittance Payout Delay (Days)") }
        )
    }
}

@Composable
private fun BreakdownCard(
    result: CalculationResult,
    revTimeframe: TimeframeOption,
    onRevTimeframeChange: (TimeframeOption) -> Unit,
    isKurdish: Boolean,
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
                        contentDescription = null,
                        tint = neuColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isKurdish) "شیکردنەوەی دارایی گشتی" else "Detailed Financial Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textMain,
                        modifier = Modifier.semantics { heading() }
                    )
                }

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
                valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.displayRevenue, false)} IQD",
                valueColor = neuColors.warning
            )

            BreakdownRowCard(
                index = 1,
                label = if (isKurdish) "ئامانجی داواکارییە سەرکەوتووەکان" else "Target Successful Orders",
                valueStr = if (result.isLoss) "0" else "${formatAmt(result.targetUnits.toDouble(), false)} ${if (isKurdish) "دانە" else "Orders"}"
            )

            BreakdownRowCard(
                index = 2,
                label = if (isKurdish) "قازانجی گشتی لە هەر داواکارییەک" else "Gross Profit / Order",
                valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.grossPerSuccess, false)} IQD"
            )

            BreakdownRowCard(
                index = 3,
                label = if (isKurdish) "خاڵی یەکسانبوونەوە (داواکارییەکان)" else "Breakeven Orders",
                valueStr = if (result.isLoss) "N/A" else "${formatAmt(result.breakEvenUnits.toDouble(), false)} ${if (isKurdish) "دانە" else "Orders"}"
            )

            BreakdownRowCard(
                index = 4,
                label = if (isKurdish) "کۆی گشتی داواکارییە نێردراوەکان" else "Total Dispatched Orders",
                valueStr = if (result.isLoss) "0" else "${formatAmt(result.totalSent.toDouble(), false)} ${if (isKurdish) "دانە" else "Dispatches"}"
            )

            BreakdownRowCard(
                index = 5,
                label = if (isKurdish) "کۆی گشتی خەرجییەکانی پڕۆژە" else "Total Project Expenses",
                valueStr = if (result.isLoss) (if (isKurdish) "زیان" else "Loss") else "${formatAmt(result.totalExpenses, false)} IQD",
                valueColor = neuColors.purple
            )
        }
    }
}

@Composable
private fun RiskSensitivityCard(
    inputs: CalculatorInputs,
    result: CalculationResult,
    isKurdish: Boolean,
    neuColors: NeumorphicColors,
    formatAmt: (Double, Boolean) -> String
) {
    val rtsLossAmount = remember(inputs, result) {
        val totalSent = result.totalSent.toDouble()
        val rejectDec = inputs.effRejectionRate / 100.0
        totalSent * (inputs.effShippingCost + inputs.effOpsCost + inputs.effRtsShippingFee) * rejectDec
    }
    val deadstockLossAmount = remember(inputs, result) {
        val totalSent = result.totalDispatchedOrders.toDouble()
        val cogs = inputs.effSourcingCost * inputs.effAovMultiplier
        val rejectDec = inputs.effRejectionRate / 100.0
        val rawRefundDec = inputs.effRefundRate / 100.0
        val successDec = kotlin.math.max(0.001, (1.0 - rejectDec) * (1.0 - rawRefundDec))
        val damageDec = inputs.effDamageRate / 100.0
        val deadDec = (inputs.effDeadstockRate / 100.0).coerceIn(0.0, 0.999)
        val consumedProb = kotlin.math.min(1.0, successDec + damageDec)
        totalSent * consumedProb * cogs * (deadDec / (1.0 - deadDec))
    }
    val floatAmount = remember(result) {
        result.workingCapitalFloat
    }

    val riskEntries = remember(rtsLossAmount, deadstockLossAmount, floatAmount, isKurdish, inputs) {
        val items = listOf(
            Triple(if (isKurdish) "تێچووی زیانی گەیاندنەکانی RTS" else "Estimated RTS Failure Loss", rtsLossAmount, "${formatAmt(rtsLossAmount, false)} IQD"),
            Triple(if (isKurdish) "خەمڵاندنی زیانی کاڵای نەفرۆشراو" else "Deadstock Capital Exposure", deadstockLossAmount, "${formatAmt(deadstockLossAmount, false)} IQD"),
            Triple(if (isKurdish) "دواکەوتنی گەڕانەوەی پارە (${inputs.effCapitalRemittanceFrequency.toInt()} ڕۆژ)" else "Remittance Delay Float Impact", floatAmount, "${formatAmt(floatAmount, false)} IQD")
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
                    color = neuColors.textMain,
                    modifier = Modifier.semantics { heading() }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) { },
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

@Composable
private fun SectionCard(
    headerText: String,
    isKurdish: Boolean,
    defaultExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    var isExpanded by remember { mutableStateOf(defaultExpanded) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .semantics {
                        stateDescription = if (isExpanded) {
                            if (isKurdish) "کراوەتەوە" else "Expanded"
                        } else {
                            if (isKurdish) "داخراوە" else "Collapsed"
                        }
                    }
                    .clickable(role = Role.Button) { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMain,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            lightGradientColor = neuColors.shadowLight.copy(alpha = 0.3f),
                            darkGradientColor = neuColors.shadowDark.copy(alpha = 0.1f),
                            cornerRadius = 16.dp,
                            elevation = 1.dp
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) {
                            if (isKurdish) "داخستنی بەش" else "Collapse section"
                        } else {
                            if (isKurdish) "کردنەوەی بەش" else "Expand section"
                        },
                        tint = neuColors.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(thickness = 1.dp, color = neuColors.border)
                    content()
                }
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { },
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
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .neuConvex(
                    lightShadowColor = neuColors.shadowLight,
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    lightGradientColor = neuColors.warning.copy(alpha = 0.15f),
                    darkGradientColor = neuColors.warning.copy(alpha = 0.05f),
                    cornerRadius = 8.dp,
                    elevation = 2.dp
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isKurdish) selectedOption.toKurdishLabel() else selectedOption.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.em,
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
