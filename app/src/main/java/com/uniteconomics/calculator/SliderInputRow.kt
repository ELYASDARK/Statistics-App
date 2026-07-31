package com.uniteconomics.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val US_SYMBOLS = DecimalFormatSymbols(Locale.US)
private val DECIMAL_FORMAT = DecimalFormat("#,##0.0", US_SYMBOLS)
private val INTEGER_FORMAT = DecimalFormat("#,##0", US_SYMBOLS)

/**
 * Bi-directional numeric text input + Compose Slider row for 19 calculation inputs.
 * Features 60fps real-time updates, thousand separators, and strict range clamping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderInputRow(
    label: String,
    subLabel: String? = null,
    value: Double,
    onValueChange: (Double) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Double = 1.0,
    unit: String,
    isMarked: Boolean = false,
    isKurdish: Boolean = true,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    // Format helper for text field display with thousand separators
    fun formatDisplay(v: Double): String {
        return if (step < 1.0) {
            DECIMAL_FORMAT.format(v)
        } else {
            INTEGER_FORMAT.format(v.toLong())
        }
    }

    var textInputState by remember(value) { mutableStateOf(formatDisplay(value)) }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = if (isMarked) neuColors.primary.copy(alpha = 0.05f) else neuColors.surface,
                cornerRadius = 16.dp,
                elevation = if (isMarked) 3.dp else 2.dp
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Label, Sublabel, Info Button, and Text Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textMain
                    )
                    
                    // Info button for term explanation modal
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

                if (!subLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = neuColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Numeric Input Box with Bi-directional synchronization (Always LTR flow for numbers + units)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 90.dp, max = 120.dp)
                            .height(44.dp)
                            .neuPressed(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 10.dp,
                                elevation = 2.dp
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = textInputState,
                            onValueChange = { input ->
                                textInputState = input
                                val cleaned = input.replace(",", "")
                                val parsed = cleaned.toDoubleOrNull()
                                if (parsed != null) {
                                    val clamped = parsed.coerceIn(
                                        valueRange.start.toDouble(),
                                        valueRange.endInclusive.toDouble()
                                    )
                                    onValueChange(clamped)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMain
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(neuColors.primary)
                        )
                    }

                    Text(
                        text = unit,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = neuColors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val fraction = ((value.toFloat() - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        // Bi-Directional Compose Slider with redesigned convex thumb & track
        Slider(
            value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = { newFloatVal ->
                val clamped = newFloatVal.toDouble().coerceIn(
                    valueRange.start.toDouble(),
                    valueRange.endInclusive.toDouble()
                )
                textInputState = formatDisplay(clamped)
                onValueChange(clamped)
            },
            valueRange = valueRange,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            lightGradientColor = neuColors.shadowLight.copy(alpha = 0.6f),
                            darkGradientColor = neuColors.shadowDark.copy(alpha = 0.3f),
                            cornerRadius = 11.dp,
                            elevation = 2.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(neuColors.primary)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.Center)
                        )
                    }
                }
            },
            track = {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                ) {
                    val width = size.width
                    val height = size.height
                    val activeWidth = width * fraction

                    // Inactive track with enhanced dark theme contrast
                    val inactiveColor = if (neuColors.isDark) Color(0xFF2C3440) else neuColors.border
                    drawRoundRect(
                        color = inactiveColor,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f, height / 2f)
                    )

                    // Active track: neuColors.primary (respecting LTR vs RTL fill direction)
                    if (isRtl) {
                        drawRoundRect(
                            color = neuColors.primary,
                            topLeft = androidx.compose.ui.geometry.Offset(width - activeWidth, 0f),
                            size = androidx.compose.ui.geometry.Size(activeWidth, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f, height / 2f)
                        )
                    } else {
                        drawRoundRect(
                            color = neuColors.primary,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(activeWidth, height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f, height / 2f)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

