package com.uniteconomics.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.uniteconomics.calculator.db.ProjectEntity

private const val MAX_NAME_LENGTH = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveProjectModal(
    inputs: CalculatorInputs,
    result: CalculationResult,
    activeLoadedProject: ProjectEntity? = null,
    isKurdish: Boolean,
    onDismiss: () -> Unit,
    onSave: (projectName: String, saveAsNew: Boolean) -> Unit
) {
    var isSavingAsNew by rememberSaveable { mutableStateOf(activeLoadedProject == null) }
    val initialText = if (activeLoadedProject != null && isSavingAsNew) "${activeLoadedProject.name} (Copy)" else ""
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(0, initialText.length)))
    }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isInputFocused by remember { mutableStateOf(false) }
    val neuColors = LocalNeumorphicColors.current
    val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSavingAsNew) {
        if (isSavingAsNew) {
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
        }
    }

    fun sanitizeProjectName(input: String): String {
        return input
            .replace(Regex("[\\r\\n\\t\\u0000-\\u001F\\u007F-\\u009F\\u200B-\\u200D\\uFEFF\\u202A-\\u202E\\u2066-\\u2069]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(MAX_NAME_LENGTH)
    }

    fun handleDismiss() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    fun validateAndSaveAsNew() {
        val sanitized = sanitizeProjectName(textFieldValue.text)
        when {
            sanitized.isBlank() -> {
                errorMessage = if (isKurdish) "تکایە ناوێک بنووسە پێش پاشەکەوتکردن" else "Please enter a valid name"
            }
            sanitized.length > MAX_NAME_LENGTH -> {
                errorMessage = if (isKurdish) "ناوی بەرهەم نابێت لە 40 پیت زیاتر بێت" else "Product name cannot exceed 40 characters"
            }
            else -> {
                errorMessage = null
                keyboardController?.hide()
                focusManager.clearFocus()
                onSave(sanitized, true)
            }
        }
    }

    fun updateExistingNumbers() {
        if (activeLoadedProject != null) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onSave(activeLoadedProject.name, false)
        }
    }

    BasicAlertDialog(
        onDismissRequest = { handleDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 520.dp)
                    .imePadding()
                    .neuFlat(
                        lightShadowColor = Color.Transparent,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 24.dp,
                        elevation = 8.dp
                    )
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Title & Neumorphic Close Button (Option 3A)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeLoadedProject != null && !isSavingAsNew) {
                                if (isKurdish) "نوێکردنەوەی ژمارەکان" else "Update Scenario"
                            } else if (activeLoadedProject != null) {
                                if (isKurdish) "پاشەکەوتکردن وەک بەرهەمی نوێ" else "Save as New Product"
                            } else {
                                if (isKurdish) "پاشەکەوتکردنی بەرهەم" else "Save Product"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = neuColors.textMain,
                            modifier = Modifier.semantics { heading() }
                        )

                        // Top-Right Close Button
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

                    // Active Loaded Project Notification Banner (Shown when loaded project exists)
                    if (activeLoadedProject != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(neuColors.primary.copy(alpha = 0.08f))
                                .border(
                                    width = 1.dp,
                                    color = neuColors.primary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = neuColors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (isKurdish) "بەرهەمی بارکراوی بنچینەیی:" else "Loaded Base Scenario:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = neuColors.primary
                                    )
                                    Text(
                                        text = "«${activeLoadedProject.name}»",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.textMain
                                    )
                                }
                            }
                        }
                    }

                    // Descriptive Subtitle
                    Text(
                        text = if (activeLoadedProject != null && !isSavingAsNew) {
                            if (isKurdish)
                                "دەتوانیت ژمارە نوێکراوەکان ڕاستەوخۆ لەسەر ئەم بەرهەمە نوێ بکەیتەوە بەبێ پێویستی بە دووبارە نووسینەوەی ناو."
                            else
                                "Update the latest calculation numbers directly for this loaded product scenario without re-entering a name."
                        } else if (activeLoadedProject != null) {
                            if (isKurdish)
                                "ناوێک بۆ ئەم کۆپییە نوێیە دابنێ بۆ ئەوەی بە جیا لەگەڵ بەرهەمە سەرەکییەکە پاشەکەوت بکرێت."
                            else
                                "Enter a name for this new product copy to save it independently from the original scenario."
                        } else {
                            if (isKurdish)
                                "ناوێک بۆ ئەم بەرهەمە دابنێ بۆ ئەوەی لە لیستی بەرهەمەکاندا پاشەکەوت بکرێت."
                            else
                                "Enter a name for this product scenario to save it to your local database."
                        },
                        fontSize = 12.sp,
                        color = neuColors.textMuted,
                        lineHeight = 18.sp
                    )

                    // Text Input Well (Only visible when saving fresh or explicitly saving as new)
                    AnimatedVisibility(
                        visible = isSavingAsNew || activeLoadedProject == null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Text(
                                text = if (isKurdish) "ناوی بەرهەم *" else "Product Name *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMain
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val isError = errorMessage != null
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .then(
                                        when {
                                            isError -> Modifier.border(1.dp, neuColors.loss.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                                            isInputFocused -> Modifier.border(1.dp, neuColors.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                            else -> Modifier
                                        }
                                    )
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
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (textFieldValue.text.isEmpty()) {
                                            Text(
                                                text = if (isKurdish) "بۆ نموونە: پێڵاوی چەرم - مۆدێل A" else "e.g. Leather Shoes - Model A",
                                                fontSize = 13.sp,
                                                color = neuColors.textMuted
                                            )
                                        }

                                        BasicTextField(
                                            value = textFieldValue,
                                            onValueChange = {
                                                if (it.text.length <= MAX_NAME_LENGTH) {
                                                    textFieldValue = it
                                                    if (errorMessage != null) errorMessage = null
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(focusRequester)
                                                .onFocusChanged { isInputFocused = it.isFocused }
                                                .semantics {
                                                    contentDescription = if (isKurdish) "ناوی بەرهەم" else "Product Name"
                                                },
                                            textStyle = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = neuColors.textMain
                                            ),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(onDone = { validateAndSaveAsNew() }),
                                            cursorBrush = SolidColor(neuColors.primary)
                                        )
                                    }

                                    // Trailing Clear ('X') Button
                                    if (textFieldValue.text.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .clickable(role = Role.Button) {
                                                    textFieldValue = TextFieldValue("", TextRange.Zero)
                                                    if (errorMessage != null) errorMessage = null
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = if (isKurdish) "سڕینەوەی ناو" else "Clear text",
                                                tint = neuColors.textMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Character Counter and Error Message Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage ?: "",
                                        fontSize = 11.sp,
                                        color = neuColors.loss,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .semantics { liveRegion = LiveRegionMode.Polite }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Text(
                                    text = "${textFieldValue.text.length} / $MAX_NAME_LENGTH",
                                    fontSize = 10.sp,
                                    color = neuColors.textMuted
                                )
                            }
                        }
                    }

                    // Key Summary Snapshot Preview Card (Recessed Inset Well with Border)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neuPressed(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 14.dp,
                                elevation = 2.dp
                            )
                            .border(1.dp, neuColors.border, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isKurdish) "پوختەی داتاکانی ئەم بەرهەمە:" else "Financial Highlights Snapshot:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = neuColors.primary
                            )

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
                                    text = formatCurrencyAmount(inputs.retailPrice, isKurdish = isKurdish),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
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
                                    text = if (isKurdish) "کۆی داهات:" else "Total Revenue:",
                                    fontSize = 12.sp,
                                    color = neuColors.textMuted,
                                    maxLines = 1
                                )
                                Text(
                                    text = formatCurrencyAmount(result.revenue, isKurdish = isKurdish),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
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
                                val statusColor = if (result.isLoss) neuColors.loss else neuColors.success
                                val profitLossText = formatCurrencyAmount(result.netProfitTotal, isKurdish = isKurdish)

                                Text(
                                    text = profitLossText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Action Buttons Hierarchy (Option 1A Smart Dynamic Layout)
                    if (activeLoadedProject != null && !isSavingAsNew) {
                        // Quick Update Mode: Primary [Update Numbers], Secondary [+ Save as New Product]
                        val updateInteractionSource = remember { MutableInteractionSource() }
                        val isUpdatePressed by updateInteractionSource.collectIsPressedAsState()

                        val saveNewInteractionSource = remember { MutableInteractionSource() }
                        val isSaveNewPressed by saveNewInteractionSource.collectIsPressedAsState()

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Action: Update Numbers Directly
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp)
                                    .then(
                                        if (isUpdatePressed) {
                                            Modifier.neuPressed(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 14.dp,
                                                elevation = 2.dp
                                            )
                                        } else {
                                            Modifier.neuConvex(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                lightGradientColor = neuColors.primary.copy(alpha = 0.28f),
                                                darkGradientColor = neuColors.primary.copy(alpha = 0.12f),
                                                cornerRadius = 14.dp,
                                                elevation = 4.dp
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = updateInteractionSource,
                                        indication = ripple(bounded = true, color = neuColors.primary)
                                    ) {
                                        updateExistingNumbers()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                        tint = neuColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isKurdish) "نوێکردنەوەی ژمارەکان بۆ «${activeLoadedProject.name}»"
                                               else "Update Numbers for \"${activeLoadedProject.name}\"",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = neuColors.primary
                                    )
                                }
                            }

                            // Secondary Action: Branch into Save as New
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 46.dp)
                                    .then(
                                        if (isSaveNewPressed) {
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
                                    .border(
                                        1.dp,
                                        neuColors.primary.copy(alpha = 0.35f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = saveNewInteractionSource,
                                        indication = ripple(bounded = true, color = neuColors.primary)
                                    ) {
                                        if (textFieldValue.text.isBlank()) {
                                            val copyName = "${activeLoadedProject.name} (Copy)"
                                            textFieldValue = TextFieldValue(copyName, TextRange(0, copyName.length))
                                        }
                                        isSavingAsNew = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "+ پاشەکەوتکردن وەک بەرهەمی نوێ" else "+ Save as New Product",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = neuColors.textMain
                                )
                            }
                        }
                    } else if (activeLoadedProject != null && isSavingAsNew) {
                        // Save As New Mode (from loaded project): Primary [Save as New], Secondary [← Back to Update]
                        val saveNewInteractionSource = remember { MutableInteractionSource() }
                        val isSaveNewPressed by saveNewInteractionSource.collectIsPressedAsState()

                        val backInteractionSource = remember { MutableInteractionSource() }
                        val isBackPressed by backInteractionSource.collectIsPressedAsState()

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Save as New Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 50.dp)
                                    .then(
                                        if (isSaveNewPressed) {
                                            Modifier.neuPressed(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                cornerRadius = 14.dp,
                                                elevation = 2.dp
                                            )
                                        } else {
                                            Modifier.neuConvex(
                                                lightShadowColor = neuColors.shadowLight,
                                                darkShadowColor = neuColors.shadowDark,
                                                backgroundColor = neuColors.surface,
                                                lightGradientColor = neuColors.primary.copy(alpha = 0.28f),
                                                darkGradientColor = neuColors.primary.copy(alpha = 0.12f),
                                                cornerRadius = 14.dp,
                                                elevation = 4.dp
                                            )
                                        }
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = saveNewInteractionSource,
                                        indication = ripple(bounded = true, color = neuColors.primary)
                                    ) {
                                        validateAndSaveAsNew()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isKurdish) "پاشەکەوتکردنی بەرهەمی نوێ" else "Save as New Product",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = neuColors.primary
                                )
                            }

                            // Secondary: Return to Update
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 46.dp)
                                    .then(
                                        if (isBackPressed) {
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
                                    .border(
                                        1.dp,
                                        neuColors.border,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        role = Role.Button,
                                        interactionSource = backInteractionSource,
                                        indication = ripple(bounded = true, color = neuColors.textMuted)
                                    ) {
                                        isSavingAsNew = false
                                        errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = neuColors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isKurdish) "گەڕانەوە بۆ نوێکردنەوە" else "Back to Update",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = neuColors.textMuted
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard Single Save Button for Fresh Project
                        val saveInteractionSource = remember { MutableInteractionSource() }
                        val isSavePressed by saveInteractionSource.collectIsPressedAsState()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .then(
                                    if (isSavePressed) {
                                        Modifier.neuPressed(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            cornerRadius = 14.dp,
                                            elevation = 2.dp
                                        )
                                    } else {
                                        Modifier.neuConvex(
                                            lightShadowColor = neuColors.shadowLight,
                                            darkShadowColor = neuColors.shadowDark,
                                            backgroundColor = neuColors.surface,
                                            lightGradientColor = neuColors.primary.copy(alpha = 0.28f),
                                            darkGradientColor = neuColors.primary.copy(alpha = 0.12f),
                                            cornerRadius = 14.dp,
                                            elevation = 4.dp
                                        )
                                    }
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    role = Role.Button,
                                    interactionSource = saveInteractionSource,
                                    indication = ripple(bounded = true, color = neuColors.primary)
                                ) {
                                    validateAndSaveAsNew()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isKurdish) "پاشەکەوتکردنی بەرهەم" else "Save Product",
                                fontSize = 14.sp,
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
