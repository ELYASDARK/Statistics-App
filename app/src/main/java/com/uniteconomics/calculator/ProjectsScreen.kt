package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniteconomics.calculator.db.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ProjectFilter {
    ALL, PROFIT, LOSS
}

/**
 * Normalizes Kurdish (Sorani/Kurmanji) & Arabic/Persian character variations
 * so search works seamlessly regardless of keyboard variant.
 */
fun normalizeSearchText(input: String): String {
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

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    isKurdish: Boolean,
    onLoadProject: (ProjectEntity) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onSaveCurrentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(ProjectFilter.ALL) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredProjects = remember(projects, searchQuery, selectedFilter) {
        val normQuery = normalizeSearchText(searchQuery)
        projects.filter { proj ->
            val normName = normalizeSearchText(proj.name)
            val matchesSearch = normQuery.isEmpty() || normName.contains(normQuery) || proj.name.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                ProjectFilter.ALL -> true
                ProjectFilter.PROFIT -> !proj.isLoss
                ProjectFilter.LOSS -> proj.isLoss
            }
            matchesSearch && matchesFilter
        }
    }

    val searchSuggestions = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val normQuery = normalizeSearchText(searchQuery)
            projects.filter { proj ->
                val normName = normalizeSearchText(proj.name)
                normName.contains(normQuery) || proj.name.contains(searchQuery, ignoreCase = true)
            }.take(4)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Title Card
        item {
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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isKurdish) "بەرهەمە پاشەکەوتکراوەکان" else "Saved Products",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain
                        )
                        Text(
                            text = if (isKurdish) "کۆی گشتی: ${projects.size} بەرهەمی تۆمارکراو"
                                   else if (projects.size == 1) "Total: 1 saved product"
                                   else "Total: ${projects.size} saved products",
                            fontSize = 12.sp,
                            color = neuColors.textMuted
                        )
                    }

                    // Save Current Product Button (Clean Black in Light Mode)
                    val isDark = neuColors.isDark
                    val saveHeaderBg = if (isDark) Color.White else Color(0xFF171C21)
                    val saveHeaderContent = if (isDark) Color(0xFF171C21) else Color.White

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = saveHeaderBg,
                                cornerRadius = 12.dp,
                                elevation = 3.dp
                            )
                            .clickable { onSaveCurrentClick() }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = saveHeaderContent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isKurdish) "پاشەکەوتکردن" else "Save Current",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = saveHeaderContent,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input inside NeuPressed Container using BasicTextField (No bottom text clipping)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 14.dp,
                            elevation = 2.dp
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = neuColors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                showSuggestions = true
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = neuColors.textMain,
                                fontWeight = FontWeight.Medium
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = if (isKurdish) "گەڕان بەدوای بەرهەمدا..." else "Search products...",
                                            color = neuColors.textMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = neuColors.textMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        searchQuery = ""
                                        showSuggestions = false
                                    }
                            )
                        }
                    }
                }

                // Interactive Hover / Auto-Suggest Dropdown Popup Card
                if (showSuggestions && searchSuggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuFlat(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 14.dp,
                                elevation = 4.dp
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isKurdish) "پێشنیارەکان" else "Suggestions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.textMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            searchSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            searchQuery = suggestion.name
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Suggestion",
                                            tint = neuColors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = suggestion.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = neuColors.textMain,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = if (suggestion.isLoss) (if (isKurdish) "زیان" else "Loss")
                                               else (if (isKurdish) "قازانج" else "Profit"),
                                        fontSize = 11.sp,
                                        color = if (suggestion.isLoss) Color(0xFFFF5252) else Color(0xFF00C853),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem(
                        label = if (isKurdish) "هەمووی (${projects.size})" else "All (${projects.size})",
                        isSelected = selectedFilter == ProjectFilter.ALL,
                        onClick = { selectedFilter = ProjectFilter.ALL },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChipItem(
                        label = if (isKurdish) "قازانج (${projects.count { !it.isLoss }})" else "Profit (${projects.count { !it.isLoss }})",
                        isSelected = selectedFilter == ProjectFilter.PROFIT,
                        onClick = { selectedFilter = ProjectFilter.PROFIT },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChipItem(
                        label = if (isKurdish) "زیان (${projects.count { it.isLoss }})" else "Loss (${projects.count { it.isLoss }})",
                        isSelected = selectedFilter == ProjectFilter.LOSS,
                        onClick = { selectedFilter = ProjectFilter.LOSS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Empty State Handler
        if (filteredProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                        .neuFlat(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 20.dp,
                            elevation = 3.dp
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            tint = neuColors.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isKurdish) "هیچ زانیارییەک نەدۆزرایەوە" else "No saved items found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain
                        )
                        Text(
                            text = if (isKurdish) "دەتوانیت ئەژمارکردنی ئێستات پاشەکەوت بکەیت بۆ بینینی لەێرەدا."
                                   else "You can save your current calculation to view it here.",
                            fontSize = 12.sp,
                            color = neuColors.textMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredProjects, key = { it.id }) { project ->
                ProjectItemCard(
                    project = project,
                    isKurdish = isKurdish,
                    onLoad = { onLoadProject(project) },
                    onDelete = { projectToDelete = project }
                )
            }
        }
    }

    // Delete Confirmation Dialog (Root Neumorphic Dialog Fix matching SaveProjectModal)
    projectToDelete?.let { target ->
        val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
        Dialog(
            onDismissRequest = { projectToDelete = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .neuFlat(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 24.dp,
                            elevation = 8.dp
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isKurdish) "سڕینەوەی بەرهەم" else "Delete Product",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (isKurdish) "دڵنیایت لە سڕینەوەی بەرهەمی '${target.name}'؟"
                                   else "Are you sure you want to delete '${target.name}'?",
                            fontSize = 13.sp,
                            color = neuColors.textMuted,
                            modifier = Modifier.fillMaxWidth()
                        )

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
                                    .clickable { projectToDelete = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "پاشگەزبوونەوە" else "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = neuColors.textMuted
                                )
                            }

                            // Delete Confirm Button (Clean Black in Light Mode)
                            val isDark = neuColors.isDark
                            val delBtnBg = if (isDark) Color.White else Color(0xFF171C21)
                            val delBtnText = if (isDark) Color(0xFF171C21) else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .neuConvex(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = delBtnBg,
                                        cornerRadius = 12.dp,
                                        elevation = 3.dp
                                    )
                                    .clickable {
                                        onDeleteProject(target)
                                        projectToDelete = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "سڕینەوە" else "Delete",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = delBtnText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .height(38.dp)
            .then(
                if (isSelected) {
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
                        elevation = 2.dp
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) neuColors.primary else neuColors.textMuted
        )
    }
}

@Composable
private fun ProjectItemCard(
    project: ProjectEntity,
    isKurdish: Boolean,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    val formattedDate = remember(project.updatedAt) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        sdf.format(Date(project.updatedAt))
    }

    val isLoss = project.isLoss
    val statusColor = if (isLoss) Color(0xFFFF5252) else Color(0xFF00C853)
    val statusBg = if (isLoss) Color(0xFFFF5252).copy(alpha = 0.12f) else Color(0xFF00C853).copy(alpha = 0.12f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 18.dp,
                elevation = 4.dp
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Title & Profit/Loss Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = project.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = neuColors.textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = neuColors.textMuted
                    )
                }

                // Profit/Loss Badge
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLoss) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Status",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLoss) {
                                if (isKurdish) "زیان" else "Loss"
                            } else {
                                if (isKurdish) "قازانج" else "Profit"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            HorizontalDivider(color = neuColors.border.copy(alpha = 0.4f), thickness = 1.dp)

            // Grid Metrics Display (2 columns x 2 rows)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column 1
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniItem(
                        label = if (isKurdish) "نرخی فرۆشتن" else "Retail Price",
                        value = "${formatNumber(project.retailPrice)} IQD",
                        neuColors = neuColors
                    )
                    MetricMiniItem(
                        label = if (isKurdish) "مووچە & خەرجی" else "Salaries & Overhead",
                        value = "${formatNumber(project.fixedMonthlyExpenses)} IQD",
                        neuColors = neuColors
                    )
                }

                // Column 2
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniItem(
                        label = if (isKurdish) "قازانج / زیانی پوختە" else "Net Profit / Loss",
                        value = "${formatNumber(project.netProfitTotal)} IQD",
                        valueColor = statusColor,
                        neuColors = neuColors
                    )
                    MetricMiniItem(
                        label = if (isKurdish) "داهات" else "Total Revenue",
                        value = "${formatNumber(project.revenue)} IQD",
                        neuColors = neuColors
                    )
                }
            }

            HorizontalDivider(color = neuColors.border.copy(alpha = 0.4f), thickness = 1.dp)

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Load Button (Clean Black in Light Mode)
                val isDark = neuColors.isDark
                val loadBtnBg = if (isDark) Color.White else Color(0xFF171C21)
                val loadBtnText = if (isDark) Color(0xFF171C21) else Color.White

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = loadBtnBg,
                            cornerRadius = 12.dp,
                            elevation = 3.dp
                        )
                        .clickable { onLoad() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKurdish) "تێکردن بۆ حاسیبە" else "Load to Calculator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = loadBtnText
                        )
                    }
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp)
                        .neuFlat(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 12.dp,
                            elevation = 3.dp
                        )
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniItem(
    label: String,
    value: String,
    neuColors: NeumorphicColors,
    valueColor: Color = neuColors.textMain
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = neuColors.textMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
