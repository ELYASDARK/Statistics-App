package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import kotlin.math.roundToInt

/**
 * Custom Canvas line chart drawing live Market Valuation & Performance curve
 * with interactive touch/drag inspection showing exact monthly gain or loss.
 * Fully compliant with RTL/LTR, System Reduce Motion, and TalkBack accessibility.
 */
@Composable
fun ValuationLineChart(
    result: CalculationResult,
    isKurdish: Boolean = true,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

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

    // Animated stroke reveal on chart load (honors system Reduce Motion)
    val animProgress = remember { Animatable(if (isReduceMotion) 1f else 0f) }
    LaunchedEffect(isReduceMotion) {
        if (isReduceMotion) {
            animProgress.snapTo(1f)
        } else if (animProgress.value < 1f) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
    }

    var selectedTimeframe by remember { mutableStateOf(TimeframeOption.MONTHLY) }

    // Generate milestone points based on active timeframe & calculation metrics
    val monthlyPoints = remember(result, selectedTimeframe) {
        when (selectedTimeframe) {
            TimeframeOption.DAILY -> {
                if (result.isLoss || result.revenue <= 0.0) {
                    val baseDailyBurn = max(5000.0, result.dailyCashBurn)
                    listOf(
                        baseDailyBurn * 1.0,
                        baseDailyBurn * 0.90,
                        baseDailyBurn * 0.80,
                        baseDailyBurn * 0.68,
                        baseDailyBurn * 0.55,
                        baseDailyBurn * 0.40,
                        baseDailyBurn * 0.25
                    )
                } else {
                    val baseDailyRev = max(50000.0, result.revenue / 30.0)
                    val dailyProfit = max(0.0, result.netProfitTotal / 30.0)
                    val dailyGrowth = if (baseDailyRev > 0) (dailyProfit / baseDailyRev).coerceIn(0.02, 0.15) else 0.04
                    (0..6).map { i -> baseDailyRev * Math.pow(1.0 + dailyGrowth, i.toDouble()) }
                }
            }
            TimeframeOption.WEEKLY -> {
                if (result.isLoss || result.revenue <= 0.0) {
                    val baseWeeklyBurn = max(25000.0, result.burnRateWeek)
                    listOf(
                        baseWeeklyBurn * 1.0,
                        baseWeeklyBurn * 0.82,
                        baseWeeklyBurn * 0.60,
                        baseWeeklyBurn * 0.35
                    )
                } else {
                    val baseWeeklyRev = max(200000.0, (result.revenue / 30.0) * 7.0)
                    val weeklyProfit = max(0.0, (result.netProfitTotal / 30.0) * 7.0)
                    val weeklyGrowth = if (baseWeeklyRev > 0) (weeklyProfit / baseWeeklyRev).coerceIn(0.05, 0.25) else 0.08
                    (0..3).map { i -> baseWeeklyRev * Math.pow(1.0 + weeklyGrowth, i.toDouble()) }
                }
            }
            TimeframeOption.MONTHLY, TimeframeOption.TOTAL -> {
                if (result.isLoss || result.revenue <= 0.0) {
                    val baseScale = max(100000.0, result.totalProjectExpenses)
                    listOf(
                        baseScale * 1.0,
                        baseScale * 0.8,
                        baseScale * 0.6,
                        baseScale * 0.45,
                        baseScale * 0.3,
                        baseScale * 0.15
                    )
                } else {
                    val baseRev = result.revenue
                    val baseProfit = max(0.0, result.netProfitTotal)
                    val growthScale = if (baseRev > 0) (baseProfit / baseRev).coerceIn(0.1, 0.5) else 0.2
                    val factor = 1.0 + growthScale
                    listOf(
                        0.4 * baseRev,
                        0.4 * baseRev * Math.pow(factor, 0.8),
                        0.4 * baseRev * Math.pow(factor, 1.6),
                        0.4 * baseRev * Math.pow(factor, 2.4),
                        0.4 * baseRev * Math.pow(factor, 3.2),
                        0.4 * baseRev * Math.pow(factor, 4.0)
                    )
                }
            }
        }
    }

    val monthLabels = remember(isKurdish, selectedTimeframe) {
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
                if (isKurdish) listOf("م١", "م٢", "م٣", "م٤", "م٥", "م٦")
                else listOf("M1", "M2", "M3", "M4", "M5", "M6")
            }
        }
    }

    val badgeText = remember(monthlyPoints, result.isLoss, selectedTimeframe, isKurdish) {
        if (result.isLoss) {
            when (selectedTimeframe) {
                TimeframeOption.DAILY -> if (isKurdish) "خەرجی ڕۆژ: -${formatAmt(result.dailyCashBurn)} IQD" else "Daily: -${formatAmt(result.dailyCashBurn)} IQD"
                TimeframeOption.WEEKLY -> if (isKurdish) "خەرجی هەفتە: -${formatAmt(result.burnRateWeek)} IQD" else "Weekly: -${formatAmt(result.burnRateWeek)} IQD"
                else -> if (isKurdish) "بەها: 0.0x" else "Valuation: 0.0x"
            }
        } else {
            val mult = if (monthlyPoints.isNotEmpty() && monthlyPoints.first() > 0) monthlyPoints.last() / monthlyPoints.first() else 1.0
            val growthPct = (mult - 1.0) * 100.0
            when (selectedTimeframe) {
                TimeframeOption.DAILY -> if (isKurdish) "گەشەی ڕۆژ: +${String.format(Locale.US, "%.0f%%", growthPct)}" else "Daily: +${String.format(Locale.US, "%.0f%%", growthPct)}"
                TimeframeOption.WEEKLY -> if (isKurdish) "گەشەی هەفتە: +${String.format(Locale.US, "%.0f%%", growthPct)}" else "Weekly: +${String.format(Locale.US, "%.0f%%", growthPct)}"
                else -> if (isKurdish) "بەها: ${String.format(Locale.US, "%.1fx", mult)}" else "Valuation: ${String.format(Locale.US, "%.1fx", mult)}"
            }
        }
    }

    val accentColor = if (result.isLoss) neuColors.loss else neuColors.success
    val chartCurveColor = if (result.isLoss) neuColors.loss else neuColors.primary

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
            // Chart Header & Timeframe Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isKurdish) "هێڵی گەشەی بەهای بازاڕ" else "Market Valuation Curve",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textMain,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = if (isKurdish) "کلیک/ڕابکێشە بۆ بینینی قازانج یان زیان" else "Tap/drag points to view gain or loss",
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
                            lightGradientColor = accentColor.copy(alpha = 0.15f),
                            darkGradientColor = accentColor.copy(alpha = 0.05f),
                            cornerRadius = 10.dp,
                            elevation = 2.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.em,
                        color = accentColor
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
                                selectedIndex = null
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

            // Floating Gain / Loss Tooltip Card
            AnimatedVisibility(visible = selectedIndex != null) {
                val activeIdx = (selectedIndex ?: 0).coerceIn(0, monthlyPoints.size - 1)
                val periodLabel = monthLabels.getOrElse(activeIdx) { "" }
                val periodVal = monthlyPoints[activeIdx]

                val progressRatio = (activeIdx + 1) / monthlyPoints.size.toDouble()
                val timeframeNet = when (selectedTimeframe) {
                    TimeframeOption.DAILY -> (result.netProfitTotal / 30.0) * progressRatio
                    TimeframeOption.WEEKLY -> ((result.netProfitTotal / 30.0) * 7.0) * progressRatio
                    else -> result.netProfitTotal * progressRatio
                }
                val isPointLoss = result.isLoss || timeframeNet <= 0.0

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
                                text = if (isKurdish) "خاڵی $periodLabel" else "$periodLabel Milestone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = chartCurveColor
                            )
                            Text(
                                text = if (isKurdish) "بەهای دارایی: ${formatAmt(periodVal)} IQD" else "Projected Value: ${formatAmt(periodVal)} IQD",
                                fontSize = 11.sp,
                                color = neuColors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isPointLoss) {
                                    if (result.isLoss && abs(timeframeNet) < 0.0001) {
                                        if (result.grossProfitPerOrder < 0) {
                                            "\u2066-${formatAmt(abs(result.grossProfitPerOrder))}\u2069 IQD (${if (isKurdish) "زیان/دانە" else "Unit Loss"})"
                                        } else {
                                            "0 IQD (${if (isKurdish) "زیان" else "Loss"})"
                                        }
                                    } else {
                                        "\u2066-${formatAmt(abs(timeframeNet))}\u2069 IQD (${if (isKurdish) "زیان" else "Loss"})"
                                    }
                                } else {
                                    "+${formatAmt(timeframeNet)} IQD (${if (isKurdish) "قازانج" else "Gain"})"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPointLoss) neuColors.loss else neuColors.success
                            )
                            Text(
                                text = if (isKurdish) "قازانج/زیانی کەڵەکەبوو" else "Cumulative Net Gain/Loss",
                                fontSize = 10.sp,
                                color = neuColors.textMuted
                            )
                        }
                    }
                }
            }

            val chartAccessibilityDesc = remember(result, isKurdish) {
                if (isKurdish) {
                    if (result.isLoss) {
                        "دیاگرامی هێڵی بەهای دارایی. مۆدێل لە دۆخی زیاندایە بە بەهای 0.0x."
                    } else {
                        "دیاگرامی هێڵی بەهای دارایی. قازانجی پوختەی مانگانە: ${formatAmt(result.displayNetProfit)} دینار."
                    }
                } else {
                    if (result.isLoss) {
                        "Valuation trend chart showing business in loss state with 0.0x valuation multiplier."
                    } else {
                        "Valuation trend chart showing projected monthly net profit of ${formatAmt(result.displayNetProfit)} IQD."
                    }
                }
            }

            // Canvas Chart Drawing Area with Accurate Point Hit-Testing & Granular Semantic Nodes
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

                // Unified Tap & Drag Pointer Input Layer
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .semantics {
                            contentDescription = chartAccessibilityDesc
                        }
                        .pointerInput(monthlyPoints, isRtl) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 16.dp.toPx()
                                val paddingRight = 16.dp.toPx()
                                val chartWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1f)

                                val fraction = if (isRtl) {
                                    ((width - paddingRight) - offset.x) / chartWidth
                                } else {
                                    (offset.x - paddingLeft) / chartWidth
                                }

                                val pointCount = monthlyPoints.size
                                if (pointCount > 1 && fraction in -0.15f..1.15f) {
                                    val nearestIdx = (fraction * (pointCount - 1)).roundToInt().coerceIn(0, pointCount - 1)
                                    selectedIndex = if (selectedIndex == nearestIdx) null else nearestIdx
                                } else {
                                    selectedIndex = null
                                }
                            }
                        }
                        .pointerInput(monthlyPoints, isRtl) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val width = size.width
                                    val paddingLeft = 16.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1f)

                                    val fraction = if (isRtl) {
                                        ((width - paddingRight) - offset.x) / chartWidth
                                    } else {
                                        (offset.x - paddingLeft) / chartWidth
                                    }
                                    val pointCount = monthlyPoints.size
                                    if (pointCount > 1) {
                                        selectedIndex = (fraction * (pointCount - 1)).roundToInt().coerceIn(0, pointCount - 1)
                                    }
                                },
                                onHorizontalDrag = { change, _ ->
                                    val width = size.width
                                    val paddingLeft = 16.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1f)

                                    val fraction = if (isRtl) {
                                        ((width - paddingRight) - change.position.x) / chartWidth
                                    } else {
                                        (change.position.x - paddingLeft) / chartWidth
                                    }
                                    val pointCount = monthlyPoints.size
                                    if (pointCount > 1) {
                                        selectedIndex = (fraction * (pointCount - 1)).roundToInt().coerceIn(0, pointCount - 1)
                                    }
                                }
                            )
                        }
                        .drawWithCache {
                            val width = size.width
                            val height = size.height

                            val paddingLeft = 16.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val paddingTop = 20.dp.toPx()
                            val paddingBottom = 30.dp.toPx()

                            val chartWidth = (width - paddingLeft - paddingRight).coerceAtLeast(1f)
                            val chartHeight = (height - paddingTop - paddingBottom).coerceAtLeast(1f)

                            val maxVal = (monthlyPoints.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)
                            val minVal = 0f

                            // Compute Point Coordinates (Mirrored along X if RTL)
                            val points = monthlyPoints.mapIndexed { index, valDouble ->
                                val fraction = if (monthlyPoints.size > 1) index.toFloat() / (monthlyPoints.size - 1) else 0f
                                val x = if (isRtl) {
                                    width - paddingRight - fraction * chartWidth
                                } else {
                                    paddingLeft + fraction * chartWidth
                                }
                                val normalizedY = ((valDouble.toFloat() - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                                val y = paddingTop + chartHeight * (1f - normalizedY)
                                Offset(x, y)
                            }

                            val strokePath = Path()
                            val fillPath = Path()
                            val lineStroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)

                            if (points.isNotEmpty()) {
                                strokePath.moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = if (i > 0) points[i - 1] else points[i]
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val p3 = if (i + 2 < points.size) points[i + 2] else p2

                                    val cp1X = p1.x + (p2.x - p0.x) / 6f
                                    val cp1Y = (p1.y + (p2.y - p0.y) / 6f).coerceIn(paddingTop, paddingTop + chartHeight)
                                    val cp2X = p2.x - (p3.x - p1.x) / 6f
                                    val cp2Y = (p2.y - (p3.y - p1.y) / 6f).coerceIn(paddingTop, paddingTop + chartHeight)

                                    strokePath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2.x, p2.y)
                                }

                                fillPath.addPath(strokePath)
                                fillPath.lineTo(points.last().x, paddingTop + chartHeight)
                                fillPath.lineTo(points.first().x, paddingTop + chartHeight)
                                fillPath.close()
                            }

                            val fillBrush = Brush.verticalGradient(
                                colors = listOf(
                                    chartCurveColor.copy(alpha = 0.35f),
                                    chartCurveColor.copy(alpha = 0.02f)
                                ),
                                startY = paddingTop,
                                endY = paddingTop + chartHeight
                            )

                            // Horizontal Grid Lines
                            val gridLineCount = 4
                            val gridLines = (0..gridLineCount).map { i ->
                                val y = paddingTop + chartHeight * (1f - i.toFloat() / gridLineCount)
                                Pair(Offset(paddingLeft, y), Offset(width - paddingRight, y))
                            }
                            val gridStrokeWidth = 1.dp.toPx()

                            // Vertical Guide Lines & Dots
                            val pointBottomOffsets = points.map { point ->
                                Offset(point.x, paddingTop + chartHeight)
                            }
                            val selectedGuideStrokeWidth = 2.dp.toPx()
                            val normalGuideStrokeWidth = 1.dp.toPx()
                            val selectedOuterRadius = 10.dp.toPx()
                            val normalOuterRadius = 7.dp.toPx()
                            val selectedInnerRadius = 5.5.dp.toPx()
                            val normalInnerRadius = 4.dp.toPx()
                            val centerDotRadius = 1.5.dp.toPx()

                            onDrawBehind {
                                // 1. Draw Grid
                                gridLines.forEach { (startOffset, endOffset) ->
                                    drawLine(
                                        color = neuColors.border,
                                        start = startOffset,
                                        end = endOffset,
                                        strokeWidth = gridStrokeWidth
                                    )
                                }

                                if (points.isNotEmpty()) {
                                    // Animated Reveal Clip
                                    drawContext.canvas.save()
                                    if (isRtl) {
                                        val revealWidth = chartWidth * animProgress.value
                                        drawContext.transform.clipRect(
                                            left = (width - paddingRight - revealWidth - 20f).coerceAtLeast(0f),
                                            top = 0f,
                                            right = width,
                                            bottom = height
                                        )
                                    } else {
                                        drawContext.transform.clipRect(
                                            left = 0f,
                                            top = 0f,
                                            right = paddingLeft + (chartWidth * animProgress.value) + 20f,
                                            bottom = height
                                        )
                                    }

                                    // 2. Draw Gradient Fill
                                    drawPath(path = fillPath, brush = fillBrush)

                                    // 3. Draw Bezier Stroke
                                    drawPath(path = strokePath, color = chartCurveColor, style = lineStroke)

                                    // 4. Draw Point Indicators & Selected Highlight
                                    points.forEachIndexed { index, point ->
                                        val isSelected = selectedIndex == index
                                        val isPointVisible = if (isRtl) {
                                            point.x >= (width - paddingRight - (chartWidth * animProgress.value) - 10f)
                                        } else {
                                            point.x <= (paddingLeft + (chartWidth * animProgress.value) + 10f)
                                        }
                                        if (isPointVisible) {
                                            // Vertical Guide Line
                                            drawLine(
                                                color = if (isSelected) chartCurveColor else neuColors.border,
                                                start = point,
                                                end = pointBottomOffsets[index],
                                                strokeWidth = if (isSelected) selectedGuideStrokeWidth else normalGuideStrokeWidth
                                            )

                                            // Outer Indicator Ring
                                            drawCircle(
                                                color = chartCurveColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                                                radius = if (isSelected) selectedOuterRadius else normalOuterRadius,
                                                center = point
                                            )

                                            // Inner Dot
                                            drawCircle(
                                                color = chartCurveColor,
                                                radius = if (isSelected) selectedInnerRadius else normalInnerRadius,
                                                center = point
                                            )

                                            // Center Dot Core
                                            drawCircle(
                                                color = Color.White,
                                                radius = centerDotRadius,
                                                center = point
                                            )
                                        }
                                    }

                                    drawContext.canvas.restore()
                                }
                            }
                        }
                )

                // Granular TalkBack Accessibility Nodes per Milestone Point
                Row(modifier = Modifier.matchParentSize()) {
                    monthlyPoints.forEachIndexed { index, valDouble ->
                        val monthLabel = monthLabels.getOrElse(index) { "M${index + 1}" }
                        val isSelected = selectedIndex == index
                        val progressRatio = (index + 1) / monthlyPoints.size.toDouble()
                        val monthlyNet = result.netProfitTotal * progressRatio
                        val isPointLoss = result.isLoss || monthlyNet <= 0.0

                        val pointDesc = if (isKurdish) {
                            if (result.isLoss) {
                                "خاڵی $monthLabel: داهاتی خەمڵێنراو 0 دینار، مۆدێل لە دۆخی زیاندایە"
                            } else {
                                "خاڵی $monthLabel: داهاتی خەمڵێنراو ${formatAmt(valDouble)} دینار، ${if (isPointLoss) "${formatAmt(abs(monthlyNet))} دینار زیان" else "${formatAmt(monthlyNet)} دینار قازانج"}"
                            }
                        } else {
                            if (result.isLoss) {
                                "$monthLabel Milestone: Projected Revenue 0 IQD, Model operating at a loss"
                            } else {
                                "$monthLabel Milestone: Projected Revenue ${formatAmt(valDouble)} IQD, ${if (isPointLoss) "\u2066-${formatAmt(abs(monthlyNet))}\u2069 IQD Loss" else "+${formatAmt(monthlyNet)} IQD Gain"}"
                            }
                        }
                        val selectActionLabel = if (isKurdish) "دیاریکردنی $monthLabel" else "Select $monthLabel"

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                    stateDescription = if (isSelected) {
                                        if (isKurdish) "دیاریکراوە" else "Selected"
                                    } else {
                                        if (isKurdish) "دیارینەکراوە" else "Not selected"
                                    }
                                    contentDescription = pointDesc
                                    onClick(label = selectActionLabel) {
                                        selectedIndex = if (selectedIndex == index) null else index
                                        true
                                    }
                                }
                        )
                    }
                }
            }

            // X-Axis Month Labels (Mathematically Centered Exactly Under Point Dots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthLabels.forEachIndexed { index, label ->
                    val isSelected = selectedIndex == index
                    Text(
                        text = label,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = if (isSelected) 11.sp else 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) chartCurveColor else neuColors.textMuted
                    )
                }
            }
        }
    }
}
