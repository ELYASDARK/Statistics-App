package com.uniteconomics.calculator

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniteconomics.calculator.db.ProjectEntity
import com.uniteconomics.calculator.db.ProjectSQLiteDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Saver implementation for CalculatorInputs to store/restore state in rememberSaveable.
 */
private val CalculatorInputsSaver = mapSaver(
    save = { inputs: CalculatorInputs ->
        mapOf(
            "retailPrice" to inputs.retailPrice,
            "sourcingCost" to inputs.sourcingCost,
            "aovMultiplier" to inputs.aovMultiplier,
            "discounts" to inputs.discounts,
            "cac" to inputs.cac,
            "adFee" to inputs.adFee,
            "ltvMultiplier" to inputs.ltvMultiplier,
            "shippingCost" to inputs.shippingCost,
            "opsCost" to inputs.opsCost,
            "rejectionRate" to inputs.rejectionRate,
            "rtsShippingFee" to inputs.rtsShippingFee,
            "refundRate" to inputs.refundRate,
            "refundPenalty" to inputs.refundPenalty,
            "damageRate" to inputs.damageRate,
            "deadstockRate" to inputs.deadstockRate,
            "fixedMonthlyExpenses" to inputs.fixedMonthlyExpenses,
            "targetProfitGoal" to inputs.targetProfitGoal,
            "projectDurationDays" to inputs.projectDurationDays,
            "capitalRemittanceFrequency" to inputs.capitalRemittanceFrequency,
            "netTimeframe" to inputs.netTimeframe.name,
            "revTimeframe" to inputs.revTimeframe.name
        )
    },
    restore = { map: Map<String, Any?> ->
        CalculatorInputs(
            retailPrice = (map["retailPrice"] as? Number)?.toDouble() ?: 35000.0,
            sourcingCost = (map["sourcingCost"] as? Number)?.toDouble() ?: 10000.0,
            aovMultiplier = (map["aovMultiplier"] as? Number)?.toDouble() ?: 1.0,
            discounts = (map["discounts"] as? Number)?.toDouble() ?: 0.0,
            cac = (map["cac"] as? Number)?.toDouble() ?: 5000.0,
            adFee = (map["adFee"] as? Number)?.toDouble() ?: 3.0,
            ltvMultiplier = (map["ltvMultiplier"] as? Number)?.toDouble() ?: 1.1,
            shippingCost = (map["shippingCost"] as? Number)?.toDouble() ?: 4000.0,
            opsCost = (map["opsCost"] as? Number)?.toDouble() ?: 750.0,
            rejectionRate = (map["rejectionRate"] as? Number)?.toDouble() ?: 20.0,
            rtsShippingFee = (map["rtsShippingFee"] as? Number)?.toDouble() ?: 0.0,
            refundRate = (map["refundRate"] as? Number)?.toDouble() ?: 1.0,
            refundPenalty = (map["refundPenalty"] as? Number)?.toDouble() ?: 5000.0,
            damageRate = (map["damageRate"] as? Number)?.toDouble() ?: 5.0,
            deadstockRate = (map["deadstockRate"] as? Number)?.toDouble() ?: 2.0,
            fixedMonthlyExpenses = (map["fixedMonthlyExpenses"] as? Number)?.toDouble() ?: 450000.0,
            targetProfitGoal = (map["targetProfitGoal"] as? Number)?.toDouble() ?: 1000000.0,
            projectDurationDays = (map["projectDurationDays"] as? Number)?.toDouble() ?: 30.0,
            capitalRemittanceFrequency = (map["capitalRemittanceFrequency"] as? Number)?.toDouble() ?: 7.0,
            netTimeframe = try { TimeframeOption.valueOf(map["netTimeframe"] as? String ?: "TOTAL") } catch (e: Exception) { TimeframeOption.TOTAL },
            revTimeframe = try { TimeframeOption.valueOf(map["revTimeframe"] as? String ?: "TOTAL") } catch (e: Exception) { TimeframeOption.TOTAL }
        )
    }
)

/**
 * Saver implementation for ProjectEntity to preserve active loaded project across configuration changes.
 */
