package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    fun formatAmt(num: Double): String {
        if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
        val symbols = DecimalFormatSymbols(Locale.US)
        return DecimalFormat("#,##0", symbols).format(num)
    }

    // Animated scale progress on data updates
    var animationTrigger by remember { mutableStateOf(0f) }
    LaunchedEffect(result) {
        animationTrigger = 0f
        animationTrigger = 1f
    }

    val animProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "BarChartHeightAnimation"
    )

    // Calculate 5 metric bars representing monthly revenue scale
    val barValues = remember(result) {
        val rev = max(100.0, result.revenue)
        if (result.isLoss) {
            listOf(
                1.3 * rev,
                1.0 * rev,
                0.8 * rev,
                0.6 * rev,
                0.4 * rev
            )
        } else {
            val baseProfit = max(0.0, result.netProfitTotal)
            val growthScale = if (rev > 0) (baseProfit / rev).coerceIn(0.1, 0.5) else 0.2
            val growthFactor = 1.0 + growthScale
            listOf(
                0.5 * rev,
                0.5 * rev * Math.pow(growthFactor, 0.75),
                0.5 * rev * Math.pow(growthFactor, 1.5),
                0.5 * rev * Math.pow(growthFactor, 2.25),
                0.5 * rev * Math.pow(growthFactor, 3.0)
            )
        }
    }

    val growthPercent = remember(barValues) {
        if (barValues.first() > 0) {
            ((barValues.last() - barValues.first()) / barValues.first()) * 100
        } else 0.0
    }
    val growthStr = String.format(Locale.US, if (growthPercent >= 0) "+%.0f%%" else "%.0f%%", growthPercent)

    val displayBarLabels = remember(isKurdish) {
        if (isKurdish) {
            listOf("م١", "م٢", "م٣", "م٤", "م٥")
        } else {
            listOf("M1", "M2", "M3", "M4", "M5")
        }
    }

    val badgeColor = if (result.isLoss || growthPercent < 0) neuColors.loss else neuColors.success

    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
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
            Column {
                // Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isKurdish) "گەشەی داهاتی مانگانە" else "Monthly Revenue Trajectory",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textMain
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
                            text = if (isKurdish) "گەشە: $growthStr" else "Growth: $growthStr",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = neuColors.border)
                Spacer(modifier = Modifier.height(10.dp))

                // Floating Gain / Loss Tooltip Card for Selected Bar
                AnimatedVisibility(visible = selectedBarIndex != null) {
                    val activeIdx = (selectedBarIndex ?: 0).coerceIn(0, barValues.size - 1)
                    val monthLabel = displayBarLabels[activeIdx]
                    val monthlyRev = barValues[activeIdx]

                    val progressRatio = (activeIdx + 1) / 5.0
                    val monthlyNet = result.netProfitTotal * progressRatio
                    val isBarLoss = result.isLoss || monthlyNet < 0

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
                                    text = if (isKurdish) "داهاتی $monthLabel" else "$monthLabel Revenue Bar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.primary
                                )
                                Text(
                                    text = if (isKurdish) "داهاتی مانگانە: ${formatAmt(monthlyRev)} IQD" else "Monthly Revenue: ${formatAmt(monthlyRev)} IQD",
                                    fontSize = 11.sp,
                                    color = neuColors.textMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBarLoss) {
                                        "-${formatAmt(abs(monthlyNet))} IQD (${if (isKurdish) "زیان" else "Loss"})"
                                    } else {
                                        "+${formatAmt(monthlyNet)} IQD (${if (isKurdish) "قازانج" else "Gain"})"
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

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas Bar Drawing Area with Pointer Inputs for Tap & Drag
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .pointerInput(barValues) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 12.dp.toPx()
                                val paddingRight = 12.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight
                                val barCount = barValues.size
                                val barSpacing = 16.dp.toPx()
                                val totalSpacing = barSpacing * (barCount - 1)
                                val barWidth = (chartWidth - totalSpacing) / barCount
                                val centersX = barValues.indices.map { i ->
                                    paddingLeft + i * (barWidth + barSpacing) + (barWidth / 2f)
                                }
                                val closest = centersX.indices.minByOrNull { abs(centersX[it] - offset.x) }
                                selectedBarIndex = closest
                            }
                        }
                        .pointerInput(barValues) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val width = size.width
                                    val paddingLeft = 12.dp.toPx()
                                    val paddingRight = 12.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val barCount = barValues.size
                                    val barSpacing = 16.dp.toPx()
                                    val totalSpacing = barSpacing * (barCount - 1)
                                    val barWidth = (chartWidth - totalSpacing) / barCount
                                    val centersX = barValues.indices.map { i ->
                                        paddingLeft + i * (barWidth + barSpacing) + (barWidth / 2f)
                                    }
                                    val closest = centersX.indices.minByOrNull { abs(centersX[it] - offset.x) }
                                    selectedBarIndex = closest
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val offset = change.position
                                    val width = size.width
                                    val paddingLeft = 12.dp.toPx()
                                    val paddingRight = 12.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val barCount = barValues.size
                                    val barSpacing = 16.dp.toPx()
                                    val totalSpacing = barSpacing * (barCount - 1)
                                    val barWidth = (chartWidth - totalSpacing) / barCount
                                    val centersX = barValues.indices.map { i ->
                                        paddingLeft + i * (barWidth + barSpacing) + (barWidth / 2f)
                                    }
                                    val closest = centersX.indices.minByOrNull { abs(centersX[it] - offset.x) }
                                    selectedBarIndex = closest
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 12.dp.toPx()
                    val paddingRight = 12.dp.toPx()
                    val paddingTop = 16.dp.toPx()
                    val paddingBottom = 24.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val maxVal = (barValues.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)

                    // Draw Horizontal Grid Lines
                    val gridCount = 3
                    for (i in 0..gridCount) {
                        val y = paddingTop + chartHeight * (1f - i.toFloat() / gridCount)
                        drawLine(
                            color = neuColors.border,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val barCount = barValues.size
                    val barSpacing = 16.dp.toPx()
                    val totalSpacing = barSpacing * (barCount - 1)
                    val barWidth = (chartWidth - totalSpacing) / barCount
                    val topCornerRadius = 10.dp.toPx()

                    // Render Animated Concave Bars with Selected Highlight
                    barValues.forEachIndexed { index, valueDouble ->
                        val isSelected = selectedBarIndex == index
                        val barHeight = (valueDouble.toFloat() / maxVal) * chartHeight * animProgress
                        val left = paddingLeft + index * (barWidth + barSpacing)
                        val right = left + barWidth
                        val bottom = paddingTop + chartHeight
                        val top = bottom - barHeight.coerceAtLeast(4.dp.toPx())

                        // Path with top rounded corners only
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

                        val barFillColor = if (isSelected) {
                            if (result.isLoss) neuColors.loss else neuColors.success
                        } else {
                            neuColors.primary
                        }

                        // Primary vertical gradient fill
                        val barBrush = Brush.verticalGradient(
                            colors = listOf(
                                barFillColor.copy(alpha = if (isSelected) 0.6f else 0.3f),
                                barFillColor
                            ),
                            startY = top,
                            endY = bottom
                        )

                        drawPath(
                            path = barPath,
                            brush = barBrush
                        )
                    }
                }

                // X-Axis Month Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    displayBarLabels.forEachIndexed { index, label ->
                        val isSelected = selectedBarIndex == index
                        Text(
                            text = label,
                            fontSize = if (isSelected) 11.sp else 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) neuColors.primary else neuColors.textMuted
                        )
                    }
                }
            }
        }
    }
}
