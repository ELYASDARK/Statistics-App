package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
private val INTEGER_FORMAT = DecimalFormat("#,##0", US_SYMBOLS)

/**
 * Custom Canvas line chart drawing live Market Valuation & Performance curve
 * with interactive touch/drag inspection showing exact monthly gain or loss.
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
        return INTEGER_FORMAT.format(num)
    }

    // Animated stroke progress on chart load & data updates
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(result) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Generate 6 monthly milestone points based on live calculation metrics
    val monthlyPoints = remember(result) {
        val baseRev = max(100.0, result.revenue)
        if (result.isLoss) {
            listOf(
                baseRev * 1.0,
                baseRev * 0.8,
                baseRev * 0.6,
                baseRev * 0.45,
                baseRev * 0.3,
                baseRev * 0.15
            )
        } else {
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

    val valuationMult = remember(monthlyPoints) {
        if (monthlyPoints.first() > 0) monthlyPoints.last() / monthlyPoints.first() else 1.0
    }
    val valuationStr = String.format(Locale.US, "%.1fx", valuationMult)

    val monthLabels = remember(isKurdish) {
        if (isKurdish) {
            listOf("مانگی ١", "مانگی ٢", "مانگی ٣", "مانگی ٤", "مانگی ٥", "مانگی ٦")
        } else {
            listOf("M1", "M2", "M3", "M4", "M5", "M6")
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                // Chart Header & Valuation Multiplier Summary
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
                            color = neuColors.textMain
                        )
                        Text(
                            text = if (isKurdish) "داتای بەشی کلیک بکە بۆ بینینی قازانج/زیان" else "Tap/drag points to view gain or loss",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = neuColors.textMuted
                        )
                    }

                    val accentColor = if (result.isLoss || valuationMult < 1.0) neuColors.loss else neuColors.success

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
                            text = if (isKurdish) "بەها: $valuationStr" else "Valuation: $valuationStr",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = neuColors.border)
                Spacer(modifier = Modifier.height(10.dp))

                // Floating Gain / Loss Tooltip Card
                AnimatedVisibility(visible = selectedIndex != null) {
                    val activeIdx = (selectedIndex ?: 0).coerceIn(0, monthlyPoints.size - 1)
                    val monthLabel = monthLabels[activeIdx]
                    val monthlyVal = monthlyPoints[activeIdx]
                    
                    // Monthly gain / loss math proportional to milestone ratio
                    val progressRatio = (activeIdx + 1) / 6.0
                    val monthlyNet = result.netProfitTotal * progressRatio
                    val isPointLoss = result.isLoss || monthlyNet < 0

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
                                    text = if (isKurdish) "خاڵی $monthLabel" else "$monthLabel Milestone",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.primary
                                )
                                Text(
                                    text = if (isKurdish) "داهاتی خەمڵێنراو: ${formatAmt(monthlyVal)} IQD" else "Projected Revenue: ${formatAmt(monthlyVal)} IQD",
                                    fontSize = 11.sp,
                                    color = neuColors.textMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isPointLoss) {
                                        "-${formatAmt(abs(monthlyNet))} IQD (${if (isKurdish) "زیان" else "Loss"})"
                                    } else {
                                        "+${formatAmt(monthlyNet)} IQD (${if (isKurdish) "قازانج" else "Gain"})"
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

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas Chart Drawing Area with Pointer Inputs for Tap & Drag
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(monthlyPoints) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 16.dp.toPx()
                                val paddingRight = 16.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight
                                val pointsX = monthlyPoints.indices.map { i ->
                                    paddingLeft + (i.toFloat() / (monthlyPoints.size - 1)) * chartWidth
                                }
                                val closest = pointsX.indices.minByOrNull { abs(pointsX[it] - offset.x) }
                                selectedIndex = closest
                            }
                        }
                        .pointerInput(monthlyPoints) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val width = size.width
                                    val paddingLeft = 16.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val pointsX = monthlyPoints.indices.map { i ->
                                        paddingLeft + (i.toFloat() / (monthlyPoints.size - 1)) * chartWidth
                                    }
                                    val closest = pointsX.indices.minByOrNull { abs(pointsX[it] - offset.x) }
                                    selectedIndex = closest
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val offset = change.position
                                    val width = size.width
                                    val paddingLeft = 16.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = width - paddingLeft - paddingRight
                                    val pointsX = monthlyPoints.indices.map { i ->
                                        paddingLeft + (i.toFloat() / (monthlyPoints.size - 1)) * chartWidth
                                    }
                                    val closest = pointsX.indices.minByOrNull { abs(pointsX[it] - offset.x) }
                                    selectedIndex = closest
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 16.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val paddingTop = 20.dp.toPx()
                    val paddingBottom = 30.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val maxVal = (monthlyPoints.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)
                    val minVal = 0f

                    // 1. Draw Horizontal Grid Lines
                    val gridLineCount = 4
                    for (i in 0..gridLineCount) {
                        val y = paddingTop + chartHeight * (1f - i.toFloat() / gridLineCount)
                        drawLine(
                            color = neuColors.border,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. Compute Point Coordinates
                    val points = monthlyPoints.mapIndexed { index, valDouble ->
                        val x = paddingLeft + (index.toFloat() / (monthlyPoints.size - 1)) * chartWidth
                        val normalizedY = ((valDouble.toFloat() - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                        val y = paddingTop + chartHeight * (1f - normalizedY)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        // 3. Construct Cubic Bezier Path
                        val strokePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                val controlY1 = p0.y
                                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                                val controlY2 = p1.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            }
                        }

                        // 4. Clip drawing scope with animated reveal progress
                        drawContext.canvas.save()
                        drawContext.transform.clipRect(
                            left = 0f,
                            top = 0f,
                            right = paddingLeft + (chartWidth * animProgress.value) + 20f,
                            bottom = height
                        )

                        // Construct Gradient Fill Area Path
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(points.last().x, paddingTop + chartHeight)
                            lineTo(points.first().x, paddingTop + chartHeight)
                            close()
                        }

                        // Draw Gradient Fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    neuColors.primary.copy(alpha = 0.3f),
                                    neuColors.primary.copy(alpha = 0.02f)
                                ),
                                startY = paddingTop,
                                endY = paddingTop + chartHeight
                            )
                        )

                        // Draw Cubic Bezier Smooth Line
                        drawPath(
                            path = strokePath,
                            color = neuColors.primary,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 5. Draw Data Point Indicators & Selected Highlight Crosshair
                        points.forEachIndexed { index, point ->
                            val isSelected = selectedIndex == index
                            if (point.x <= paddingLeft + (chartWidth * animProgress.value) + 10f) {
                                // Vertical guide line (highlighted when selected)
                                drawLine(
                                    color = if (isSelected) neuColors.primary else neuColors.border,
                                    start = Offset(point.x, point.y),
                                    end = Offset(point.x, paddingTop + chartHeight),
                                    strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx()
                                )

                                val pointColor = if (result.isLoss) neuColors.loss else neuColors.primary

                                // Outer glowing indicator ring (larger if selected)
                                drawCircle(
                                    color = pointColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                                    radius = if (isSelected) 10.dp.toPx() else 7.dp.toPx(),
                                    center = point
                                )

                                // Inner solid indicator dot
                                drawCircle(
                                    color = pointColor,
                                    radius = if (isSelected) 5.5.dp.toPx() else 4.dp.toPx(),
                                    center = point
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = 1.5.dp.toPx(),
                                    center = point
                                )
                            }
                        }

                        drawContext.canvas.restore()
                    }
                }

                // X-Axis Month Labels
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
