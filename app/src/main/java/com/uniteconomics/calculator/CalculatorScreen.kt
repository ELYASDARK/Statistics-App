package com.uniteconomics.calculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    // 1. Force RTL Layout Direction for Kurdish language support
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val extendedColors = LocalExtendedColors.current

        // 2. Calculator Input States (initialized with defaults)
        var retailText by remember { mutableStateOf("35000") }
        var sourcingText by remember { mutableStateOf("10000") }
        var shippingText by remember { mutableStateOf("4000") }
        var cacText by remember { mutableStateOf("5000") }
        var adFeeText by remember { mutableStateOf("3") }
        var packText by remember { mutableStateOf("500") }
        var telecomText by remember { mutableStateOf("250") }

        var rejectText by remember { mutableStateOf("20") }
        var returnFeeText by remember { mutableStateOf("5000") }
        var damageText by remember { mutableStateOf("5") }
        var damageEnabled by remember { mutableStateOf(true) }
        var deadstockText by remember { mutableStateOf("2") }
        var deadstockEnabled by remember { mutableStateOf(true) }

        var discountText by remember { mutableStateOf("0") }
        var discountEnabled by remember { mutableStateOf(true) }
        var ltvText by remember { mutableStateOf("1.1") }
        var ltvEnabled by remember { mutableStateOf(true) }
        var refundText by remember { mutableStateOf("1") }
        var refundEnabled by remember { mutableStateOf(true) }

        var aovText by remember { mutableStateOf("1.0") }
        var opexText by remember { mutableStateOf("150000") }
        var salaryText by remember { mutableStateOf("300000") }
        var remitText by remember { mutableStateOf("7") }
        var goalText by remember { mutableStateOf("1000000") }
        var daysText by remember { mutableStateOf("30") }

        // 3. Info Dialog State
        var infoDialogTitle by remember { mutableStateOf<String?>(null) }
        var infoDialogBody by remember { mutableStateOf<String?>(null) }

        // Helper to reset defaults
        fun resetToDefaults() {
            retailText = "35000"
            sourcingText = "10000"
            shippingText = "4000"
            cacText = "5000"
            adFeeText = "3"
            packText = "500"
            telecomText = "250"
            rejectText = "20"
            returnFeeText = "5000"
            damageText = "5"
            damageEnabled = true
            deadstockText = "2"
            deadstockEnabled = true
            discountText = "0"
            discountEnabled = true
            ltvText = "1.1"
            ltvEnabled = true
            refundText = "1"
            refundEnabled = true
            aovText = "1.0"
            opexText = "150000"
            salaryText = "300000"
            remitText = "7"
            goalText = "1000000"
            daysText = "30"
        }

        // 4. Parse inputs and run calculation model
        val parsedInputs = CalculatorInputs(
            retail = retailText.toDoubleOrNull() ?: 0.0,
            sourcing = sourcingText.toDoubleOrNull() ?: 0.0,
            shipping = shippingText.toDoubleOrNull() ?: 0.0,
            cac = cacText.toDoubleOrNull() ?: 0.0,
            adFee = adFeeText.toDoubleOrNull() ?: 0.0,
            pack = packText.toDoubleOrNull() ?: 0.0,
            telecom = telecomText.toDoubleOrNull() ?: 0.0,
            reject = rejectText.toDoubleOrNull() ?: 0.0,
            returnFee = returnFeeText.toDoubleOrNull() ?: 0.0,
            damage = damageText.toDoubleOrNull() ?: 0.0,
            damageEnabled = damageEnabled,
            deadstock = deadstockText.toDoubleOrNull() ?: 0.0,
            deadstockEnabled = deadstockEnabled,
            discount = discountText.toDoubleOrNull() ?: 0.0,
            discountEnabled = discountEnabled,
            ltv = ltvText.toDoubleOrNull() ?: 1.0,
            ltvEnabled = ltvEnabled,
            refund = refundText.toDoubleOrNull() ?: 0.0,
            refundEnabled = refundEnabled,
            aov = aovText.toDoubleOrNull() ?: 1.0,
            opex = opexText.toDoubleOrNull() ?: 0.0,
            salary = salaryText.toDoubleOrNull() ?: 0.0,
            remit = remitText.toDoubleOrNull() ?: 0.0,
            goal = goalText.toDoubleOrNull() ?: 0.0,
            days = daysText.toDoubleOrNull() ?: 30.0
        )

        val result = CalculatorLogic.runFinancialModel(parsedInputs)
        val isHealthy = result.netProfit > 0.0 && result.trueNetProfitPerOrder > 0.0

        // Helper formatting functions
        val moneyFormat = remember {
            DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        }
        fun formatMoney(value: Double): String {
            return if (value.isNaN() || value.isInfinite()) "0" else moneyFormat.format(value.toInt())
        }
        fun formatK(value: Float): String {
            return "${(value / 1000).toInt()}k"
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "داشبۆردی شیکردنەوەی قازانج و تێچوو",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )
                            )
                            Text(
                                text = "Unit Economics Calculator",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = extendedColors.textMuted,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onThemeToggle) {
                            Text(
                                text = if (darkTheme) "🌙" else "☀️",
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        Text(
                            text = "📊",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // === TOP DASHBOARD CARDS (SaaS grid look from new 8. k.html) ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Net Profit Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, extendedColors.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "پوختەی قازانجی سافی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHealthy) "${formatMoney(result.trueNetProfitPerOrder)} IQD" else "زیان",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isHealthy) extendedColors.success else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Total Shipped Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, extendedColors.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "کۆی داواکارییە نێردراوەکان",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHealthy) formatMoney(result.totalUnits.toDouble()) else "زیان",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Sales Target Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, extendedColors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ئامانجی فرۆشتنی ڕۆژانە",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHealthy) DecimalFormat("0.0").format(result.dailySales) else "زیان",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isHealthy) extendedColors.success else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isHealthy) {
                                        if (darkTheme) Color(0x264ADE80) else Color(0xFFD1FAE5)
                                    } else {
                                        if (darkTheme) Color(0x26F87171) else Color(0xFFFEE2E2)
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isHealthy) Color(0xFF34D399) else Color(0xFFF87171),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isHealthy) "✅ ئامانجێکی باشە" else "❌ یەکەیەکی زیانبەخشە",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHealthy) {
                                    if (darkTheme) Color(0xFF4ADE80) else Color(0xFF065F46)
                                } else {
                                    if (darkTheme) Color(0xFFF87171) else Color(0xFF991B1B)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === CARD 1: Revenue and COGS ===
                SectionCard(
                    title = "داهات و تێچووی سەرەکی کاڵا",
                    icon = "💰",
                    titleColor = extendedColors.success
                ) {
                    CalculatorRow(
                        title = "نرخی فرۆشتنی کاڵا",
                        value = retailText,
                        onValueChange = { retailText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "retail"),
                        onInfoClick = {
                            infoDialogTitle = "Unit Retail Price (Gross)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "تێچووی کڕینی کاڵا (مفرد)",
                        subtitle = "نرخی کڕینی یەک کاڵا بە جوملە",
                        value = sourcingText,
                        onValueChange = { sourcingText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "sourcing"),
                        onInfoClick = {
                            infoDialogTitle = "Cost of Goods Sold (COGS)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "ژمارەی کاڵا لە یەک داواکارییدا",
                        subtitle = "تێکڕای فرۆشتنی چەند دانەیەک پێکەوە",
                        value = aovText,
                        onValueChange = { aovText = it },
                        suffix = "دانە",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "aov"),
                        onInfoClick = {
                            infoDialogTitle = "Units Per Transaction (UPT)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "ڕێژەی داشکاندن",
                        subtitle = "ئەگەر ئۆفەر یان داشکاندنت نییە، ئەمە بکوژێنەوە",
                        value = discountText,
                        onValueChange = { discountText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "discount"),
                        enabled = discountEnabled,
                        hasToggle = true,
                        toggleChecked = discountEnabled,
                        onToggleChange = { discountEnabled = it },
                        onInfoClick = {
                            infoDialogTitle = "Trade Discount Rate"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )
                }

                // === CARD 2: Customer Acquisition Cost ===
                SectionCard(
                    title = "تێچووی پەیداکردنی کڕیار (ڕیکلام)",
                    icon = "📢",
                    titleColor = extendedColors.info
                ) {
                    CalculatorRow(
                        title = "تێچووی پەیداکردنی کڕیارێک",
                        subtitle = "تێچووی سپۆنسەر بۆ هەر داواکارییەکی پەسەندکراو",
                        value = cacText,
                        onValueChange = { cacText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "cac"),
                        onInfoClick = {
                            infoDialogTitle = "Customer Acquisition Cost (CAC)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "عمولەی کارتی بانکی / ڕیکلام",
                        subtitle = "ئەو عمولەیەی بانک لێتی دەبڕێت بۆ ڕیکلامەکان",
                        value = adFeeText,
                        onValueChange = { adFeeText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "ad-fee"),
                        onInfoClick = {
                            infoDialogTitle = "Payment Gateway / FX Fee"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "کڕینەوەی کاڵا لەلایەن هەمان کڕیار",
                        subtitle = "ئەگەر کڕیارەکانت تەنها یەکجار شت دەکڕن، بکوژێنەوە",
                        value = ltvText,
                        onValueChange = { ltvText = it },
                        suffix = "جار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "ltv"),
                        enabled = ltvEnabled,
                        hasToggle = true,
                        toggleChecked = ltvEnabled,
                        onToggleChange = { ltvEnabled = it },
                        onInfoClick = {
                            infoDialogTitle = "Customer Lifetime Value (LTV)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )
                }

                // === CARD 3: Delivery and Fulfillment ===
                SectionCard(
                    title = "گەیاندن و ئامادەکردن",
                    icon = "📦",
                    titleColor = extendedColors.purple
                ) {
                    CalculatorRow(
                        title = "تێچووی گەیاندن بۆ لای کڕیار",
                        value = shippingText,
                        onValueChange = { shippingText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "shipping"),
                        onInfoClick = {
                            infoDialogTitle = "Outbound Freight / Shipping Cost"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "تێچووی پاکەتکردن و ئامادەکردن",
                        subtitle = "کارتۆن، کیسە، و ماندووبوونی کارمەند",
                        value = packText,
                        onValueChange = { packText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "pack"),
                        onInfoClick = {
                            infoDialogTitle = "Fulfillment (Pick & Pack) Cost"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "تێچووی پشتڕاستکردنەوە (تەلەفۆن)",
                        subtitle = "تێچووی باڵانسی پەیوەندی یان نامە بە کڕیار",
                        value = telecomText,
                        onValueChange = { telecomText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "telecom"),
                        onInfoClick = {
                            infoDialogTitle = "Order Verification Cost"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )
                }

                // === CARD 4: Risks and Margin ===
                SectionCard(
                    title = "مەترسییەکان و زیانی کاڵا",
                    icon = "⚠️",
                    titleColor = MaterialTheme.colorScheme.error
                ) {
                    CalculatorRow(
                        title = "ڕێژەی ڕەتکردنەوەی کاڵا لە کاتی گەیاندن",
                        subtitle = "ئەو کڕیارانەی لە دەرگای ماڵەوە کاڵاکە وەرناگرن",
                        value = rejectText,
                        onValueChange = { rejectText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "reject"),
                        onInfoClick = {
                            infoDialogTitle = "Order Refusal (RTS) Rate"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "تێچووی گەڕانەوەی کاڵای ڕەتکراوە",
                        subtitle = "ئەو پارەیەی کۆمپانیای گەیاندن لێتی دەبڕێت بۆ هێنانەوەی کاڵاکە",
                        value = returnFeeText,
                        onValueChange = { returnFeeText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "return"),
                        onInfoClick = {
                            infoDialogTitle = "Reverse Logistics (RTS) Fee"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "گەڕاندنەوە دوای وەرگرتن",
                        subtitle = "کڕیار کاڵاکەی پێ ناخۆشە و دەیگەڕێنێتەوە",
                        value = refundText,
                        onValueChange = { refundText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "refund"),
                        enabled = refundEnabled,
                        hasToggle = true,
                        toggleChecked = refundEnabled,
                        onToggleChange = { refundEnabled = it },
                        onInfoClick = {
                            infoDialogTitle = "Post-Delivery Return Rate"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "تێکچوون یان ونبوونی کاڵا",
                        subtitle = "ئەو کاڵایانەی لە کۆگا یان لە ڕێگا تێکدەچن",
                        value = damageText,
                        onValueChange = { damageText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "damage"),
                        enabled = damageEnabled,
                        hasToggle = true,
                        toggleChecked = damageEnabled,
                        onToggleChange = { damageEnabled = it },
                        onInfoClick = {
                            infoDialogTitle = "Inventory Shrinkage Rate"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "بەسەرچوون یان نافرۆشرێن",
                        subtitle = "ئەو کاڵایانەی دەمێننەوە و مۆدێلیان نامێنێت",
                        value = deadstockText,
                        onValueChange = { deadstockText = it },
                        suffix = "٪",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "deadstock"),
                        enabled = deadstockEnabled,
                        hasToggle = true,
                        toggleChecked = deadstockEnabled,
                        onToggleChange = { deadstockEnabled = it },
                        onInfoClick = {
                            infoDialogTitle = "Inventory Obsolescence Rate"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contribution Margin Row Highlight
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = extendedColors.border,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "قازانجی سەرەتایی هەر کاڵایەک",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            infoDialogTitle = "Contribution Margin per Unit"
                                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "info",
                                            tint = extendedColors.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "قازانج پێش دەرکردنی خەرجییە جێگیرەکانی مانگانە",
                                    fontSize = 11.sp,
                                    color = extendedColors.textMuted
                                )
                            }
                            Text(
                                text = if (isHealthy) "${formatMoney(result.netProfit)} IQD" else "زیان",
                                fontWeight = FontWeight.Bold,
                                color = if (isHealthy) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // === CARD 5: Fixed Monthly Expenses ===
                SectionCard(
                    title = "خەرجییە جێگیرەکانی مانگانە",
                    icon = "🏢",
                    titleColor = extendedColors.textMuted
                ) {
                    CalculatorRow(
                        title = "خەرجی کارپێکردن (کرێ، غاز...)",
                        subtitle = "کرێی دوکان، ئینتەرنێت، بەنزین و خەرجی ڕۆژانە",
                        value = opexText,
                        onValueChange = { opexText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "opex"),
                        onInfoClick = {
                            infoDialogTitle = "Fixed Operating Expenses (OPEX)"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "کۆی مووچەی کارمەندان",
                        subtitle = "مووچەی مانگانەی کارمەندانی جێگیر",
                        value = salaryText,
                        onValueChange = { salaryText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "salary"),
                        onInfoClick = {
                            infoDialogTitle = "Fixed Payroll & Labor Costs"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fully Allocated Net Profit Row Highlight
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = extendedColors.success.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (darkTheme) Color(0x144ADE80) else Color(0x0A16A34A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "پوختەی قازانجی ڕاستەقینە لە هەر داواکارییەک",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = extendedColors.success
                                    )
                                    IconButton(
                                        onClick = {
                                            infoDialogTitle = "Fully Allocated Net Profit per Unit"
                                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "info",
                                            tint = extendedColors.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "ئەو قازانجەی دەچێتە گیرفانتەوە دوای هەموو خەرجییەکان",
                                    fontSize = 11.sp,
                                    color = extendedColors.success.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = if (isHealthy) "${formatMoney(result.trueNetProfitPerOrder)} IQD" else "زیان",
                                fontWeight = FontWeight.Bold,
                                color = if (isHealthy) extendedColors.success else MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // === CARD 6: Targets and Remit ===
                SectionCard(
                    title = "ئامانجەکان و جووڵەی پارە",
                    icon = "🔄",
                    titleColor = extendedColors.info
                ) {
                    CalculatorRow(
                        title = "ئامانجی قازانجی سافی",
                        subtitle = "دەتەوێت مانگانە چەند قازانجی سافیت بۆ بمێنێتەوە؟",
                        value = goalText,
                        onValueChange = { goalText = it },
                        suffix = "دینار",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "goal"),
                        onInfoClick = {
                            infoDialogTitle = "Target Net Income"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "ماوەی کارکردن",
                        subtitle = "ئەو ماوەیەی کە دەتەوێت بگەیت بە ئامانجەکەت",
                        value = daysText,
                        onValueChange = { daysText = it },
                        suffix = "ڕۆژ",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "days"),
                        onInfoClick = {
                            infoDialogTitle = "Operational Period"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    CalculatorRow(
                        title = "دواکەوتنی پارەی گەیاندن",
                        subtitle = "کۆمپانیای گەیاندن دوای چەند ڕۆژ پارەکەت پێدەدات؟",
                        value = remitText,
                        onValueChange = { remitText = it },
                        suffix = "ڕۆژ",
                        status = CalculatorLogic.evaluateMetric(parsedInputs, "remit"),
                        onInfoClick = {
                            infoDialogTitle = "Cash Conversion Cycle"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )
                }

                // === CARD 7: Projections and Capital ===
                SectionCard(
                    title = "پێشبینییەکان و سەرمایەی پێویست",
                    icon = "📈",
                    titleColor = extendedColors.purple
                ) {
                    ResultRow(
                        title = "خاڵی یەکسانبوون (تەنها دەرکردنی تێچووەکان)",
                        value = if (isHealthy) formatMoney(result.breakEven.toDouble()) else "زیان",
                        onInfoClick = {
                            infoDialogTitle = "Unit Sales Break-Even Point"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    ResultRow(
                        title = "ژمارەی داواکارییە سەرکەوتووەکانی پێویست",
                        value = if (isHealthy) formatMoney(result.totalUnits.toDouble()) else "زیان",
                        onInfoClick = {
                            infoDialogTitle = "Required Successful Conversions"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    ResultRow(
                        title = "کۆی ئەو داواکارییانەی دەبێت بیاننێریت",
                        subtitle = "ئەوانەش دەگرێتەوە کە لە دەرگای ماڵەوە ڕەتدەکرێنەوە",
                        value = if (isHealthy) formatMoney(result.totalSent.toDouble()) else "زیان",
                        valueColor = extendedColors.warning,
                        onInfoClick = {
                            infoDialogTitle = "Gross Shipped Volume"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    ResultRow(
                        title = "کۆی سەرمایەی پێویست بەکارهاتوو",
                        subtitle = "تەواوی ئەو پارەیەی دەبێت خەرجی بکەیت بۆ کاڵا و ڕیکلام",
                        value = if (isHealthy) "${formatMoney(result.totalCapital)} IQD" else "زیان",
                        valueColor = extendedColors.purple,
                        onInfoClick = {
                            infoDialogTitle = "Total Capital Deployment"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )

                    ResultRow(
                        title = "سەرمایەی ئامادەکراو بۆ سووڕانەوەی کار",
                        subtitle = "ئەو پارەیەی پێویستە لە گیرفانتدا بێت کاتێک پارەکەت لای گەیاندنە",
                        value = if (isHealthy) "${formatMoney(result.floatCapital)} IQD" else "زیان",
                        valueColor = MaterialTheme.colorScheme.error,
                        onInfoClick = {
                            infoDialogTitle = "Required Working Capital Float"
                            infoDialogBody = CalculatorLogic.businessTerms[infoDialogTitle]
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === CARD 8: Sliders Section ===
                SectionCard(
                    title = "تاقیکردنەوەی خێرا (گۆڕانکاری)",
                    icon = "🎚️",
                    titleColor = extendedColors.info
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // COGS Slider
                        val cogsVal = parsedInputs.sourcing.toFloat()
                        SliderRow(
                            label = "تێچووی کڕین",
                            value = cogsVal,
                            onValueChange = { sourcingText = it.toInt().toString() },
                            valueDisplay = formatK(cogsVal),
                            range = 0f..50000f
                        )

                        // Retail Slider
                        val retailVal = parsedInputs.retail.toFloat()
                        SliderRow(
                            label = "نرخی فرۆشتن",
                            value = retailVal,
                            onValueChange = { retailText = it.toInt().toString() },
                            valueDisplay = formatK(retailVal),
                            range = 0f..150000f
                        )

                        // Shipping Slider
                        val shippingVal = parsedInputs.shipping.toFloat()
                        SliderRow(
                            label = "گەیاندن",
                            value = shippingVal,
                            onValueChange = { shippingText = it.toInt().toString() },
                            valueDisplay = formatK(shippingVal),
                            range = 0f..15000f
                        )

                        // CAC Slider
                        val cacVal = parsedInputs.cac.toFloat()
                        SliderRow(
                            label = "تێچووی ڕیکلام",
                            value = cacVal,
                            onValueChange = { cacText = it.toInt().toString() },
                            valueDisplay = formatK(cacVal),
                            range = 0f..25000f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reset Button
                Button(
                    onClick = { resetToDefaults() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = extendedColors.textMuted
                    ),
                    border = BorderStroke(1.dp, extendedColors.border),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "🔄 گەڕانەوە بۆ بنەڕەت",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 5. Native M3 Info Dialog
        if (infoDialogTitle != null && infoDialogBody != null) {
            Dialog(onDismissRequest = {
                infoDialogTitle = null
                infoDialogBody = null
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = infoDialogTitle ?: "",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = infoDialogBody ?: "",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = extendedColors.textMuted,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Start
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                infoDialogTitle = null
                                infoDialogBody = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extendedColors.success,
                                contentColor = extendedColors.onSuccess
                            )
                        ) {
                            Text(
                                text = "تێگەیشتم",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: String,
    titleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, LocalExtendedColors.current.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LocalExtendedColors.current.border)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun CalculatorRow(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    status: MetricStatus,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    hasToggle: Boolean = false,
    toggleChecked: Boolean = true,
    onToggleChange: (Boolean) -> Unit = {}
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (enabled) extendedColors.border else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .drawBehind {
                val barColor = when (status) {
                    MetricStatus.GOOD -> extendedColors.success
                    MetricStatus.AVG -> extendedColors.warning
                    MetricStatus.BAD -> Color(0xFFF87171) // Red indicator
                }
                if (enabled) {
                    // Draw status indicator on the rightmost edge (canvas coordinate starts at size.width)
                    drawLine(
                        color = barColor,
                        start = Offset(size.width - 2f, 8f),
                        end = Offset(size.width - 2f, size.height - 8f),
                        strokeWidth = 6f
                    )
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = extendedColors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (hasToggle) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = toggleChecked,
                        onCheckedChange = onToggleChange,
                        modifier = Modifier.scale(0.7f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = extendedColors.info,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = extendedColors.border
                        )
                    )
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = extendedColors.textMuted,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(130.dp),
            horizontalArrangement = Arrangement.End
        ) {
            CompactTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = suffix,
                fontSize = 11.sp,
                color = extendedColors.textMuted
            )
        }
    }
}

@Composable
fun ResultRow(
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onInfoClick: (() -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 13.sp
                )
                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            tint = extendedColors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = extendedColors.textMuted,
                    lineHeight = 14.sp
                )
            }
        }
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueDisplay: String,
    range: ClosedFloatingPointRange<Float>
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(90.dp)
        )
        
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = extendedColors.info,
                activeTrackColor = extendedColors.info,
                inactiveTrackColor = extendedColors.border
            )
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(6.dp))
                .border(1.dp, extendedColors.border, shape = RoundedCornerShape(6.dp))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.info
            )
        }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val extendedColors = LocalExtendedColors.current
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Left
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(extendedColors.info),
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .drawBehind {
                val lineColor = if (isFocused) extendedColors.info else Color.Transparent
                // Draw a simple bottom line on focus
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 3f
                )
            }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    )
}
