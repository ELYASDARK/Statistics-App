package com.uniteconomics.calculator

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.calculator.app.data.CalculatorInputs
import com.calculator.app.data.CalculatorLogic
import com.calculator.app.data.TimeframeOption
import com.uniteconomics.calculator.db.ProjectEntity
import com.uniteconomics.calculator.db.ProjectSQLiteDatabase
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Global helper function formatting numeric values with localized symbols.
 */
fun formatNumber(num: Double, isDecimal: Boolean = false): String {
    if (!num.isFinite()) return "نەزانراو"
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = if (isDecimal) {
        DecimalFormat("#,##0.0", symbols)
    } else {
        DecimalFormat("#,##0", symbols)
    }
    return formatter.format(num)
}

/**
 * Main host composable managing multi-screen Jetpack Compose architecture,
 * native Android SQLite persistence via ProjectSQLiteDatabase, tab navigation (Dashboard, Analysis, Projects),
 * language state (Kurdish Sorani RTL vs English LTR), top app bar, floating bottom navbar,
 * SharedPreferences input caching, and save/dictionary modal dialogs.
 */
@Composable
fun CalculatorApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs: SharedPreferences = remember {
        context.getSharedPreferences("unit_economics_calculator_prefs_v2", Context.MODE_PRIVATE)
    }

    // Native Android SQLite Database Setup
    val database = remember { ProjectSQLiteDatabase.getInstance(context) }
    val savedProjects by database.projectsFlow.collectAsState(initial = emptyList())

    // Language State & Persistence
    var isKurdish by remember {
        mutableStateOf(prefs.getBoolean("is_kurdish_language", true))
    }

    // Theme Mode State & Persistence (SYSTEM default, DARK, LIGHT manual override)
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    var themeModePref by remember {
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

    // Tab Navigation State
    var currentScreen by remember { mutableStateOf(NavScreen.DASHBOARD) }

    // Dictionary & Save Project Modal States
    var selectedTermForModal by remember { mutableStateOf<String?>(null) }
    var isDictionaryModalOpen by remember { mutableStateOf(false) }
    var isSaveProjectModalOpen by remember { mutableStateOf(false) }

    // 19 Input State initializers loaded directly from SharedPreferences or defaults
    var retailText by remember { mutableStateOf(prefs.getString("inp-retail", "35000") ?: "35000") }
    var sourcingText by remember { mutableStateOf(prefs.getString("inp-sourcing", "10000") ?: "10000") }
    var aovText by remember { mutableStateOf(prefs.getString("inp-aov", "1.0") ?: "1.0") }
    var discountText by remember { mutableStateOf(prefs.getString("inp-discount", "0") ?: "0") }

    var cacText by remember { mutableStateOf(prefs.getString("inp-cac", "5000") ?: "5000") }
    var adFeeText by remember { mutableStateOf(prefs.getString("inp-ad-fee", "3") ?: "3") }
    var ltvText by remember { mutableStateOf(prefs.getString("inp-ltv", "1.1") ?: "1.1") }

    var shippingText by remember { mutableStateOf(prefs.getString("inp-shipping", "4000") ?: "4000") }
    var opsText by remember { mutableStateOf(prefs.getString("inp-ops", "750") ?: "750") }
    var rejectText by remember { mutableStateOf(prefs.getString("inp-reject", "20") ?: "20") }
    var returnFeeRTSText by remember { mutableStateOf(prefs.getString("inp-return", "0") ?: "0") }
    var refundText by remember { mutableStateOf(prefs.getString("inp-refund", "1") ?: "1") }
    var refundPenaltyText by remember { mutableStateOf(prefs.getString("inp-refund-penalty", "5000") ?: "5000") }
    var damageText by remember { mutableStateOf(prefs.getString("inp-damage", "5") ?: "5") }
    var deadstockText by remember { mutableStateOf(prefs.getString("inp-deadstock", "2") ?: "2") }

    var fixedMonthlyText by remember { mutableStateOf(prefs.getString("inp-fixed", "450000") ?: "450000") }
    var goalText by remember { mutableStateOf(prefs.getString("inp-goal", "1000000") ?: "1000000") }
    var daysText by remember { mutableStateOf(prefs.getString("inp-days", "30") ?: "30") }
    var remitText by remember { mutableStateOf(prefs.getString("inp-remit", "7") ?: "7") }

    var netTimeframe by remember {
        mutableStateOf(
            try { TimeframeOption.valueOf(prefs.getString("net-timeframe", "TOTAL") ?: "TOTAL") }
            catch (e: Exception) { TimeframeOption.TOTAL }
        )
    }

    var revTimeframe by remember {
        mutableStateOf(
            try { TimeframeOption.valueOf(prefs.getString("rev-timeframe", "DAILY") ?: "DAILY") }
            catch (e: Exception) { TimeframeOption.DAILY }
        )
    }

    // Save state persistence helper (offloaded to Dispatchers.IO for 120fps fluid slider dragging)
    fun saveInputsToPrefs() {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            prefs.edit().apply {
                putBoolean("is_kurdish_language", isKurdish)
                putString("inp-retail", retailText)
                putString("inp-sourcing", sourcingText)
                putString("inp-aov", aovText)
                putString("inp-discount", discountText)
                putString("inp-cac", cacText)
                putString("inp-ad-fee", adFeeText)
                putString("inp-ltv", ltvText)
                putString("inp-shipping", shippingText)
                putString("inp-ops", opsText)
                putString("inp-reject", rejectText)
                putString("inp-return", returnFeeRTSText)
                putString("inp-refund", refundText)
                putString("inp-refund-penalty", refundPenaltyText)
                putString("inp-damage", damageText)
                putString("inp-deadstock", deadstockText)
                putString("inp-fixed", fixedMonthlyText)
                putString("inp-goal", goalText)
                putString("inp-days", daysText)
                putString("inp-remit", remitText)
                putString("net-timeframe", netTimeframe.name)
                putString("rev-timeframe", revTimeframe.name)
                apply()
            }
        }
    }

    fun parseVal(text: String, fallback: Double = 0.0): Double {
        return text.toDoubleOrNull() ?: fallback
    }

    // Construct CalculatorInputs data model
    val inputs = remember(
        retailText, sourcingText, aovText, discountText,
        cacText, adFeeText, ltvText,
        shippingText, opsText, rejectText, returnFeeRTSText, refundText, refundPenaltyText, damageText, deadstockText,
        fixedMonthlyText, goalText, daysText, remitText,
        netTimeframe, revTimeframe
    ) {
        CalculatorInputs(
            retailPrice = parseVal(retailText, 35000.0),
            sourcingCost = parseVal(sourcingText, 10000.0),
            aovMultiplier = parseVal(aovText, 1.0),
            discounts = parseVal(discountText, 0.0),
            cac = parseVal(cacText, 5000.0),
            adFee = parseVal(adFeeText, 3.0),
            ltvMultiplier = parseVal(ltvText, 1.1),
            shippingCost = parseVal(shippingText, 4000.0),
            opsCost = parseVal(opsText, 750.0),
            rejectionRate = parseVal(rejectText, 20.0),
            rtsShippingFee = parseVal(returnFeeRTSText, 0.0),
            refundRate = parseVal(refundText, 1.0),
            refundPenalty = parseVal(refundPenaltyText, 5000.0),
            damageRate = parseVal(damageText, 5.0),
            deadstockRate = parseVal(deadstockText, 2.0),
            fixedMonthlyExpenses = parseVal(fixedMonthlyText, 450000.0),
            targetProfitGoal = parseVal(goalText, 1000000.0),
            projectDurationDays = parseVal(daysText, 30.0),
            capitalRemittanceFrequency = parseVal(remitText, 7.0),
            netTimeframe = netTimeframe,
            revTimeframe = revTimeframe
        )
    }

    // Recalculate CalculationResult on inputs change
    val calculationResult = remember(inputs) {
        CalculatorLogic.runFinancialModel(inputs)
    }

    // Load Project Entity into Active Calculator Inputs
    fun loadProject(project: ProjectEntity) {
        val projInputs = project.toCalculatorInputs()
        retailText = projInputs.effRetailPrice.toString()
        sourcingText = projInputs.effSourcingCost.toString()
        aovText = projInputs.effAovMultiplier.toString()
        discountText = projInputs.effDiscounts.toString()
        cacText = projInputs.cac.toString()
        adFeeText = projInputs.adFee.toString()
        ltvText = projInputs.effLtvMultiplier.toString()
        shippingText = projInputs.effShippingCost.toString()
        opsText = projInputs.effOpsCost.toString()
        rejectText = projInputs.effRejectionRate.toString()
        returnFeeRTSText = projInputs.effRtsShippingFee.toString()
        refundText = projInputs.effRefundRate.toString()
        refundPenaltyText = projInputs.refundPenalty.toString()
        damageText = projInputs.effDamageRate.toString()
        deadstockText = projInputs.effDeadstockRate.toString()
        fixedMonthlyText = projInputs.effFixedMonthlyExpenses.toString()
        goalText = projInputs.effTargetProfitGoal.toString()
        daysText = projInputs.effProjectDurationDays.toString()
        remitText = projInputs.effCapitalRemittanceFrequency.toString()
        saveInputsToPrefs()
        currentScreen = NavScreen.DASHBOARD
    }

    // Save Current Project into SQLite Database
    fun saveProject(projectName: String) {
        coroutineScope.launch {
            val entity = ProjectEntity.fromInputsAndResult(
                name = projectName,
                inputs = inputs,
                result = calculationResult
            )
            database.insertOrUpdateProject(entity)
            isSaveProjectModalOpen = false
            currentScreen = NavScreen.PROJECTS
        }
    }

    // Delete Project from SQLite Database
    fun deleteProject(project: ProjectEntity) {
        coroutineScope.launch {
            database.deleteProject(project)
        }
    }

    // Dynamic Layout Direction: Kurdish Sorani RTL vs English LTR
    val layoutDirection = if (isKurdish) LayoutDirection.Rtl else LayoutDirection.Ltr
    AppTheme(darkTheme = isDarkTheme) {
        val neuColors = LocalNeumorphicColors.current
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Scaffold(
                topBar = {
                    NeumorphicTopAppBar(
                        currentScreen = currentScreen,
                        isKurdish = isKurdish,
                        onLanguageToggle = { newLang ->
                            isKurdish = newLang
                            saveInputsToPrefs()
                        },
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { toggleTheme() },
                        onOpenDictionary = {
                            isDictionaryModalOpen = true
                        },
                        onOpenProjects = {
                            currentScreen = NavScreen.PROJECTS
                        }
                    )
                },
            bottomBar = {
                FloatingBottomNavBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { nav -> currentScreen = nav },
                    onOpenSaveModal = { currentScreen = NavScreen.PROJECTS },
                    isKurdish = isKurdish
                )
            },
            containerColor = neuColors.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { targetScreen ->
                    when (targetScreen) {
                        NavScreen.DASHBOARD -> {
                            DashboardScreen(
                                result = calculationResult,
                                inputs = inputs,
                                netTimeframe = netTimeframe,
                                onNetTimeframeChange = { newTf ->
                                    netTimeframe = newTf
                                    saveInputsToPrefs()
                                },
                                isKurdish = isKurdish,
                                onOpenDictionaryForTerm = { term ->
                                    selectedTermForModal = term
                                }
                            )
                        }

                        NavScreen.ANALYSIS -> {
                            AnalysisScreen(
                                inputs = inputs,
                                result = calculationResult,
                                onInputsChange = { newInp ->
                                    retailText = newInp.retailPrice.toString()
                                    sourcingText = newInp.sourcingCost.toString()
                                    aovText = newInp.aovMultiplier.toString()
                                    discountText = newInp.discounts.toString()
                                    cacText = newInp.cac.toString()
                                    adFeeText = newInp.adFee.toString()
                                    ltvText = newInp.ltvMultiplier.toString()
                                    shippingText = newInp.shippingCost.toString()
                                    opsText = newInp.opsCost.toString()
                                    rejectText = newInp.rejectionRate.toString()
                                    returnFeeRTSText = newInp.rtsShippingFee.toString()
                                    refundText = newInp.refundRate.toString()
                                    refundPenaltyText = newInp.refundPenalty.toString()
                                    damageText = newInp.damageRate.toString()
                                    deadstockText = newInp.deadstockRate.toString()
                                    fixedMonthlyText = newInp.fixedMonthlyExpenses.toString()
                                    goalText = newInp.targetProfitGoal.toString()
                                    daysText = newInp.projectDurationDays.toString()
                                    remitText = newInp.capitalRemittanceFrequency.toString()
                                    saveInputsToPrefs()
                                },
                                revTimeframe = revTimeframe,
                                onRevTimeframeChange = { newRev ->
                                    revTimeframe = newRev
                                    saveInputsToPrefs()
                                },
                                isKurdish = isKurdish,
                                onOpenDictionaryForTerm = { term ->
                                    selectedTermForModal = term
                                }
                            )
                        }

                        NavScreen.PROJECTS -> {
                            ProjectsScreen(
                                projects = savedProjects,
                                isKurdish = isKurdish,
                                onLoadProject = { proj -> loadProject(proj) },
                                onDeleteProject = { proj -> deleteProject(proj) },
                                onSaveCurrentClick = { isSaveProjectModalOpen = true }
                            )
                        }
                    }
                }

                // Interactive Save Project Modal
                if (isSaveProjectModalOpen) {
                    SaveProjectModal(
                        inputs = inputs,
                        result = calculationResult,
                        isKurdish = isKurdish,
                        onDismiss = { isSaveProjectModalOpen = false },
                        onSave = { name -> saveProject(name) }
                    )
                }

                // Interactive Dictionary Dialog Modal
                if (isDictionaryModalOpen || selectedTermForModal != null) {
                    DictionaryDialog(
                        initialTerm = selectedTermForModal,
                        isKurdish = isKurdish,
                        onDismiss = {
                            isDictionaryModalOpen = false
                            selectedTermForModal = null
                        }
                    )
                }
            }
        }
    }
}
}