private val ProjectEntitySaver = mapSaver<ProjectEntity?>(
    save = { entity ->
        if (entity == null) emptyMap()
        else mapOf(
            "id" to entity.id,
            "name" to entity.name,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "retailPrice" to entity.retailPrice,
            "sourcingCost" to entity.sourcingCost,
            "aovMultiplier" to entity.aovMultiplier,
            "discounts" to entity.discounts,
            "cac" to entity.cac,
            "adFee" to entity.adFee,
            "ltvMultiplier" to entity.ltvMultiplier,
            "shippingCost" to entity.shippingCost,
            "opsCost" to entity.opsCost,
            "rejectionRate" to entity.rejectionRate,
            "rtsShippingFee" to entity.rtsShippingFee,
            "refundRate" to entity.refundRate,
            "refundPenalty" to entity.refundPenalty,
            "damageRate" to entity.damageRate,
            "deadstockRate" to entity.deadstockRate,
            "fixedMonthlyExpenses" to entity.fixedMonthlyExpenses,
            "targetProfitGoal" to entity.targetProfitGoal,
            "projectDurationDays" to entity.projectDurationDays,
            "capitalRemittanceFrequency" to entity.capitalRemittanceFrequency,
            "revenue" to entity.revenue,
            "netProfitTotal" to entity.netProfitTotal,
            "totalExpenses" to entity.totalExpenses,
            "dailyCashBurn" to entity.dailyCashBurn,
            "isLoss" to entity.isLoss
        )
    },
    restore = { map ->
        if (map.isEmpty() || !map.containsKey("name")) null
        else ProjectEntity(
            id = (map["id"] as? Number)?.toLong() ?: 0L,
            name = map["name"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            retailPrice = (map["retailPrice"] as? Number)?.toDouble() ?: 35000.0,
            sourcingCost = (map["sourcingCost"] as? Number)?.toDouble() ?: 10000.0,
            aovMultiplier = (map["aovMultiplier"] as? Number)?.toDouble() ?: 1.0,
            discounts = (map["discounts"] as? Number)?.toDouble() ?: 0.0,
            cac = (map["cac"] as? Number)?.toDouble() ?: 5000.0,
            adFee = (map["adFee"] as? Number)?.toDouble() ?: 3.0,
            ltvMultiplier = (map["ltvMultiplier"] as? Number)?.toDouble() ?: 1.1,
            shippingCost = (map["shippingCost"] as? Number)?.toDouble() ?: 4000.0,
            opsCost = (map["opsCost"] as? Number)?.toDouble() ?: 750.0,
            rejectionRate = (map["rejectionRate"] as? Number)?.toDouble() ?: 20.0,
            rtsShippingFee = (map["rtsShippingFee"] as? Number)?.toDouble() ?: 0.0,
            refundRate = (map["refundRate"] as? Number)?.toDouble() ?: 1.0,
            refundPenalty = (map["refundPenalty"] as? Number)?.toDouble() ?: 5000.0,
            damageRate = (map["damageRate"] as? Number)?.toDouble() ?: 5.0,
            deadstockRate = (map["deadstockRate"] as? Number)?.toDouble() ?: 2.0,
            fixedMonthlyExpenses = (map["fixedMonthlyExpenses"] as? Number)?.toDouble() ?: 450000.0,
            targetProfitGoal = (map["targetProfitGoal"] as? Number)?.toDouble() ?: 1000000.0,
            projectDurationDays = (map["projectDurationDays"] as? Number)?.toDouble() ?: 30.0,
            capitalRemittanceFrequency = (map["capitalRemittanceFrequency"] as? Number)?.toDouble() ?: 7.0,
            revenue = (map["revenue"] as? Number)?.toDouble() ?: 0.0,
            netProfitTotal = (map["netProfitTotal"] as? Number)?.toDouble() ?: 0.0,
            totalExpenses = (map["totalExpenses"] as? Number)?.toDouble() ?: 0.0,
            dailyCashBurn = (map["dailyCashBurn"] as? Number)?.toDouble() ?: 0.0,
            isLoss = map["isLoss"] as? Boolean ?: false
        )
    }
)

/**
 * Global helper function formatting numeric values with localized symbols.
 */
fun formatNumber(num: Double, isDecimal: Boolean = false, isKurdish: Boolean = true): String {
    if (!num.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
    val formatter = if (isDecimal) DECIMAL_FORMAT else INTEGER_FORMAT
    val formatted = synchronized(formatter) { formatter.format(kotlin.math.abs(num)) }
    return if (num < 0) "\u2066-$formatted\u2069" else formatted
}

/**
 * Main host composable managing multi-screen Jetpack Compose architecture,
 * native Android SQLite persistence via ProjectSQLiteDatabase, tab navigation (Dashboard, Analysis, Projects),
 * language state (Kurdish Sorani RTL vs English LTR), top app bar, floating bottom navbar,
 * SharedPreferences input caching, and save/dictionary modal dialogs.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs: SharedPreferences = remember {
        context.getSharedPreferences("unit_economics_calculator_prefs_v2", Context.MODE_PRIVATE)
    }

    // Native Android SQLite Database Setup
    val database = remember { ProjectSQLiteDatabase.getInstance(context) }
    val savedProjects by database.projectsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // Language State & Persistence
    var isKurdish by rememberSaveable {
        mutableStateOf(prefs.getBoolean("is_kurdish_language", true))
    }

    // Theme Mode State & Persistence (SYSTEM default, DARK, LIGHT manual override)
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    var themeModePref by rememberSaveable {
        mutableStateOf(prefs.getString("theme_mode_pref", "SYSTEM") ?: "SYSTEM")
    }

    val isDarkTheme = when (themeModePref) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    fun toggleTheme() {
        val nextMode = if (isDarkTheme) "LIGHT" else "DARK"
        themeModePref = nextMode
        prefs.edit().putString("theme_mode_pref", nextMode).apply()
    }

    // Tab Navigation State with Backstack
    val screenBackstack = rememberSaveable(
        saver = androidx.compose.runtime.saveable.listSaver<SnapshotStateList<NavScreen>, String>(
            save = { it.map { screen -> screen.name } },
            restore = { it.map { name -> NavScreen.valueOf(name) }.toMutableStateList() }
        )
    ) {
        mutableStateListOf(NavScreen.DASHBOARD)
    }
    val currentScreen = screenBackstack.lastOrNull() ?: NavScreen.DASHBOARD

    fun navigateTo(screen: NavScreen) {
        if (screenBackstack.lastOrNull() != screen) {
            screenBackstack.add(screen)
        }
    }

    // Dictionary Term Deep-Linking & Save Project Modal States
    var selectedTermForGlossary by rememberSaveable { mutableStateOf<String?>(null) }
    var isSaveProjectModalOpen by rememberSaveable { mutableStateOf(false) }

    // CalculatorInputs State loaded synchronously on cold start and stored directly in rememberSaveable
    var inputs by rememberSaveable(stateSaver = CalculatorInputsSaver) {
        val netTf = try { TimeframeOption.valueOf(prefs.getString("net-timeframe", "TOTAL") ?: "TOTAL") }
                    catch (e: Exception) { TimeframeOption.TOTAL }
        val revTf = try { TimeframeOption.valueOf(prefs.getString("rev-timeframe", "DAILY") ?: "DAILY") }
                    catch (e: Exception) { TimeframeOption.DAILY }

        fun getDoublePref(key: String, default: Double): Double {
            val str = prefs.getString(key, null)
            return str?.toDoubleOrNull() ?: default
        }

        mutableStateOf(
            CalculatorInputs(
                retailPrice = getDoublePref("inp-retail", 35000.0),
                sourcingCost = getDoublePref("inp-sourcing", 10000.0),
                aovMultiplier = getDoublePref("inp-aov", 1.0),
                discounts = getDoublePref("inp-discount", 0.0),
                cac = getDoublePref("inp-cac", 5000.0),
                adFee = getDoublePref("inp-ad-fee", 3.0),
                ltvMultiplier = getDoublePref("inp-ltv", 1.1),
                shippingCost = getDoublePref("inp-shipping", 4000.0),
                opsCost = getDoublePref("inp-ops", 750.0),
                rejectionRate = getDoublePref("inp-reject", 20.0),
                rtsShippingFee = getDoublePref("inp-return", 0.0),
                refundRate = getDoublePref("inp-refund", 1.0),
                refundPenalty = getDoublePref("inp-refund-penalty", 5000.0),
                damageRate = getDoublePref("inp-damage", 5.0),
                deadstockRate = getDoublePref("inp-deadstock", 2.0),
                fixedMonthlyExpenses = getDoublePref("inp-fixed", 450000.0),
                targetProfitGoal = getDoublePref("inp-goal", 1000000.0),
                projectDurationDays = getDoublePref("inp-days", 30.0),
                capitalRemittanceFrequency = getDoublePref("inp-remit", 7.0),
                netTimeframe = netTf,
                revTimeframe = revTf
            )
        )
    }

    // Active loaded project tracking for database updates — preserved across config changes
    var activeLoadedProject by rememberSaveable(stateSaver = ProjectEntitySaver) {
        mutableStateOf<ProjectEntity?>(null)
    }

    // Save state persistence helper with Mutex synchronization and 300ms trailing debounce
    val persistenceMutex = remember { kotlinx.coroutines.sync.Mutex() }
    val saveJobHolder = remember {
        object {
            var job: kotlinx.coroutines.Job? = null
        }
    }
    fun saveInputsToPrefs(immediate: Boolean = false) {
        saveJobHolder.job?.cancel()
        val currentInputs = inputs
        val currentKurdish = isKurdish
        val currentTheme = themeModePref
        val job = coroutineScope.launch {
            if (!immediate) {
                kotlinx.coroutines.delay(300)
            }
            persistenceMutex.withLock {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    prefs.edit().apply {
                        putBoolean("is_kurdish_language", currentKurdish)
                        putString("theme_mode_pref", currentTheme)
                        putString("inp-retail", currentInputs.retailPrice.toString())
                        putString("inp-sourcing", currentInputs.sourcingCost.toString())
                        putString("inp-aov", currentInputs.aovMultiplier.toString())
                        putString("inp-discount", currentInputs.discounts.toString())
                        putString("inp-cac", currentInputs.cac.toString())
                        putString("inp-ad-fee", currentInputs.adFee.toString())
                        putString("inp-ltv", currentInputs.ltvMultiplier.toString())
                        putString("inp-shipping", currentInputs.shippingCost.toString())
                        putString("inp-ops", currentInputs.opsCost.toString())
                        putString("inp-reject", currentInputs.rejectionRate.toString())
                        putString("inp-return", currentInputs.rtsShippingFee.toString())
                        putString("inp-refund", currentInputs.refundRate.toString())
                        putString("inp-refund-penalty", currentInputs.refundPenalty.toString())
                        putString("inp-damage", currentInputs.damageRate.toString())
                        putString("inp-deadstock", currentInputs.deadstockRate.toString())
                        putString("inp-fixed", currentInputs.fixedMonthlyExpenses.toString())
                        putString("inp-goal", currentInputs.targetProfitGoal.toString())
                        putString("inp-days", currentInputs.projectDurationDays.toString())
                        putString("inp-remit", currentInputs.capitalRemittanceFrequency.toString())
                        putString("net-timeframe", currentInputs.netTimeframe.name)
                        putString("rev-timeframe", currentInputs.revTimeframe.name)
                        apply()
                    }
                }
            }
        }
        saveJobHolder.job = job
    }

    // Flush pending changes immediately on lifecycle pause/stop
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, inputs, isKurdish, themeModePref) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                saveInputsToPrefs(immediate = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Recalculate CalculationResult on inputs change directly with zero string allocations
    val calculationResult = remember(inputs) {
        CalculatorLogic.runFinancialModel(inputs)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load Project Entity into Active Calculator Inputs
    fun loadProject(project: ProjectEntity) {
        activeLoadedProject = project
        inputs = project.toCalculatorInputs().copy(
            netTimeframe = inputs.netTimeframe,
            revTimeframe = inputs.revTimeframe
        )
        saveInputsToPrefs()
        navigateTo(NavScreen.DASHBOARD)
    }

    fun unloadProject() {
        val prevName = activeLoadedProject?.name ?: ""
        activeLoadedProject = null
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = if (isKurdish) "بەرهەمی «$prevName» لە بژمێر جیاکرایەوە" else "Product \"$prevName\" detached from calculator",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Save Current Project into SQLite Database
    fun saveProject(projectName: String, saveAsNew: Boolean = false) {
        coroutineScope.launch {
            val targetId = if (saveAsNew) 0L else (activeLoadedProject?.id ?: 0L)
            val entity = ProjectEntity.fromInputsAndResult(
                id = targetId,
                name = projectName,
                inputs = inputs,
                result = calculationResult
            )
            val resultId = database.insertOrUpdateProject(entity)
            if (resultId != -1L) {
                activeLoadedProject = entity.copy(id = resultId)
                isSaveProjectModalOpen = false
                snackbarHostState.showSnackbar(
                    message = if (targetId == 0L) {
                        if (isKurdish) "بەرهەمی نوێ بەسەرکەوتوویی پاشەکەوتکرا" else "New product saved successfully"
                    } else {
                        if (isKurdish) "پڕۆژەکە بەسەرکەوتوویی نوێکرایەوە" else "Product updated successfully"
                    },
                    duration = SnackbarDuration.Short
                )
                navigateTo(NavScreen.PROJECTS)
            } else {
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "هەڵەیەک ڕوویدا لە پاشەکەوتکردنی پڕۆژەکە" else "Failed to save project",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Delete Project from SQLite Database
    fun deleteProject(project: ProjectEntity) {
        coroutineScope.launch {
            val success = database.deleteProject(project)
            if (success) {
                val wasActive = activeLoadedProject?.id == project.id
                if (wasActive) {
                    activeLoadedProject = null
                }
                val result = snackbarHostState.showSnackbar(
                    message = if (isKurdish) "پڕۆژەکە سڕایەوە" else "Project deleted",
                    actionLabel = if (isKurdish) "گەڕاندنەوە" else "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    val restoredId = database.restoreProjectWithId(project)
                    if (restoredId > 0 && wasActive) {
                        activeLoadedProject = project
                    }
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "سڕینەوە سەرکەوتوو نەبوو" else "Failed to delete project",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Clone / Duplicate Project in SQLite Database
    fun duplicateProject(project: ProjectEntity) {
        coroutineScope.launch {
            val suffix = if (isKurdish) " (کۆپی)" else " (Copy)"
            val maxBaseLen = (40 - suffix.length).coerceAtLeast(1)
            val baseName = project.name.take(maxBaseLen).trim()
            val duplicateName = "$baseName$suffix"

            val duplicate = project.copy(
                id = 0,
                name = duplicateName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val resultId = database.insertOrUpdateProject(duplicate)
            if (resultId != -1L) {
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "کۆپیی پڕۆژەکە دروستکرا" else "Project duplicated successfully",
                    duration = SnackbarDuration.Short
                )
            } else {
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "هەڵەیەک ڕوویدا لە کۆپیکردنی پڕۆژە" else "Failed to duplicate project",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Rename Project in SQLite Database
    fun renameProject(project: ProjectEntity, newName: String) {
        coroutineScope.launch {
            val updated = project.copy(
                name = newName,
                updatedAt = System.currentTimeMillis()
            )
            val resultId = database.insertOrUpdateProject(updated)
            if (resultId != -1L) {
                if (activeLoadedProject?.id == project.id) {
                    activeLoadedProject = updated
                }
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "ناوی بەرهەمەکە بەسەرکەوتوویی گۆڕدرا" else "Product renamed successfully",
                    duration = SnackbarDuration.Short
                )
            } else {
                snackbarHostState.showSnackbar(
                    message = if (isKurdish) "هەڵەیەک ڕوویدا لە گۆڕینی ناوی بەرهەم" else "Failed to rename product",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Intelligent BackHandler hierarchy compliant with Android Predictive Back guidelines
    if (isSaveProjectModalOpen) {
        BackHandler { isSaveProjectModalOpen = false }
    } else if (screenBackstack.size > 1) {
        BackHandler {
            screenBackstack.removeAt(screenBackstack.lastIndex)
        }
    }

    // Dynamic Layout Direction: Kurdish Sorani RTL vs English LTR
    val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
    AppTheme(darkTheme = isDarkTheme) {
        val neuColors = LocalNeumorphicColors.current
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val widthSizeClass = WindowWidthSizeClass.fromWidth(maxWidth)
                val isExpandedTablet = widthSizeClass == WindowWidthSizeClass.Expanded

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isExpandedTablet) {
                        NeumorphicNavRail(
                            currentScreen = currentScreen,
                            onScreenSelected = { nav -> navigateTo(nav) },
                            onOpenSaveModal = { isSaveProjectModalOpen = true },
                            isKurdish = isKurdish
                        )
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            NeumorphicTopAppBar(
                                currentScreen = currentScreen,
                                isKurdish = isKurdish,
                                onLanguageToggle = { newLang ->
                                    isKurdish = newLang
                                    saveInputsToPrefs(immediate = true)
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { toggleTheme() }
                            )
                        },
                        bottomBar = {
                            if (!isExpandedTablet) {
                                FloatingBottomNavBar(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { nav -> navigateTo(nav) },
                                    onOpenSaveModal = { isSaveProjectModalOpen = true },
                                    isKurdish = isKurdish
                                )
                            }
                        },
                        containerColor = neuColors.background,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { targetScreen ->
                    when (targetScreen) {
                        NavScreen.DASHBOARD -> {
                            DashboardScreen(
                                result = calculationResult,
                                inputs = inputs,
                                onInputsChange = { newInp ->
                                    inputs = newInp
                                    saveInputsToPrefs()
                                },
                                netTimeframe = inputs.netTimeframe,
                                onNetTimeframeChange = { newTf ->
                                    inputs = inputs.copy(netTimeframe = newTf)
                                    saveInputsToPrefs()
                                },
                                isKurdish = isKurdish,
                                onOpenDictionaryForTerm = { term ->
                                    selectedTermForGlossary = term
                                    navigateTo(NavScreen.GLOSSARY)
                                }
                            )
                        }

                        NavScreen.ANALYSIS -> {
                            AnalysisScreen(
                                inputs = inputs,
                                result = calculationResult,
                                onInputsChange = { newInp ->
                                    inputs = newInp
                                    saveInputsToPrefs()
                                },
                                revTimeframe = inputs.revTimeframe,
                                onRevTimeframeChange = { newRev ->
                                    inputs = inputs.copy(revTimeframe = newRev)
                                    saveInputsToPrefs()
                                },
                                isKurdish = isKurdish,
                                onOpenDictionaryForTerm = { term ->
                                    selectedTermForGlossary = term
                                    navigateTo(NavScreen.GLOSSARY)
                                }
                            )
                        }

                        NavScreen.PROJECTS -> {
                            ProjectsScreen(
                                projects = savedProjects,
                                activeProjectId = activeLoadedProject?.id,
                                isKurdish = isKurdish,
                                onLoadProject = { proj -> loadProject(proj) },
                                onUnloadProject = { unloadProject() },
                                onDeleteProject = { proj -> deleteProject(proj) },
                                onCloneProject = { proj -> duplicateProject(proj) },
                                onRenameProject = { proj, newName -> renameProject(proj, newName) },
                                onSaveCurrentClick = { isSaveProjectModalOpen = true }
                            )
                        }

                        NavScreen.GLOSSARY -> {
                            GlossaryScreen(
                                isKurdish = isKurdish,
                                initialSelectedTerm = selectedTermForGlossary,
                                onClearSelectedTerm = { selectedTermForGlossary = null }
                            )
                        }
                    }
                }

                // Interactive Save Project Modal
                if (isSaveProjectModalOpen) {
                    SaveProjectModal(
                        inputs = inputs,
                        result = calculationResult,
                        activeLoadedProject = activeLoadedProject,
                        isKurdish = isKurdish,
                        onDismiss = { isSaveProjectModalOpen = false },
                        onSave = { name, saveAsNew -> saveProject(name, saveAsNew) }
                    )
                }
            }
        }
    }
}
}
}
}
