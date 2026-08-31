package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * Custom Canvas bar chart drawing Neumorphic concave revenue growth bars
 * with interactive touch/drag inspection showing exact monthly gain or loss.
 */
@Composable
fun RevenueGrowthBarChart(
    result: CalculationResult,
    isKurdish: Boolean = true,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val haptic = LocalHapticFeedback.current
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    fun formatAmt(num: Double): String {
        if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
        return synchronized(INTEGER_FORMAT) { INTEGER_FORMAT.format(num) }
    }

    val context = LocalContext.current
    val isReduceMotion = remember(context) {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }

    // Animated scale progress on initial load (honors system Reduce Motion)
    var animationTrigger by remember { mutableStateOf(if (isReduceMotion) 1f else 0f) }
    LaunchedEffect(isReduceMotion) {
        animationTrigger = 1f
    }

    val animProgressRaw by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "BarChartHeightAnimation"
    )
    val animProgress = if (isReduceMotion) 1f else animProgressRaw

    var selectedTimeframe by remember { mutableStateOf(TimeframeOption.MONTHLY) }

    // Calculate metric bars representing revenue scale with robust loss & profit math across timeframes
    val barValues = remember(result, selectedTimeframe) {
        when (selectedTimeframe) {
            TimeframeOption.DAILY -> {
                val baseDaily = if (result.revenue > 0.0) result.revenue / 30.0 else 5000.0
                if (result.isLoss) {
                    listOf(
                        1.30 * baseDaily,
                        1.15 * baseDaily,
                        1.00 * baseDaily,
                        0.85 * baseDaily,
                        0.70 * baseDaily,
                        0.55 * baseDaily,
                        0.40 * baseDaily
                    )
                } else {
                    val baseProfit = max(0.0, result.netProfitTotal / 30.0)
                    val growthScale = if (result.revenue > 0) (baseProfit / (result.revenue / 30.0)).coerceIn(0.02, 0.15) else 0.04
                    val growthFactor = 1.0 + growthScale
                    (0..6).map { i -> 0.70 * baseDaily * Math.pow(growthFactor, i.toDouble()) }
                }
            }
            TimeframeOption.WEEKLY -> {
                val baseWeekly = if (result.revenue > 0.0) (result.revenue / 30.0) * 7.0 else 35000.0
                if (result.isLoss) {
                    listOf(
                        1.30 * baseWeekly,
                        1.00 * baseWeekly,
                        0.70 * baseWeekly,
                        0.40 * baseWeekly
                    )
                } else {
                    val baseProfit = max(0.0, (result.netProfitTotal / 30.0) * 7.0)
                    val growthScale = if (result.revenue > 0) (baseProfit / baseWeekly).coerceIn(0.05, 0.25) else 0.08
                    val growthFactor = 1.0 + growthScale
                    (0..3).map { i -> 0.65 * baseWeekly * Math.pow(growthFactor, i.toDouble()) }
                }
            }
            TimeframeOption.MONTHLY, TimeframeOption.TOTAL -> {
                val baseRev = if (result.revenue > 0.0) result.revenue else 100.0
                if (result.isLoss) {
                    listOf(
                        1.30 * baseRev,
                        1.00 * baseRev,
                        0.80 * baseRev,
                        0.60 * baseRev,
                        0.40 * baseRev
                    )
                } else {
                    val baseProfit = max(0.0, result.netProfitTotal)
                    val growthScale = if (result.revenue > 0) (baseProfit / result.revenue).coerceIn(0.10, 0.40) else 0.15
                    val growthFactor = 1.0 + growthScale
                    listOf(
                        0.60 * baseRev,
                        0.60 * baseRev * Math.pow(growthFactor, 1.0),
                        0.60 * baseRev * Math.pow(growthFactor, 2.0),
                        0.60 * baseRev * Math.pow(growthFactor, 3.0),
                        0.60 * baseRev * Math.pow(growthFactor, 4.0)
                    )
                }
            }
        }
    }

    val growthPercent = remember(barValues) {
        if (barValues.isNotEmpty() && barValues.first() > 0) {
            ((barValues.last() - barValues.first()) / barValues.first()) * 100.0
        } else 0.0
    }
    val growthStr = String.format(Locale.US, if (growthPercent >= 0) "+%.0f%%" else "%.0f%%", growthPercent)

    val displayBarLabels = remember(isKurdish, selectedTimeframe) {
        when (selectedTimeframe) {
            TimeframeOption.DAILY -> {
                if (isKurdish) listOf("ڕ١", "ڕ٢", "ڕ٣", "ڕ٤", "ڕ٥", "ڕ٦", "ڕ٧")
                else listOf("D1", "D2", "D3", "D4", "D5", "D6", "D7")
            }
            TimeframeOption.WEEKLY -> {
                if (isKurdish) listOf("هـ١", "هـ٢", "هـ٣", "هـ٤")
                else listOf("W1", "W2", "W3", "W4")
            }
            TimeframeOption.MONTHLY, TimeframeOption.TOTAL -> {
                if (isKurdish) listOf("م١", "م٢", "م٣", "م٤", "م٥")
                else listOf("M1", "M2", "M3", "M4", "M5")
            }
        }
    }

    val chartTitle = remember(isKurdish, selectedTimeframe) {
        when (selectedTimeframe) {
            TimeframeOption.DAILY -> if (isKurdish) "گەشەی داهاتی ڕۆژانە" else "Daily Revenue Trajectory"
            TimeframeOption.WEEKLY -> if (isKurdish) "گەشەی داهاتی هەفتانە" else "Weekly Revenue Trajectory"
            else -> if (isKurdish) "گەشەی داهاتی مانگانە" else "Monthly Revenue Trajectory"
        }
    }

    val badgeColor = if (result.isLoss || growthPercent < 0) neuColors.loss else neuColors.success

    Box(
        modifier = modifier
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
            // Header Info & Growth Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chartTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textMain,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = if (isKurdish) "کلیک لەسەر ستوونەکان بکە بۆ دەرکەوتنی قازانج/زیان" else "Tap/drag bars to view gain or loss",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = neuColors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            lightGradientColor = badgeColor.copy(alpha = 0.15f),
                            darkGradientColor = badgeColor.copy(alpha = 0.05f),
                            cornerRadius = 10.dp,
                            elevation = 2.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isKurdish) "گەشە: \u2066$growthStr\u2069" else "Growth: $growthStr",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.em,
                        color = badgeColor
                    )
                }
            }

            // Neumorphic Timeframe Segmented Control: Daily / Weekly / Monthly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(TimeframeOption.DAILY, TimeframeOption.WEEKLY, TimeframeOption.MONTHLY).forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    val tfLabel = when (tf) {
                        TimeframeOption.DAILY -> if (isKurdish) "ڕۆژانە" else "Daily"
                        TimeframeOption.WEEKLY -> if (isKurdish) "هەفتانە" else "Weekly"
                        TimeframeOption.MONTHLY -> if (isKurdish) "مانگانە" else "Monthly"
                        else -> ""
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.neuPressed(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 8.dp,
                                        elevation = 2.dp
                                    )
                                } else {
                                    Modifier.neuFlat(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 8.dp,
                                        elevation = 1.dp
                                    )
                                }
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.Tab) {
                                selectedTimeframe = tf
                                selectedBarIndex = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tfLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) neuColors.primary else neuColors.textMuted
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = neuColors.border)

            // Floating Gain / Loss Tooltip Card for Selected Bar
            AnimatedVisibility(
                visible = selectedBarIndex != null,
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                val activeIdx = (selectedBarIndex ?: 0).coerceIn(0, barValues.size - 1)
                val barLabel = displayBarLabels.getOrElse(activeIdx) { "" }
                val barRev = barValues[activeIdx]

                // Consistent net gain/loss calculation
                val profitMargin = if (result.revenue > 0) result.netProfitTotal / result.revenue else 0.0
                val barNet = if (result.revenue > 0) {
                    barRev * profitMargin
                } else {
                    val tfTotalNet = when (selectedTimeframe) {
                        TimeframeOption.DAILY -> result.netProfitTotal / 30.0
                        TimeframeOption.WEEKLY -> (result.netProfitTotal / 30.0) * 7.0
                        else -> result.netProfitTotal
                    }
                    tfTotalNet * ((activeIdx + 1) / barValues.size.toDouble())
                }
                val isBarLoss = result.isLoss || barNet < 0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 12.dp,
                            elevation = 3.dp
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isKurdish) "داهاتی $barLabel" else "$barLabel Revenue Bar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.primary
                            )
                            Text(
                                text = if (isKurdish) "داهات: ${formatAmt(barRev)} IQD" else "Projected Revenue: ${formatAmt(barRev)} IQD",
                                fontSize = 11.sp,
                                color = neuColors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBarLoss) {
                                    if (result.isLoss && abs(barNet) < 0.0001) {
                                        if (isKurdish) "زیان" else "Loss"
                                    } else {
                                        "\u2066-${formatAmt(abs(barNet))}\u2069 IQD (${if (isKurdish) "زیان" else "Loss"})"
                                    }
                                } else {
                                    "+${formatAmt(barNet)} IQD (${if (isKurdish) "قازانج" else "Gain"})"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBarLoss) neuColors.loss else neuColors.success
                            )
                            Text(
                                text = if (isKurdish) "تەخمینی قازانج/زیان" else "Estimated Net Gain/Loss",
                                fontSize = 10.sp,
                                color = neuColors.textMuted
                            )
                        }
                    }
                }
            }

            val chartAccessibilityDesc = remember(result, isKurdish) {
                if (isKurdish) {
                    "دیاگرامی ستوونی گەشەی داهات. داهاتی خەمڵێنراو: ${formatAmt(result.displayRevenue)} دینار."
                } else {
                    "Revenue growth bar chart showing projected total revenue of ${formatAmt(result.displayRevenue)} IQD."
                }
            }

            // Canvas Bar Drawing Area with Accurate Bar Hit-Testing & Granular Semantic Nodes
            Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl || isKurdish
                val density = LocalDensity.current

                fun updateBarSelection(offset: Offset, width: Float, height: Float) {
                    val hitIndex = with(density) {
                        val paddingLeft = 12.dp.toPx()
                        val paddingRight = 12.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 24.dp.toPx()
                        val chartWidth = width - paddingLeft - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom
                        val maxVal = (barValues.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)

                        val barCount = barValues.size
                        val barSpacing = 16.dp.toPx()
                        val totalSpacing = barSpacing * (barCount - 1)
                        val barWidth = (chartWidth - totalSpacing) / barCount

                        barValues.indices.firstOrNull { i ->
                            val valueDouble = barValues[i]
                            val barHeight = (valueDouble.toFloat() / maxVal) * chartHeight
                            val left = if (isRtl) {
                                paddingLeft + (barCount - 1 - i) * (barWidth + barSpacing)
                            } else {
                                paddingLeft + i * (barWidth + barSpacing)
                            }
                            val right = left + barWidth
                            val bottom = paddingTop + chartHeight
                            val top = bottom - barHeight.coerceAtLeast(4.dp.toPx())

                            val horizontalHit = offset.x >= (left - 8.dp.toPx()) && offset.x <= (right + 8.dp.toPx())
                            val verticalHit = offset.y >= (paddingTop - 8.dp.toPx()) && offset.y <= (bottom + 16.dp.toPx())

                            horizontalHit && verticalHit
                        }
                    }

                    if (hitIndex != selectedBarIndex) {
                        selectedBarIndex = hitIndex
                        if (hitIndex != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }

                // Canvas drawing layer with zero-allocation drawWithCache and interactive gestures
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .semantics {
                            contentDescription = chartAccessibilityDesc
                        }
                        .pointerInput(barValues, isRtl) {
                            detectTapGestures { offset ->
                                updateBarSelection(offset, size.width.toFloat(), size.height.toFloat())
                            }
                        }
                        .pointerInput(barValues, isRtl) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    updateBarSelection(offset, size.width.toFloat(), size.height.toFloat())
                                },
                                onHorizontalDrag = { change, _ ->
                                    updateBarSelection(change.position, size.width.toFloat(), size.height.toFloat())
                                }
                            )
                        }
                        .drawWithCache {
                            val width = size.width
                            val height = size.height

                            val paddingLeft = 12.dp.toPx()
                            val paddingRight = 12.dp.toPx()
                            val paddingTop = 16.dp.toPx()
                            val paddingBottom = 24.dp.toPx()

                            val chartWidth = width - paddingLeft - paddingRight
                            val chartHeight = height - paddingTop - paddingBottom
                            val maxVal = (barValues.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)

                            val barCount = barValues.size
                            val barSpacing = 16.dp.toPx()
                            val totalSpacing = barSpacing * (barCount - 1)
                            val barWidth = (chartWidth - totalSpacing) / barCount
                            val topCornerRadius = 10.dp.toPx()

                            // Precompute Bar Geometries & Brushes (Zero Allocations on Redraw)
                            data class BarCacheItem(
                                val barPath: Path,
                                val primaryBrush: Brush,
                                val selectedBrush: Brush,
                                val lossSelectedBrush: Brush,
                                val left: Float,
                                val top: Float,
                                val right: Float,
                                val bottom: Float
                            )

                            val barCaches = barValues.mapIndexed { index, valueDouble ->
                                val barHeight = (valueDouble.toFloat() / maxVal) * chartHeight
                                val left = if (isRtl) {
                                    paddingLeft + (barCount - 1 - index) * (barWidth + barSpacing)
                                } else {
                                    paddingLeft + index * (barWidth + barSpacing)
                                }
                                val right = left + barWidth
                                val bottom = paddingTop + chartHeight
                                val top = bottom - barHeight.coerceAtLeast(6.dp.toPx())

                                // Clean solid rounded bar path
                                val barPath = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            left = left,
                                            top = top,
                                            right = right,
                                            bottom = bottom,
                                            topLeftCornerRadius = CornerRadius(topCornerRadius, topCornerRadius),
                                            topRightCornerRadius = CornerRadius(topCornerRadius, topCornerRadius),
                                            bottomLeftCornerRadius = CornerRadius.Zero,
                                            bottomRightCornerRadius = CornerRadius.Zero
                                        )
                                    )
                                }

                                val primaryColor = neuColors.primary
                                val primaryBrush = Brush.linearGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.80f), primaryColor),
                                    start = Offset(left, top),
                                    end = Offset(right, bottom)
                                )

                                val successColor = neuColors.success
                                val selectedBrush = Brush.linearGradient(
                                    colors = listOf(successColor.copy(alpha = 0.85f), successColor),
                                    start = Offset(left, top),
                                    end = Offset(right, bottom)
                                )

                                val lossColor = neuColors.loss
                                val lossSelectedBrush = Brush.linearGradient(
                                    colors = listOf(lossColor.copy(alpha = 0.85f), lossColor),
                                    start = Offset(left, top),
                                    end = Offset(right, bottom)
                                )

                                BarCacheItem(
                                    barPath = barPath,
                                    primaryBrush = primaryBrush,
                                    selectedBrush = selectedBrush,
                                    lossSelectedBrush = lossSelectedBrush,
                                    left = left,
                                    top = top,
                                    right = right,
                                    bottom = bottom
                                )
                            }

                            // Precompute horizontal grid line offsets and stroke width
                            val gridCount = 3
                            val gridLines = (0..gridCount).map { i ->
                                val y = paddingTop + chartHeight * (1f - i.toFloat() / gridCount)
                                Pair(Offset(paddingLeft, y), Offset(width - paddingRight, y))
                            }
                            val gridStrokeWidth = 1.dp.toPx()
                            val selectedStrokeWidth = 2.dp.toPx()

                            onDrawBehind {
                                // 1. Draw Horizontal Grid Lines
                                gridLines.forEach { (startOffset, endOffset) ->
                                    drawLine(
                                        color = neuColors.border,
                                        start = startOffset,
                                        end = endOffset,
                                        strokeWidth = gridStrokeWidth
                                    )
                                }

                                // 2. Draw Active Convex Bars with Vertical Scale Animation
                                drawContext.canvas.save()
                                // Scale only active bars from baseline
                                drawContext.transform.translate(0f, paddingTop + chartHeight)
                                drawContext.transform.scale(scaleX = 1f, scaleY = animProgress)
                                drawContext.transform.translate(0f, -(paddingTop + chartHeight))

                                barCaches.forEachIndexed { index, item ->
                                    val isSelected = selectedBarIndex == index
                                    val barBrush = if (isSelected) {
                                        if (result.isLoss) item.lossSelectedBrush else item.selectedBrush
                                    } else {
                                        item.primaryBrush
                                    }

                                    // Render Bar Body
                                    drawPath(
                                        path = item.barPath,
                                        brush = barBrush
                                    )

                                    // Crisp Accent Border on Selected Bar
                                    if (isSelected) {
                                        val accentColor = if (result.isLoss) neuColors.loss else neuColors.success
                                        drawPath(
                                            path = item.barPath,
                                            color = accentColor,
                                            style = Stroke(width = selectedStrokeWidth)
                                        )
                                    }
                                }

                                drawContext.canvas.restore()
                            }
                        }
                )

                // Granular TalkBack accessibility nodes aligned with bar columns
                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    barValues.forEachIndexed { index, valueDouble ->
                        val barLabel = displayBarLabels.getOrElse(index) { "" }
                        val profitMargin = if (result.revenue > 0) result.netProfitTotal / result.revenue else 0.0
                        val barNet = if (result.revenue > 0) {
                            valueDouble * profitMargin
                        } else {
                            val tfTotalNet = when (selectedTimeframe) {
                                TimeframeOption.DAILY -> result.netProfitTotal / 30.0
                                TimeframeOption.WEEKLY -> (result.netProfitTotal / 30.0) * 7.0
                                else -> result.netProfitTotal
                            }
                            tfTotalNet * ((index + 1) / barValues.size.toDouble())
                        }
                        val isBarLoss = result.isLoss || barNet < 0
                        val isSelected = selectedBarIndex == index

                        val barDesc = if (isKurdish) {
                            "ستوونی $barLabel: داهاتی ${formatAmt(valueDouble)} دینار، تەخمینی ${if (isBarLoss) "${formatAmt(abs(barNet))} دینار زیان" else "${formatAmt(barNet)} دینار قازانج"}"
                        } else {
                            "$barLabel Bar: Revenue ${formatAmt(valueDouble)} IQD, Estimated ${if (isBarLoss) "\u2066-${formatAmt(abs(barNet))}\u2069 IQD Loss" else "+${formatAmt(barNet)} IQD Gain"}"
                        }
                        val selectActionLabel = if (isKurdish) "دیاریکردنی ستوونی $barLabel" else "Select $barLabel Bar"
                        val stateDesc = if (isSelected) (if (isKurdish) "دیاریکراوە" else "Selected") else (if (isKurdish) "دیارینەکراوە" else "Not selected")

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .semantics {
                                    contentDescription = barDesc
                                    stateDescription = stateDesc
                                    selected = isSelected
                                    role = Role.Button
                                    onClick(label = selectActionLabel) {
                                        selectedBarIndex = if (selectedBarIndex == index) null else index
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        true
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // X-Axis Month Labels Pixel-Aligned Under Each Bar Column
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                displayBarLabels.forEachIndexed { index, label ->
                    val isSelected = selectedBarIndex == index
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = if (isSelected) 11.sp else 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) neuColors.primary else neuColors.textMuted
                    )
                }
            }
        }
    }
}
