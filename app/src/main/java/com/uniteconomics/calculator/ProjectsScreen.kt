package com.uniteconomics.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.uniteconomics.calculator.db.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PROJECT_DATE_FORMAT = ThreadLocal.withInitial {
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
}

enum class ProjectFilter {
    ALL, PROFIT, LOSS
}

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    activeProjectId: Long? = null,
    isKurdish: Boolean,
    onLoadProject: (ProjectEntity) -> Unit,
    onUnloadProject: () -> Unit = {},
    onDeleteProject: (ProjectEntity) -> Unit,
    onCloneProject: (ProjectEntity) -> Unit = {},
    onRenameProject: (ProjectEntity, newName: String) -> Unit = { _, _ -> },
    onSaveCurrentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(ProjectFilter.ALL) }
    var projectToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var projectToRenameId by rememberSaveable { mutableStateOf<Long?>(null) }

    val projectToDelete = remember(projectToDeleteId, projects) {
        projects.find { it.id == projectToDeleteId }
    }
    val projectToRename = remember(projectToRenameId, projects) {
        projects.find { it.id == projectToRenameId }
    }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(200)
        debouncedQuery = searchQuery
    }

    val filteredProjects = remember(projects, debouncedQuery, selectedFilter) {
        val rawTokens = debouncedQuery.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val normTokens = rawTokens.map { normalizeKurdishSearch(it) }
        projects.filter { proj ->
            val normName = normalizeKurdishSearch(proj.name)
            val matchesSearch = if (rawTokens.isEmpty()) {
                true
            } else {
                normTokens.indices.all { i ->
                    val normTok = normTokens[i]
                    val rawTok = rawTokens[i]
                    (normTok.isNotEmpty() && normName.contains(normTok)) || proj.name.contains(rawTok, ignoreCase = true)
                }
            }
            val matchesFilter = when (selectedFilter) {
                ProjectFilter.ALL -> true
                ProjectFilter.PROFIT -> !proj.isLoss
                ProjectFilter.LOSS -> proj.isLoss
            }
            matchesSearch && matchesFilter
        }
    }

    val searchSuggestions = remember(debouncedQuery, filteredProjects) {
        if (debouncedQuery.isBlank()) emptyList() else filteredProjects.take(4)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthSizeClass = WindowWidthSizeClass.fromWidth(maxWidth)
        val isWideScreen = widthSizeClass != WindowWidthSizeClass.Compact

        LazyColumn(
            modifier = Modifier
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
                                color = neuColors.textMain,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = if (isKurdish) "کۆی گشتی: ${formatKurdishDigits(projects.size)} بەرهەمی تۆمارکراو"
                                       else if (projects.size == 1) "Total: 1 saved product"
                                       else "Total: ${projects.size} saved products",
                                fontSize = 12.sp,
                                color = neuColors.textMuted
                            )
                        }

                        val saveInteractionSource = remember { MutableInteractionSource() }
                        val isSavePressed by saveInteractionSource.collectIsPressedAsState()

                        Box(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .then(
                                    if (isSavePressed) {
                                        Modifier.neuPressed(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            cornerRadius = 12.dp,
                                            elevation = 2.dp
                                        )
                                    } else {
                                        Modifier.neuConvex(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            lightGradientColor = neuColors.primary.copy(alpha = 0.2f),
                                            darkGradientColor = neuColors.primary.copy(alpha = 0.08f),
                                            cornerRadius = 12.dp,
                                            elevation = 3.dp
                                        )
                                    }
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    role = Role.Button,
                                    interactionSource = saveInteractionSource,
                                    indication = ripple(bounded = true, color = neuColors.primary)
                                ) { onSaveCurrentClick() }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = if (isKurdish) "پاشەکەوتکردنی بەرهەم" else "Save Product",
                                    tint = neuColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isKurdish) "پاشەکەوتکردنی ئێستا" else "Save Current",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.primary
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Chips (Only rendered when there are saved projects)
            if (projects.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 14.dp,
                                    elevation = 2.dp
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = if (isKurdish) "گەڕان" else "Search",
                                    tint = neuColors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        showSuggestions = true
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            showSuggestions = false
                                        }
                                    ),
                                    textStyle = TextStyle(
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
                                    cursorBrush = SolidColor(neuColors.primary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics {
                                            contentDescription = if (isKurdish) "گەڕان بەدوای بەرهەمدا" else "Search products"
                                        }
                                )

                                if (searchQuery.isNotEmpty()) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                            .clickable(role = Role.Button) {
                                                searchQuery = ""
                                                debouncedQuery = ""
                                                showSuggestions = false
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = if (isKurdish) "سڕینەوەی گەڕان" else "Clear Search",
                                            tint = neuColors.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

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
                                                .heightIn(min = 48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(role = Role.Button) {
                                                    searchQuery = suggestion.name
                                                    showSuggestions = false
                                                    keyboardController?.hide()
                                                    focusManager.clearFocus()
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
                                                    contentDescription = if (isKurdish) "پێشنیار" else "Suggestion",
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
                                                color = if (suggestion.isLoss) neuColors.loss else neuColors.success,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChipItem(
                                label = if (isKurdish) "هەمووی (${formatKurdishDigits(projects.size)})" else "All (${projects.size})",
                                isSelected = selectedFilter == ProjectFilter.ALL,
                                onClick = { selectedFilter = ProjectFilter.ALL }
                            )
                            FilterChipItem(
                                label = if (isKurdish) "قازانج (${formatKurdishDigits(projects.count { !it.isLoss })})" else "Profit (${projects.count { !it.isLoss }})",
                                isSelected = selectedFilter == ProjectFilter.PROFIT,
                                onClick = { selectedFilter = ProjectFilter.PROFIT }
                            )
                            FilterChipItem(
                                label = if (isKurdish) "زیان (${formatKurdishDigits(projects.count { it.isLoss })})" else "Loss (${projects.count { it.isLoss }})",
                                isSelected = selectedFilter == ProjectFilter.LOSS,
                                onClick = { selectedFilter = ProjectFilter.LOSS }
                            )
                        }
                    }
                }
            }

            // Empty State Handler
            if (filteredProjects.isEmpty()) {
                item {
                    if (projects.isEmpty()) {
                        // Initial Zero State (Empty Database) with Primary CTA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 20.dp,
                                    elevation = 3.dp
                                )
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = if (isKurdish) "هیچ بەرهەمێک نەدۆزرایەوە" else "No Products Found",
                                    tint = neuColors.textMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (isKurdish) "هیچ بەرهەمێکی پاشەکەوتکراو نەدۆزرایەوە" else "No saved products found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textMain
                                )
                                Text(
                                    text = if (isKurdish) "دەتوانیت ئەژمارکردنی ئێستات پاشەکەوت بکەیت بۆ بینینی لێرەدا."
                                           else "You can save your current calculation scenario to manage it here.",
                                    fontSize = 12.sp,
                                    color = neuColors.textMuted,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val ctaInteractionSource = remember { MutableInteractionSource() }
                                val isCtaPressed by ctaInteractionSource.collectIsPressedAsState()

                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .then(
                                            if (isCtaPressed) {
                                                Modifier.neuPressed(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    cornerRadius = 12.dp,
                                                    elevation = 2.dp
                                                )
                                            } else {
                                                Modifier.neuConvex(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    lightGradientColor = neuColors.primary.copy(alpha = 0.25f),
                                                    darkGradientColor = neuColors.primary.copy(alpha = 0.1f),
                                                    cornerRadius = 12.dp,
                                                    elevation = 3.dp
                                                )
                                            }
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(
                                            role = Role.Button,
                                            interactionSource = ctaInteractionSource,
                                            indication = ripple(bounded = true, color = neuColors.primary)
                                        ) { onSaveCurrentClick() }
                                        .padding(horizontal = 20.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isKurdish) "+ پاشەکەوتکردنی ئەژمارکردنی ئێستا" else "+ Save Current Calculation",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.primary
                                    )
                                }
                            }
                        }
                    } else {
                        // Search / Filter Empty State with Clear Filter Action
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 20.dp,
                                    elevation = 3.dp
                                )
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = if (isKurdish) "هیچ بەرهەمێکی هاوتا نەدۆزرایەوە" else "No matching products",
                                    tint = neuColors.textMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = if (isKurdish) "هیچ بەرهەمێکی هاوتا نەدۆزرایەوە" else "No matching products found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.textMain
                                )
                                Text(
                                    text = if (isKurdish) "هیچ بەرهەمێک لەگەڵ وشەی گەڕان یان فلتەرەکەتدا یەکناگرێتەوە."
                                           else "No products match your search query or selected filter.",
                                    fontSize = 12.sp,
                                    color = neuColors.textMuted,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val clearInteractionSource = remember { MutableInteractionSource() }
                                val isClearPressed by clearInteractionSource.collectIsPressedAsState()

                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .then(
                                            if (isClearPressed) {
                                                Modifier.neuPressed(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    cornerRadius = 12.dp,
                                                    elevation = 2.dp
                                                )
                                            } else {
                                                Modifier.neuConvex(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    lightGradientColor = neuColors.primary.copy(alpha = 0.2f),
                                                    darkGradientColor = neuColors.primary.copy(alpha = 0.08f),
                                                    cornerRadius = 12.dp,
                                                    elevation = 3.dp
                                                )
                                            }
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(
                                            role = Role.Button,
                                            interactionSource = clearInteractionSource,
                                            indication = ripple(bounded = true, color = neuColors.primary)
                                        ) {
                                            searchQuery = ""
                                            debouncedQuery = ""
                                            selectedFilter = ProjectFilter.ALL
                                            showSuggestions = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 20.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isKurdish) "پاککردنەوەی گەڕان و فلتەر" else "Clear Search & Filter",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isWideScreen) {
                // Adaptive 2-Column Grid for Tablet & Landscape Viewports
                val chunkedProjects = filteredProjects.chunked(2)
                items(chunkedProjects, key = { chunk -> chunk.joinToString("_") { it.id.toString() } }) { rowProjects ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowProjects.forEach { project ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProjectItemCard(
                                    project = project,
                                    isActive = project.id == activeProjectId,
                                    isKurdish = isKurdish,
                                    onLoad = { onLoadProject(project) },
                                    onUnload = { onUnloadProject() },
                                    onRename = { projectToRenameId = project.id },
                                    onClone = { onCloneProject(project) },
                                    onDelete = { projectToDeleteId = project.id }
                                )
                            }
                        }
                        if (rowProjects.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectItemCard(
                        project = project,
                        isActive = project.id == activeProjectId,
                        isKurdish = isKurdish,
                        onLoad = { onLoadProject(project) },
                        onUnload = { onUnloadProject() },
                        onRename = { projectToRenameId = project.id },
                        onClone = { onCloneProject(project) },
                        onDelete = { projectToDeleteId = project.id }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog (Unified 24dp Neumorphic Dialog with Tablet Constraints)
    projectToDelete?.let { target ->
        val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
        Dialog(
            onDismissRequest = { projectToDeleteId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .widthIn(max = 480.dp)
                        .neuFlat(
                            lightShadowColor = Color.Transparent,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 24.dp,
                            elevation = 8.dp
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isKurdish) "سڕینەوەی بەرهەم" else "Delete Product",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { heading() }
                        )

                        Text(
                            text = if (isKurdish) "دڵنیایت لە سڕینەوەی بەرهەمی «${target.name}»؟"
                                   else "Are you sure you want to delete \"${target.name}\"?",
                            fontSize = 13.sp,
                            color = neuColors.textMuted,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val deleteCancelInteractionSource = remember { MutableInteractionSource() }
                        val isDeleteCancelPressed by deleteCancelInteractionSource.collectIsPressedAsState()

                        val deleteConfirmInteractionSource = remember { MutableInteractionSource() }
                        val isDeleteConfirmPressed by deleteConfirmInteractionSource.collectIsPressedAsState()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .then(
                                        if (isDeleteCancelPressed) {
                                            Modifier.neuPressed(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 12.dp,
                                                elevation = 1.dp
                                            )
                                        } else {
                                            Modifier.neuFlat(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 12.dp,
                                                elevation = 2.dp
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = deleteCancelInteractionSource,
                                        indication = ripple(bounded = true, color = neuColors.textMuted)
                                    ) { projectToDeleteId = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "پاشگەزبوونەوە" else "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = neuColors.textMuted
                                )
                            }

                            // Delete Confirm Button (WCAG AAA contrast in Dark and Light mode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .then(
                                        if (isDeleteConfirmPressed) {
                                            Modifier.neuPressed(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.loss,
                                                cornerRadius = 12.dp,
                                                elevation = 1.dp
                                            )
                                        } else {
                                            Modifier.neuConvex(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.loss,
                                                lightGradientColor = neuColors.loss,
                                                darkGradientColor = neuColors.loss.copy(alpha = 0.85f),
                                                cornerRadius = 12.dp,
                                                elevation = 3.dp
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = deleteConfirmInteractionSource,
                                        indication = ripple(bounded = true, color = Color.White)
                                    ) {
                                        onDeleteProject(target)
                                        projectToDeleteId = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "سڕینەوە" else "Delete",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (neuColors.isDark) NeuDarkOnPrimary else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Product Dialog (Option 2A Dedicated Name Editing)
    projectToRename?.let { target ->
        val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
        RenameProjectDialog(
            project = target,
            isKurdish = isKurdish,
            neuColors = neuColors,
            layoutDirection = layoutDirection,
            onDismiss = { projectToRenameId = null },
            onConfirm = { newName ->
                onRenameProject(target, newName)
                projectToRenameId = null
            }
        )
    }
}

@Composable
private fun RenameProjectDialog(
    project: ProjectEntity,
    isKurdish: Boolean,
    neuColors: NeumorphicColors,
    layoutDirection: LayoutDirection,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameText by rememberSaveable { mutableStateOf(project.name) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    fun handleDismiss() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    fun validateAndSave() {
        val sanitized = nameText.trim().take(40)
        if (sanitized.isBlank()) {
            errorMessage = if (isKurdish) "تکایە ناوێک بنووسە" else "Please enter a valid name"
        } else {
            keyboardController?.hide()
            onConfirm(sanitized)
        }
    }

    Dialog(
        onDismissRequest = { handleDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 480.dp)
                    .neuFlat(
                        lightShadowColor = Color.Transparent,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 24.dp,
                        elevation = 8.dp
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKurdish) "گۆڕینی ناوی بەرهەم" else "Rename Product",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain,
                            modifier = Modifier.semantics { heading() }
                        )

                        val closeInteractionSource = remember { MutableInteractionSource() }
                        val isClosePressed by closeInteractionSource.collectIsPressedAsState()

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (isClosePressed) {
                                        Modifier.neuPressed(
                                            cornerShape = CircleShape,
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            elevation = 1.dp
                                        )
                                    } else {
                                        Modifier.neuFlat(
                                            cornerShape = CircleShape,
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            elevation = 2.dp
                                        )
                                    }
                                )
                                .clip(CircleShape)
                                .clickable(
                                    role = Role.Button,
                                    interactionSource = closeInteractionSource,
                                    indication = ripple(bounded = true, color = neuColors.primary)
                                ) { handleDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isKurdish) "داخستن" else "Close",
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isKurdish) "ناوی نوێ بۆ ئەم بەرهەمە بنووسە:" else "Enter a new name for this product scenario:",
                        fontSize = 13.sp,
                        color = neuColors.textMuted
                    )

                    // Text Input Inset Well
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .neuPressed(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 14.dp,
                                elevation = 2.dp
                            )
                            .border(1.dp, neuColors.border, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = nameText,
                            onValueChange = {
                                if (it.length <= 40) {
                                    nameText = it
                                    if (errorMessage != null) errorMessage = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = neuColors.textMain
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { validateAndSave() }),
                            cursorBrush = SolidColor(neuColors.primary)
                        )
                    }

                    // Character counter and Error Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.sp,
                                color = neuColors.loss,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(
                            text = "${nameText.length} / 40",
                            fontSize = 10.sp,
                            color = neuColors.textMuted
                        )
                    }

                    val cancelInteractionSource = remember { MutableInteractionSource() }
                    val isCancelPressed by cancelInteractionSource.collectIsPressedAsState()

                    val saveInteractionSource = remember { MutableInteractionSource() }
                    val isSavePressed by saveInteractionSource.collectIsPressedAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .then(
                                    if (isCancelPressed) {
                                        Modifier.neuPressed(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            cornerRadius = 12.dp,
                                            elevation = 1.dp
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
                                .border(1.dp, neuColors.border, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    role = Role.Button,
                                    interactionSource = cancelInteractionSource,
                                    indication = ripple(bounded = true, color = neuColors.textMuted)
                                ) { handleDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isKurdish) "پاشگەزبوونەوە" else "Cancel",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = neuColors.textMuted
                            )
                        }

                        // Save Name Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .then(
                                    if (isSavePressed) {
                                        Modifier.neuPressed(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            cornerRadius = 12.dp,
                                            elevation = 2.dp
                                        )
                                    } else {
                                        Modifier.neuConvex(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            lightGradientColor = neuColors.primary.copy(alpha = 0.28f),
                                            darkGradientColor = neuColors.primary.copy(alpha = 0.12f),
                                            cornerRadius = 12.dp,
                                            elevation = 4.dp
                                        )
                                    }
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    role = Role.Button,
                                    interactionSource = saveInteractionSource,
                                    indication = ripple(bounded = true, color = neuColors.primary)
                                ) { validateAndSave() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isKurdish) "پاشەکەوتکردن" else "Save Name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.primary
                            )
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
    val chipInteractionSource = remember { MutableInteractionSource() }
    val isChipPressed by chipInteractionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (isChipPressed || isSelected) {
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
            .clip(RoundedCornerShape(10.dp))
            .semantics {
                stateDescription = if (isSelected) "Selected" else "Not selected"
            }
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                interactionSource = chipInteractionSource,
                indication = ripple(bounded = true, color = neuColors.primary)
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
    isActive: Boolean = false,
    isKurdish: Boolean,
    onLoad: () -> Unit,
    onUnload: () -> Unit = {},
    onRename: () -> Unit = {},
    onClone: () -> Unit = {},
    onDelete: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    val formattedDate = remember(project.updatedAt, project.createdAt) {
        runCatching {
            val targetEpoch = if (project.updatedAt > 0L) project.updatedAt else project.createdAt
            if (targetEpoch <= 0L) {
                "---"
            } else {
                isolateBiDiText(PROJECT_DATE_FORMAT.get()?.format(Date(targetEpoch)) ?: "---")
            }
        }.getOrElse { "---" }
    }

    val isLoss = project.isLoss
    val statusColor = if (isLoss) neuColors.loss else neuColors.success
    val statusBg = statusColor.copy(alpha = 0.12f)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = project.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .semantics { heading() }
                        )
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(neuColors.primary.copy(alpha = 0.12f))
                                    .border(1.dp, neuColors.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .clickable(role = Role.Button) { onUnload() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isKurdish) "چالاکە" else "Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isKurdish) "لابردنی دۆخی چالاک" else "Deactivate project",
                                        tint = neuColors.primary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = neuColors.textMuted
                    )
                }

                // Profit/Loss Badge (Fixed financial non-mirrored arrow)
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
                            contentDescription = if (isKurdish) "بارودۆخ" else "Status",
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
                            letterSpacing = if (isKurdish) 0.sp else 0.04.em,
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
                        value = formatCurrencyAmount(project.retailPrice, isKurdish = isKurdish),
                        isKurdish = isKurdish,
                        neuColors = neuColors
                    )
                    MetricMiniItem(
                        label = if (isKurdish) "مووچە و خەرجی" else "Salaries & Overhead",
                        value = formatCurrencyAmount(project.fixedMonthlyExpenses, isKurdish = isKurdish),
                        isKurdish = isKurdish,
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
                        value = formatCurrencyAmount(project.netProfitTotal, isKurdish = isKurdish),
                        valueColor = statusColor,
                        isKurdish = isKurdish,
                        neuColors = neuColors
                    )
                    MetricMiniItem(
                        label = if (isKurdish) "کۆی داهات" else "Total Revenue",
                        value = formatCurrencyAmount(project.revenue, isKurdish = isKurdish),
                        isKurdish = isKurdish,
                        neuColors = neuColors
                    )
                }
            }

            HorizontalDivider(color = neuColors.border.copy(alpha = 0.4f), thickness = 1.dp)

            // Action Buttons Row (Load / Unload, Rename ✏️, Duplicate 📄, Delete 🗑️)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val loadInteractionSource = remember { MutableInteractionSource() }
                val isLoadPressed by loadInteractionSource.collectIsPressedAsState()

                if (isActive) {
                    // Active State: Inset pressed neumorphic styling. Tapping unloads / deactivates the project.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .then(
                                if (isLoadPressed) {
                                    Modifier.neuFlat(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 12.dp,
                                        elevation = 1.dp
                                    )
                                } else {
                                    Modifier.neuPressed(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 12.dp,
                                        elevation = 2.dp
                                    )
                                }
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                role = Role.Button,
                                interactionSource = loadInteractionSource,
                                indication = ripple(bounded = true, color = neuColors.primary)
                            ) { onUnload() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKurdish) "✓ بارکراوە لە بژمێر" else "✓ Loaded in Calculator",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.primary
                            )
                        }
                    }
                } else {
                    // Inactive State: Elevated convex neumorphic styling. Tapping loads the project into calculator.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .then(
                                if (isLoadPressed) {
                                    Modifier.neuPressed(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        cornerRadius = 12.dp,
                                        elevation = 2.dp
                                    )
                                } else {
                                    Modifier.neuConvex(
                                        lightShadowColor = neuColors.shadowLight,
                                        darkShadowColor = neuColors.shadowDark,
                                        backgroundColor = neuColors.surface,
                                        lightGradientColor = neuColors.primary.copy(alpha = 0.2f),
                                        darkGradientColor = neuColors.primary.copy(alpha = 0.08f),
                                        cornerRadius = 12.dp,
                                        elevation = 3.dp
                                    )
                                }
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                role = Role.Button,
                                interactionSource = loadInteractionSource,
                                indication = ripple(bounded = true, color = neuColors.primary)
                            ) { onLoad() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isKurdish) "بارکردن بۆ بژمێر" else "Load to Calculator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.primary
                        )
                    }
                }

                // Edit / Rename Button (Option 2A, 48dp minimum touch target)
                val renameInteractionSource = remember { MutableInteractionSource() }
                val isRenamePressed by renameInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .then(
                            if (isRenamePressed) {
                                Modifier.neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 1.dp
                                )
                            } else {
                                Modifier.neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 3.dp
                                )
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.Button,
                            interactionSource = renameInteractionSource,
                            indication = ripple(bounded = true, color = neuColors.primary)
                        ) { onRename() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = if (isKurdish) "گۆڕینی ناوی «${project.name}»" else "Rename ${project.name}",
                        tint = neuColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clone / Duplicate Button (48dp minimum touch target)
                val cloneInteractionSource = remember { MutableInteractionSource() }
                val isClonePressed by cloneInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .then(
                            if (isClonePressed) {
                                Modifier.neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 1.dp
                                )
                            } else {
                                Modifier.neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 3.dp
                                )
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.Button,
                            interactionSource = cloneInteractionSource,
                            indication = ripple(bounded = true, color = neuColors.primary)
                        ) { onClone() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = if (isKurdish) "کۆپیکردنی «${project.name}»" else "Duplicate ${project.name}",
                        tint = neuColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button (48dp minimum touch target)
                val deleteInteractionSource = remember { MutableInteractionSource() }
                val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .then(
                            if (isDeletePressed) {
                                Modifier.neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 1.dp
                                )
                            } else {
                                Modifier.neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 3.dp
                                )
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            role = Role.Button,
                            interactionSource = deleteInteractionSource,
                            indication = ripple(bounded = true, color = neuColors.loss)
                        ) { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isKurdish) "سڕینەوەی «${project.name}»" else "Delete ${project.name}",
                        tint = neuColors.loss,
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
    isKurdish: Boolean = false,
    neuColors: NeumorphicColors,
    valueColor: Color = neuColors.textMain
) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) { }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = if (isKurdish) 0.sp else 0.02.em,
            color = neuColors.textMuted
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
