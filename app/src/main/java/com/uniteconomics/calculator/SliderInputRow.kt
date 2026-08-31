package com.uniteconomics.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
    minVal: Float = 0f,
    maxVal: Float = 100f,
    valueRange: ClosedFloatingPointRange<Float> = minVal..maxVal,
    step: Double = 1.0,
    unit: String,
    isMarked: Boolean = false,
    isKurdish: Boolean = true,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveMin = if (valueRange != 0f..100f) valueRange.start else minVal
    val effectiveMax = if (valueRange != 0f..100f) valueRange.endInclusive else maxVal
    val effectiveRange = remember(effectiveMin, effectiveMax) { effectiveMin..effectiveMax }
    val neuColors = LocalNeumorphicColors.current
    val focusManager = LocalFocusManager.current

    // Format helper for text field display with thousand separators
    fun formatDisplay(v: Double): String {
        return if (step < 1.0) {
            synchronized(DECIMAL_FORMAT) { DECIMAL_FORMAT.format(v) }
        } else {
            synchronized(INTEGER_FORMAT) { INTEGER_FORMAT.format(kotlin.math.round(v).toLong()) }
        }
    }

    var textInputState by remember { mutableStateOf(formatDisplay(value)) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value, isFocused) {
        if (!isFocused) {
            textInputState = formatDisplay(value)
        }
    }

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
                    
                    // Info button for term explanation modal with 48dp touch target
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button) { onInfoClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .neuConvex(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    lightGradientColor = neuColors.shadowLight.copy(alpha = 0.4f),
                                    darkGradientColor = neuColors.shadowDark.copy(alpha = 0.2f),
                                    cornerRadius = 12.dp,
                                    elevation = 1.dp
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = if (isKurdish) "زانیاری لەسەر $label" else "Information about $label",
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
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
                            .heightIn(min = 48.dp)
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
                                val limited = if (input.length > 25) input.take(25) else input
                                textInputState = limited
                                val cleaned = normalizeNumericInput(limited)
                                val parsed = cleaned.toDoubleOrNull()
                                if (parsed != null && !parsed.isNaN() && !parsed.isInfinite()) {
                                    val clamped = parsed.coerceIn(
                                        effectiveMin.toDouble(),
                                        effectiveMax.toDouble()
                                    )
                                    onValueChange(clamped)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = if (isKurdish) "نووسینی بەهای $label" else "$label numeric input"
                                }
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                    if (!it.isFocused) {
                                        textInputState = formatDisplay(value)
                                    }
                                },
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMain
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (step < 1.0) KeyboardType.Decimal else KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
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

        Spacer(modifier = Modifier.height(16.dp))

        val safeValue = if (value.isNaN() || value.isInfinite()) effectiveMin.toDouble() else value
        val rangeDiff = (effectiveMax - effectiveMin)
        val fraction = if (rangeDiff > 0f) {
            ((safeValue.toFloat() - effectiveMin) / rangeDiff).coerceIn(0f, 1f)
        } else 0f
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        // Bi-Directional Compose Slider with accessibility semantics & systemGestureExclusion
        Slider(
            value = safeValue.toFloat().coerceIn(effectiveMin, effectiveMax),
            onValueChange = { newFloatVal ->
                val safeFloat = if (newFloatVal.isNaN() || newFloatVal.isInfinite()) effectiveMin else newFloatVal
                val clamped = safeFloat.toDouble().coerceIn(
                    effectiveMin.toDouble(),
                    effectiveMax.toDouble()
                )
                textInputState = formatDisplay(clamped)
                onValueChange(clamped)
            },
            valueRange = effectiveRange,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .systemGestureExclusion(),
                    contentAlignment = Alignment.Center
                ) {
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
                                    .background(neuColors.surface)
                                    .align(Alignment.Center)
                            )
                        }
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

                    // Inactive track using high-contrast token ensuring >= 3.0:1 contrast against background in Light and Dark mode
                    val inactiveTrackColor = if (neuColors.isDark) Color(0xFF64748B) else Color(0xFF6B7C93)
                    drawRoundRect(
                        color = inactiveTrackColor,
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
            modifier = Modifier
                .fillMaxWidth()
                .systemGestureExclusion()
                .semantics {
                    contentDescription = "$label: ${formatDisplay(value)} $unit"
                    stateDescription = "${formatDisplay(value)} $unit"
                }
        )
    }
}
