package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Locale

/**
 * Normalizes Kurdish (Sorani/Kurmanji) & Arabic/Persian character variations
 * so search works seamlessly regardless of keyboard variant.
 */
private fun normalizeKurdishSearch(input: String): String {
    return input.lowercase(Locale.ROOT)
        .replace('ي', 'ی')
        .replace('ى', 'ی')
        .replace('ێ', 'ی')
        .replace('ك', 'ک')
        .replace('ۆ', 'و')
        .replace('ڕ', 'ر')
        .replace('ڵ', 'ل')
        .replace('ە', 'ه')
        .trim()
}

/**
 * Kurdish Sorani & English bilingual interactive dictionary dialog modal.
 * Displays definitions and e-commerce financial explanations for all calculator terms.
 * Features instant live auto-suggest hover dropdown, bilingual Kurdish/English cross-matching,
 * and 1-tap clear query support.
 */
@Composable
fun DictionaryDialog(
    initialTerm: String? = null,
    isKurdish: Boolean = true,
    onDismiss: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    val activeDict = if (isKurdish) KurdishTerms.dictionary else EnglishTerms.dictionary
    val altDict = if (isKurdish) EnglishTerms.dictionary else KurdishTerms.dictionary
    val activeGetDesc: (String) -> String = if (isKurdish) { { KurdishTerms.getDescription(it) } } else { { EnglishTerms.getDescription(it) } }

    var selectedTerm by remember(isKurdish) {
        mutableStateOf(initialTerm ?: activeDict.keys.firstOrNull() ?: "")
    }

    val allTermsList = remember(isKurdish) { activeDict.keys.toList() }
    val altTermsList = remember(isKurdish) { altDict.keys.toList() }

    // Instant bilingual search filtering with Kurdish character normalization
    val filteredTerms = remember(searchQuery, isKurdish, allTermsList) {
        if (searchQuery.isBlank()) {
            allTermsList
        } else {
            val normQuery = normalizeKurdishSearch(searchQuery)
            allTermsList.filterIndexed { index, term ->
                val normTerm = normalizeKurdishSearch(term)
                val normAltTerm = if (index < altTermsList.size) normalizeKurdishSearch(altTermsList[index]) else ""
                val desc = normalizeKurdishSearch(activeGetDesc(term))

                normTerm.contains(normQuery) ||
                normAltTerm.contains(normQuery) ||
                desc.contains(normQuery)
            }
        }
    }

    // Live Auto-Suggest Suggestions (Top 4 matching results as user types 1 or 2 characters)
    val autoSuggestList = remember(searchQuery, filteredTerms) {
        if (searchQuery.isBlank()) emptyList() else filteredTerms.take(4)
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
                    .fillMaxHeight(0.88f)
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
                                        lightGradientColor = neuColors.shadowLight.copy(alpha = 0.6f),
                                        darkGradientColor = neuColors.shadowDark.copy(alpha = 0.3f),
                                        cornerRadius = 12.dp,
                                        elevation = 3.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = "Dictionary",
                                    tint = neuColors.textMuted,
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
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onDismiss() },
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

                    // Search Filter Box with Trailing Clear Button & Instant Suggestion Dropdown Container
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                                    tint = neuColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = neuColors.textMuted,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { searchQuery = "" }
                                    )
                                }
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
                    }

                    // Live Auto-Suggest Floating Dropdown Panel (Appears as user types 1 or 2 characters)
                    AnimatedVisibility(
                        visible = autoSuggestList.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neuFlat(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 16.dp,
                                        elevation = 6.dp
                                    )
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isKurdish) "ئەنجامە پێشنیارکراوەکان:" else "Suggested Results:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                    autoSuggestList.forEach { suggestTerm ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (suggestTerm == selectedTerm) neuColors.primary.copy(alpha = 0.1f)
                                                    else neuColors.surface
                                                )
                                                .clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ) {
                                                    selectedTerm = suggestTerm
                                                    searchQuery = suggestTerm
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = suggestTerm,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = neuColors.textMain,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = if (isKurdish) "دیاریکردن" else "Select",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = neuColors.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { selectedTerm = term }
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

