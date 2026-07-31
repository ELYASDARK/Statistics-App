package com.uniteconomics.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calculator.app.data.CalculationResult
import com.calculator.app.data.CalculatorInputs

@Composable
fun SaveProjectModal(
    inputs: CalculatorInputs,
    result: CalculationResult,
    isKurdish: Boolean,
    onDismiss: () -> Unit,
    onSave: (projectName: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val neuColors = LocalNeumorphicColors.current
    val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 24.dp,
                        elevation = 4.dp
                    )
                    .padding(24.dp)
            ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title (Cleaned to Product as requested)
                Text(
                    text = if (isKurdish) "پاشەکەوتکردنی بەرهەم" else "Save Product",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMain,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (isKurdish) "تکایە ناوێک بنووسە بۆ تۆمارکردنی بەرهەم:"
                           else "Please enter a name to save this product:",
                    fontSize = 13.sp,
                    color = neuColors.textMuted,
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Field inside NeuPressed Container using BasicTextField
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 14.dp,
                            elevation = 3.dp
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            showError = false
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = neuColors.textMain,
                            fontWeight = FontWeight.Medium
                        ),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (projectName.isEmpty()) {
                                    Text(
                                        text = if (isKurdish) "نموونە: بەرهەمی نوێ" else "e.g., Winter Jacket",
                                        color = neuColors.textMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showError) {
                    Text(
                        text = if (isKurdish) "تکایە ناوێک بنووسە پێش پاشەکەوتکردن" else "Please enter a valid name",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Summary Financial Highlights Preview (Fixed Layout Spacing & No Wrapping/Overlapping)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neuFlat(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 14.dp,
                            elevation = 2.dp
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) "نرخی فرۆشتن:" else "Retail Price:",
                                fontSize = 12.sp,
                                color = neuColors.textMuted,
                                maxLines = 1
                            )
                            Text(
                                text = "${formatNumber(inputs.effRetailPrice)} IQD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.textMain,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) "مووچە و خەرجی:" else "Salaries & Overhead:",
                                fontSize = 12.sp,
                                color = neuColors.textMuted,
                                maxLines = 1
                            )
                            Text(
                                text = "${formatNumber(inputs.effFixedMonthlyExpenses)} IQD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.textMain,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) "قازانج / زیانی پوختە:" else "Net Profit / Loss:",
                                fontSize = 12.sp,
                                color = neuColors.textMuted,
                                maxLines = 1
                            )
                            val statusColor = if (result.isLoss) Color(0xFFFF5252) else Color(0xFF00C853)
                            Text(
                                text = "${formatNumber(result.netProfitTotal)} IQD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .neuFlat(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 12.dp,
                                elevation = 3.dp
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isKurdish) "داخستن" else "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = neuColors.textMuted
                        )
                    }

                    // Save Button (Clean Black in Light Mode)
                    val isDark = neuColors.isDark
                    val saveBtnBg = if (isDark) Color.White else Color(0xFF171C21)
                    val saveBtnText = if (isDark) Color(0xFF171C21) else Color.White

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = saveBtnBg,
                                cornerRadius = 12.dp,
                                elevation = 3.dp
                            )
                            .clickable {
                                if (projectName.trim().isEmpty()) {
                                    showError = true
                                } else {
                                    onSave(projectName.trim())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isKurdish) "پاشەکەوتکردن" else "Save Product",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = saveBtnText
                        )
                    }
                }
            }
        }
    }
}
}
