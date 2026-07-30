package com.uniteconomics.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Kurdish Sorani & English bilingual interactive dictionary dialog modal.
 * Displays definitions and e-commerce financial explanations for all calculator terms.
 */
@Composable
fun DictionaryDialog(
    initialTerm: String? = null,
    isKurdish: Boolean = true,
    onDismiss: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    var searchQuery by remember { mutableStateOf("") }
    
    val activeDict = if (isKurdish) KurdishTerms.dictionary else EnglishTerms.dictionary
    val activeGetDesc: (String) -> String = if (isKurdish) { { KurdishTerms.getDescription(it) } } else { { EnglishTerms.getDescription(it) } }

    var selectedTerm by remember(isKurdish) {
        mutableStateOf(initialTerm ?: activeDict.keys.firstOrNull() ?: "")
    }

    val allTerms = remember(isKurdish) { activeDict.keys.toList() }
    val filteredTerms = remember(searchQuery, allTerms) {
        if (searchQuery.isBlank()) {
            allTerms
        } else {
            allTerms.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val currentDescription = remember(selectedTerm, isKurdish) {
        activeGetDesc(selectedTerm)
    }

    val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 24.dp,
                        elevation = 3.dp
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .neuConvex(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        lightGradientColor = neuColors.primary.copy(alpha = 0.2f),
                                        darkGradientColor = neuColors.primary.copy(alpha = 0.05f),
                                        cornerRadius = 12.dp,
                                        elevation = 3.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = "Dictionary",
                                    tint = neuColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (isKurdish) "فەرهەنگی زاراوەکان" else "Financial Dictionary",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = neuColors.textMain
                                )
                                Text(
                                    text = if (isKurdish) "ڕوونکردنەوەی تێرمە ئابوورییەکان" else "E-Commerce Unit Economics Terms",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = neuColors.textMuted
                                )
                            }
                        }

                        // Close Button styled with neuConvex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .neuConvex(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    lightGradientColor = neuColors.shadowLight.copy(alpha = 0.4f),
                                    darkGradientColor = neuColors.shadowDark.copy(alpha = 0.2f),
                                    cornerRadius = 18.dp,
                                    elevation = 2.dp
                                )
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Dialog",
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Filter Box (Search icon mirrors position in RTL)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = if (isKurdish) "گەڕان لە فەرهەنگدا..." else "Search term...",
                                fontSize = 13.sp,
                                color = neuColors.textMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neuColors.primary,
                            unfocusedBorderColor = neuColors.border,
                            focusedContainerColor = neuColors.surface,
                            unfocusedContainerColor = neuColors.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Term Detail View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuPressed(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 16.dp,
                                elevation = 4.dp
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedTerm,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(thickness = 1.dp, color = neuColors.border)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentDescription,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = neuColors.textMain,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isKurdish) "تەواوی زاراوەکان (${filteredTerms.size}):" else "All Terms (${filteredTerms.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = neuColors.textMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable List of All Terms with subtle term card dividers
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTerms) { term ->
                            val isSelected = term == selectedTerm
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isSelected) {
                                            Modifier.neuPressed(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 12.dp,
                                                elevation = 2.dp
                                            )
                                        } else {
                                            Modifier.neuFlat(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 12.dp,
                                                elevation = 1.dp
                                            )
                                        }
                                    )
                                    .clickable { selectedTerm = term }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = term,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) neuColors.primary else neuColors.textMain
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

