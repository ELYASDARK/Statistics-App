package com.uniteconomics.calculator

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.util.Locale

/**
 * Number of fixed items preceding the glossary term cards inside LazyColumn.
 * Search & Filter section (0), Results counter (1).
 */
private const val HEADER_ITEMS_COUNT = 2

/**
 * High contrast accessible primary color token for text on Neumorphic light surfaces
 * satisfying WCAG AA contrast ratio >= 4.5:1.
 */
private val NeuLightAccessiblePrimary = Color(0xFF1D4ED8)

/**
 * Reusable Shape Singletons for zero heap allocation during composition.
 */
private val ShapeRadius8 = RoundedCornerShape(8.dp)
private val ShapeRadius12 = RoundedCornerShape(12.dp)
private val ShapeRadius14 = RoundedCornerShape(14.dp)
private val ShapeRadius16 = RoundedCornerShape(16.dp)
private val ShapeRadius20 = RoundedCornerShape(20.dp)

/**
 * Reusable Kurdish Eastern Arabic-Indic Digit Array.
 */
private val KURDISH_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/**
 * Type-safe Saver for expanded terms set across configuration changes and process death.
 */
private val ExpandedTermsSaver: Saver<Set<String>, Any> = listSaver(
    save = { it.toList() },
    restore = { it.filterIsInstance<String>().toSet() }
)

/**
 * Glossary term categories for structured financial browsing.
 */
enum class GlossaryCategory {
    ALL,
    PROFIT_REVENUE,
    MARKETING_ACQUISITION,
    LOGISTICS_OPERATIONS,
    CAPITAL_RISK
}

/**
 * Immutable pre-indexed model for zero-allocation searching, filtering, and rendering.
 */
@Immutable
data class GlossaryItem(
    val term: String,
    val altTerm: String,
    val description: String,
    val altDescription: String,
    val formula: String?,
    val category: GlossaryCategory,
    val normalizedTerm: String,
    val normalizedAltTerm: String,
    val normalizedDesc: String,
    val normalizedAltDesc: String
)

// Bidirectional mapping between Kurdish and English terms for cross-language search & resolution
val kurdishToEnglishTermMap = mapOf(
    "کۆی قازانجی پوختە" to "Total Net Profit",
    "داهات" to "Revenue",
    "قازانجی پوختەی هەر بەرهەمێک" to "Net Profit Per Product",
    "ئامانجی داواکارییەکانی ڕۆژانە" to "Target Daily Orders",
    "کۆی گشتی خەرجییەکانی پڕۆژە" to "Total Project Expenses",
    "خەرجییە ڕۆژانەییەکان" to "Daily Cash Burn",
    "ئامانجی داواکارییە سەرکەوتووەکان" to "Target Successful Orders",
    "نرخی فرۆشتن بە کڕیار" to "Retail Price",
    "تێچووی گەیشتنی بەرهەم" to "Sourcing Cost",
    "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" to "Average Order Value (AOV)",
    "داشکاندنی پێدراو (%)" to "Discounts (%)",
    "تێچووی بەدەستهێنانی کڕیار (CAC)" to "Customer Acquisition Cost (CAC)",
    "کرێی دەروازەی پارەدان (%)" to "Payment Gateway Fee (%)",
    "ڕێژەی دووبارە کڕینەوە (LTV)" to "Repeat Purchase Rate (LTV)",
    "کرێی گواستنەوە" to "Shipping Cost",
    "تێچووی پێچانەوە و گەیاندن" to "Packaging & Handling",
    "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" to "Rejection Rate (RTS %)",
    "سزای دارایی داواکارییە ڕەتکراوەکان" to "RTS Penalty Fee",
    "ڕێژەی پارە گەڕاندنەوە" to "Refund Rate (%)",
    "سزای پارە گەڕاندنەوەی دوای گەیاندن" to "Post-Delivery Refund Penalty",
    "ڕێژەی زیان/لەناوچوون" to "Damage/Loss Rate (%)",
    "ڕێژەی کاڵای نەفرۆشراو" to "Deadstock Rate (%)",
    "خەرجییە جێگیرەکانی مانگانە" to "Fixed Monthly Overhead (Rent/Salaries)",
    "ئامانجی قازانجی پوختە" to "Net Profit Target",
    "ماوەی کات (بە ڕۆژ)" to "Time Horizon (Days)",
    "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" to "Remittance Payout Delay (Days)",
    "قازانجی گشتی لە هەر داواکارییەک" to "Gross Profit Per Order",
    "خاڵی یەکسانبوونەوە (داواکارییەکان)" to "Break-even Orders",
    "کۆی گشتی داواکارییە نێردراوەکان" to "Total Dispatched Orders",
    "سەرمایەی کارپێکردنی پێویست" to "Working Capital Float"
)

val englishToKurdishTermMap = buildMap {
    kurdishToEnglishTermMap.forEach { (k, e) -> put(e, k) }
    put("Fixed Monthly Overhead", "خەرجییە جێگیرەکانی مانگانە")
    put("Fixed Monthly Overhead (Rent/Salaries)", "خەرجییە جێگیرەکانی مانگانە")
    put("Remittance Delay (Days)", "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)")
    put("Remittance Payout Delay (Days)", "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)")
    put("Ad Payment Gateway Fee (%)", "کرێی دەروازەی پارەدان (%)")
    put("Payment Gateway Fee (%)", "کرێی دەروازەی پارەدان (%)")
    put("Basket Size / Items per Order (UPT)", "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)")
    put("Items / Order (AOV)", "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)")
    put("Average Order Value (AOV)", "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)")
}

/**
 * Formula definitions mapping key terms to clean unit economics mathematical formulations.
 * Covers all 35 terms and synonyms across Kurdish and English with proper Unicode directional isolation.
 */
val termFormulaMap = mapOf(
    // 1. Total Net Profit
    "Total Net Profit" to "Total Net Profit = Target Successful Orders × Net Profit Per Order = Revenue - Sourcing Costs - Ad Spend - Shipping - RTS/Refund Penalties - Ops Cost - Prorated Overhead",
    "کۆی قازانجی پوختە" to "کۆی قازانجی پوختە = داواکارییە سەرکەوتووەکان × قازانجی پوختەی هەر داواکارییەک = کۆی داهات - تێچووی کاڵا - خەرجی ڕیکلام - کرێی گەیاندن - زیانی گەڕانەوە - پێچانەوە - خەرجی جێگیر",

    // 2. Revenue
    "Revenue" to "Revenue = Target Successful Orders × (Retail Price × (1 - Discounts % / 100) × Items Per Order)",
    "داهات" to "داهات = داواکارییە سەرکەوتووەکان × (نرخی فرۆشتن × (١ - داشکاندن / ١٠٠) × ژمارەی کاڵا لە داواکاریدا)",

    // 3. Net Profit Per Product
    "Net Profit Per Product" to "Net Profit Per Product = Net Profit Per Order / Items Per Order (AOV)",
    "قازانجی پوختەی هەر بەرهەمێک" to "قازانجی پوختەی هەر بەرهەمێک = قازانجی پوختەی هەر داواکارییەک / ژمارەی کاڵا لە هەر داواکارییەکدا \u2066(AOV)\u2069",

    // 4. Target Daily Orders
    "Target Daily Orders" to "Target Daily Orders = Target Successful Orders / Project Duration (Days)",
    "ئامانجی داواکارییەکانی ڕۆژانە" to "ئامانجی داواکارییەکانی ڕۆژانە = ئامانجی داواکارییە سەرکەوتووەکان / ماوەی پڕۆژە (بە ڕۆژ)",

    // 5. Total Project Expenses
    "Total Project Expenses" to "Total Expenses = Total Dispatched Sourcing + Total Ad Spend + Total Outbound Shipping + Total RTS/Refund Penalties + Ops + Prorated Overhead",
    "کۆی گشتی خەرجییەکانی پڕۆژە" to "کۆی گشتی خەرجییەکان = تێچووی کاڵا نێردراوەکان + خەرجی ڕیکلام + کرێی گەیاندن + سزاکانی گەڕانەوە + پێچانەوە + بەشی خەرجی جێگیر",

    // 6. Daily Cash Burn
    "Daily Cash Burn" to "Daily Cash Burn = (Dispatched Sourcing + Shipping + Return Penalties + CAC + Ops + Daily Overhead) × Daily Dispatches",
    "خەرجییە ڕۆژانەییەکان" to "خەرجییە ڕۆژانەییەکان = (تێچووی کاڵا + گەیاندن + سزای گەڕانەوە + \u2066CAC\u2069 + پێچانەوە + خەرجی جێگیری ڕۆژانە) × ناردنی ڕۆژانە",

    // 7. Target Successful Orders
    "Target Successful Orders" to "Target Successful Orders = ⌈(Target Net Profit + Prorated Fixed Overhead) / Gross Profit Per Successful Order⌉",
    "ئامانجی داواکارییە سەرکەوتووەکان" to "ئامانجی داواکارییە سەرکەوتووەکان = ⌈(ئامانجی قازانجی پوختە + بەشی خەرجی جێگیر) / قازانجی گشتی لە هەر داواکارییەک⌉",

    // 8. Retail Price
    "Retail Price" to "Effective Retail Price = Retail Price × (1 - Discounts % / 100) × Items Per Order",
    "نرخی فرۆشتن بە کڕیار" to "نرخی فرۆشتنی کاریگەر = نرخی فرۆشتن × (١ - ڕێژەی داشکاندن / ١٠٠) × ژمارەی کاڵا",

    // 9. Sourcing Cost
    "Sourcing Cost" to "Effective COGS = Sourcing Cost × Items Per Order",
    "تێچووی گەیشتنی بەرهەم" to "تێچووی کاڵای کاریگەر \u2066(COGS)\u2069 = تێچووی گەیشتنی بەرهەم × ژمارەی کاڵا لە داواکاریدا \u2066(AOV)\u2069",

    // 10. AOV / Items per order
    "Average Order Value (AOV)" to "Basket Size Multiplier (AOV) = Total Units Sold / Total Orders",
    "Basket Size / Items per Order (UPT)" to "Units Per Transaction (UPT) = Total Units Sold / Total Orders",
    "Items / Order (AOV)" to "Units Per Transaction (AOV) = Total Units Sold / Total Orders",
    "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" to "تێکڕای قەبارەی سەبەتە \u2066(AOV)\u2069 = کۆی ژمارەی یەکە فرۆشراوەکان / کۆی داواکارییەکان",

    // 11. Discounts (%)
    "Discounts (%)" to "Effective Price per Order = (Retail Price × (1 - Discounts % / 100)) × Items Per Order",
    "داشکاندنی پێدراو (%)" to "نرخی داشکێندراو = (نرخی سەرەکی × (١ - ڕێژەی داشکاندن % / ١٠٠)) × ژمارەی کاڵا",

    // 12. Customer Acquisition Cost (CAC)
    "Customer Acquisition Cost (CAC)" to "Effective CAC = (CAC × (1 + Payment Gateway Fee % / 100)) / LTV Multiplier",
    "تێچووی بەدەستهێنانی کڕیار (CAC)" to "تێچووی کاریگەری کڕیار = (\u2066CAC\u2069 × (١ + کرێی دەروازەی پارەدان % / ١٠٠)) / ڕێژەی دووبارە کڕینەوە \u2066(LTV)\u2069",

    // 13. Payment Gateway Fee (%)
    "Payment Gateway Fee (%)" to "Payment Surcharge = CAC × (Gateway Fee % / 100)",
    "Ad Payment Gateway Fee (%)" to "Payment Surcharge = CAC × (Gateway Fee % / 100)",
    "کرێی دەروازەی پارەدان (%)" to "کرێی دەروازەی پارەدان = \u2066CAC\u2069 × (ڕێژەی کرێی دەروازە % / ١٠٠)",

    // 14. Repeat Purchase Rate (LTV)
    "Repeat Purchase Rate (LTV)" to "CAC per Repeat Order = Ad Spend per Customer / Lifetime Order Multiplier",
    "ڕێژەی دووبارە کڕینەوە (LTV)" to "تێچووی کڕیار بۆ هەر کڕینێک = تێچووی ڕیکلام / تێکڕای ژمارەی کڕینەوەکانی کڕیار \u2066(LTV)\u2069",

    // 15. Shipping Cost
    "Shipping Cost" to "Total Shipping Cost = Total Dispatched Orders × Outbound Courier Shipping Fee",
    "کرێی گواستنەوە" to "کۆی کرێی گواستنەوە = کۆی داواکارییە نێردراوەکان × کرێی گەیاندنی یەک داواکاری",

    // 16. Packaging & Handling
    "Packaging & Handling" to "Total Fulfillment Cost = Total Dispatched Orders × Handling Cost per Order",
    "تێچووی پێچانەوە و گەیاندن" to "کۆی تێچووی پێچانەوە = کۆی داواکارییە نێردراوەکان × تێچووی پێچانەوە و پشتڕاستکردنەوە",

    // 17. Rejection Rate (RTS %)
    "Rejection Rate (RTS %)" to "RTS Orders = Total Dispatched Orders × (Rejection Rate % / 100)",
    "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" to "داواکارییە ڕەتکراوەکان = کۆی داواکارییە نێردراوەکان × (ڕێژەی ڕەتکردنەوە % / ١٠٠)",

    // 18. RTS Penalty Fee
    "RTS Penalty Fee" to "Total RTS Loss = Total Dispatched Orders × (RTS % / 100) × (Shipping + Ops + RTS Return Fee)",
    "سزای دارایی داواکارییە ڕەتکراوەکان" to "کۆی زیانی ڕەتکراوەکان = داواکارییە نێردراوەکان × (ڕێژەی ڕەتکردنەوە % / ١٠٠) × (گواستنەوە + پێچانەوە + سزای گەڕانەوە)",

    // 19. Refund Rate (%)
    "Refund Rate (%)" to "Actual Refund Orders = Dispatched Orders × (1 - RTS %) × (Refund Rate % / 100)",
    "ڕێژەی پارە گەڕاندنەوە" to "داواکارییە گەڕێنراوەکان = داواکارییە گەیشتووەکان × (ڕێژەی پارە گەڕاندنەوە % / ١٠٠)",

    // 20. Post-Delivery Refund Penalty
    "Post-Delivery Refund Penalty" to "Total Refund Loss = Delivered Orders × (Refund % / 100) × (Shipping + Ops + Post-Delivery Penalty)",
    "سزای پارە گەڕاندنەوەی دوای گەیاندن" to "کۆی زیانی پارە گەڕاندنەوە = داواکارییە گەیشتووەکان × (ڕێژەی گەڕاندنەوە % / ١٠٠) × (گواستنەوە + پێچانەوە + سزای هێنانەوە)",

    // 21. Damage/Loss Rate (%)
    "Damage/Loss Rate (%)" to "Total Damage Loss = Total Dispatched Orders × (Damage Rate % / 100) × COGS",
    "ڕێژەی زیان/لەناوچوون" to "زیانی کاڵای لەناوچوو = کۆی داواکارییە نێردراوەکان × (ڕێژەی زیان % / ١٠٠) × تێچووی کاڵا \u2066(COGS)\u2069",

    // 22. Deadstock Rate (%)
    "Deadstock Rate (%)" to "Deadstock Loss = Consumed Units × COGS × (Deadstock Rate % / (100 - Deadstock Rate %))",
    "ڕێژەی کاڵای نەفرۆشراو" to "زیانی کاڵای نەفرۆشراو = کاڵا بەکارهاتووەکان × تێچووی کاڵا × (ڕێژەی نەفرۆشراو % / (١٠٠ - ڕێژەی نەفرۆشراو %))",

    // 23. Fixed Monthly Overhead
    "Fixed Monthly Overhead (Rent/Salaries)" to "Prorated Overhead = Fixed Monthly Expenses × (Project Duration Days / 30.0)",
    "Fixed Monthly Overhead" to "Prorated Overhead = Fixed Monthly Expenses × (Project Duration Days / 30.0)",
    "خەرجییە جێگیرەکانی مانگانە" to "بەشی خەرجییە جێگیرەکان = خەرجی جێگیری مانگانە × (ماوەی پڕۆژە بە ڕۆژ / ٣٠)",

    // 24. Net Profit Target
    "Net Profit Target" to "Target Gross Required = Net Profit Target + Prorated Fixed Overhead",
    "ئامانجی قازانجی پوختە" to "قازانجی گشتی پێویست = ئامانجی قازانجی پوختە + بەشی خەرجییە جێگیرەکان",

    // 25. Time Horizon (Days)
    "Time Horizon (Days)" to "Proration Multiplier = Project Duration (Days) / 30.0",
    "ماوەی کات (بە ڕۆژ)" to "ڕێژەی کاتی پڕۆژە = ماوەی کات (بە ڕۆژ) / ٣٠",

    // 26. Remittance Delay (Days)
    "Remittance Payout Delay (Days)" to "Cash Flow Delay Cycle = Courier Remittance Delay Days",
    "Remittance Delay (Days)" to "Cash Flow Delay Cycle = Courier Remittance Delay Days",
    "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" to "خولەی دواکەوتنی پارە = ژمارەی ئەو ڕۆژانەی پارەکە لای کۆمپانیای گەیاندن دەمێنێتەوە",

    // 27. Gross Profit Per Order
    "Gross Profit Per Order" to "Gross Profit Per Order = Expected Value per Dispatch / Success Rate",
    "قازانجی گشتی لە هەر داواکارییەک" to "قازانجی گشتی لە هەر داواکارییەک = بەهای پێشبینیکراوی هەر ناردنێک / ڕێژەی سەرکەوتنی گەیاندن",

    // 28. Break-even Orders
    "Break-even Orders" to "Break-even Orders = ⌈Prorated Fixed Overhead / Gross Profit Per Successful Order⌉",
    "خاڵی یەکسانبوونەوە (داواکارییەکان)" to "خاڵی یەکسانبوون = ⌈بەشی خەرجییە جێگیرەکان / قازانجی گشتی لە هەر داواکارییەک⌉",

    // 29. Total Dispatched Orders
    "Total Dispatched Orders" to "Total Dispatched Orders = ⌈Target Successful Orders / ((1 - RTS %) × (1 - Refund %))⌉",
    "کۆی گشتی داواکارییە نێردراوەکان" to "کۆی داواکارییە نێردراوەکان = ⌈ئامانجی داواکارییە سەرکەوتووەکان / ((١ - ڕێژەی ڕەتکردنەوە) × (١ - ڕێژەی گەڕاندنەوە))⌉",

    // 30. Working Capital Float
    "Working Capital Float" to "Working Capital Float = (Dispatched Sourcing + Dispatched CAC + Ops + Daily Overhead) × Daily Dispatches × Remittance Delay Days",
    "سەرمایەی کارپێکردنی پێویست" to "سەرمایەی کارپێکردنی پێویست = (تێچووی کاڵا + \u2066CAC\u2069 + پێچانەوە + خەرجی جێگیر) × ناردنی ڕۆژانە × ڕۆژانی دواکەوتنی گەڕانەوەی پارە"
)

/**
 * Explicit deterministic dictionary category mappings covering all 35 terms and aliases
 * avoiding any substring collision issues.
 */
val termCategoryMap: Map<String, GlossaryCategory> = mapOf(
    // PROFIT_REVENUE
    "Total Net Profit" to GlossaryCategory.PROFIT_REVENUE,
    "کۆی قازانجی پوختە" to GlossaryCategory.PROFIT_REVENUE,
    "Revenue" to GlossaryCategory.PROFIT_REVENUE,
    "داهات" to GlossaryCategory.PROFIT_REVENUE,
    "Net Profit Per Product" to GlossaryCategory.PROFIT_REVENUE,
    "قازانجی پوختەی هەر بەرهەمێک" to GlossaryCategory.PROFIT_REVENUE,
    "Retail Price" to GlossaryCategory.PROFIT_REVENUE,
    "نرخی فرۆشتن بە کڕیار" to GlossaryCategory.PROFIT_REVENUE,
    "Sourcing Cost" to GlossaryCategory.PROFIT_REVENUE,
    "تێچووی گەیشتنی بەرهەم" to GlossaryCategory.PROFIT_REVENUE,
    "Average Order Value (AOV)" to GlossaryCategory.PROFIT_REVENUE,
    "Basket Size / Items per Order (UPT)" to GlossaryCategory.PROFIT_REVENUE,
    "Items / Order (AOV)" to GlossaryCategory.PROFIT_REVENUE,
    "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" to GlossaryCategory.PROFIT_REVENUE,
    "Gross Profit Per Order" to GlossaryCategory.PROFIT_REVENUE,
    "قازانجی گشتی لە هەر داواکارییەک" to GlossaryCategory.PROFIT_REVENUE,

    // MARKETING_ACQUISITION
    "Discounts (%)" to GlossaryCategory.MARKETING_ACQUISITION,
    "داشکاندنی پێدراو (%)" to GlossaryCategory.MARKETING_ACQUISITION,
    "Customer Acquisition Cost (CAC)" to GlossaryCategory.MARKETING_ACQUISITION,
    "تێچووی بەدەستهێنانی کڕیار (CAC)" to GlossaryCategory.MARKETING_ACQUISITION,
    "Payment Gateway Fee (%)" to GlossaryCategory.MARKETING_ACQUISITION,
    "Ad Payment Gateway Fee (%)" to GlossaryCategory.MARKETING_ACQUISITION,
    "کرێی دەروازەی پارەدان (%)" to GlossaryCategory.MARKETING_ACQUISITION,
    "Repeat Purchase Rate (LTV)" to GlossaryCategory.MARKETING_ACQUISITION,
    "ڕێژەی دووبارە کڕینەوە (LTV)" to GlossaryCategory.MARKETING_ACQUISITION,

    // LOGISTICS_OPERATIONS
    "Shipping Cost" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "کرێی گواستنەوە" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Packaging & Handling" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "تێچووی پێچانەوە و گەیاندن" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Rejection Rate (RTS %)" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "RTS Penalty Fee" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "سزای دارایی داواکارییە ڕەتکراوەکان" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Refund Rate (%)" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "ڕێژەی پارە گەڕاندنەوە" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Post-Delivery Refund Penalty" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "سزای پارە گەڕاندنەوەی دوای گەیاندن" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Damage/Loss Rate (%)" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "ڕێژەی زیان/لەناوچوون" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Deadstock Rate (%)" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "ڕێژەی کاڵای نەفرۆشراو" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "Total Dispatched Orders" to GlossaryCategory.LOGISTICS_OPERATIONS,
    "کۆی گشتی داواکارییە نێردراوەکان" to GlossaryCategory.LOGISTICS_OPERATIONS,

    // CAPITAL_RISK
    "Target Daily Orders" to GlossaryCategory.CAPITAL_RISK,
    "ئامانجی داواکارییەکانی ڕۆژانە" to GlossaryCategory.CAPITAL_RISK,
    "Total Project Expenses" to GlossaryCategory.CAPITAL_RISK,
    "کۆی گشتی خەرجییەکانی پڕۆژە" to GlossaryCategory.CAPITAL_RISK,
    "Daily Cash Burn" to GlossaryCategory.CAPITAL_RISK,
    "خەرجییە ڕۆژانەییەکان" to GlossaryCategory.CAPITAL_RISK,
    "Target Successful Orders" to GlossaryCategory.CAPITAL_RISK,
    "ئامانجی داواکارییە سەرکەوتووەکان" to GlossaryCategory.CAPITAL_RISK,
    "Fixed Monthly Overhead (Rent/Salaries)" to GlossaryCategory.CAPITAL_RISK,
    "Fixed Monthly Overhead" to GlossaryCategory.CAPITAL_RISK,
    "خەرجییە جێگیرەکانی مانگانە" to GlossaryCategory.CAPITAL_RISK,
    "Net Profit Target" to GlossaryCategory.CAPITAL_RISK,
    "ئامانجی قازانجی پوختە" to GlossaryCategory.CAPITAL_RISK,
    "Time Horizon (Days)" to GlossaryCategory.CAPITAL_RISK,
    "ماوەی کات (بە ڕۆژ)" to GlossaryCategory.CAPITAL_RISK,
    "Remittance Payout Delay (Days)" to GlossaryCategory.CAPITAL_RISK,
    "Remittance Delay (Days)" to GlossaryCategory.CAPITAL_RISK,
    "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" to GlossaryCategory.CAPITAL_RISK,
    "Break-even Orders" to GlossaryCategory.CAPITAL_RISK,
    "خاڵی یەکسانبوونەوە (داواکارییەکان)" to GlossaryCategory.CAPITAL_RISK,
    "Working Capital Float" to GlossaryCategory.CAPITAL_RISK,
    "سەرمایەی کارپێکردنی پێویست" to GlossaryCategory.CAPITAL_RISK
)

/**
 * Categorizes a term deterministically into one of the structured financial categories.
 */
fun categorizeGlossaryTerm(term: String): GlossaryCategory {
    termCategoryMap[term]?.let { return it }
    termCategoryMap[term.trim()]?.let { return it }
    val lower = term.lowercase(Locale.ROOT)
    return when {
        lower.contains("overhead") || lower.contains("float") || lower.contains("remittance") ||
        lower.contains("duration") || lower.contains("burn") || lower.contains("break-even") ||
        lower.contains("target") || lower.contains("capital") || lower.contains("expenses") ||
        lower.contains("خەرجی") || lower.contains("سەرمایە") || lower.contains("دواکەوتن") ||
        lower.contains("ماوە") || lower.contains("ئامانج") || lower.contains("یەکسانبوون") -> GlossaryCategory.CAPITAL_RISK

        lower.contains("deadstock") || lower.contains("dispatched") || lower.contains("shipping") ||
        lower.contains("packaging") || lower.contains("rts") || lower.contains("refund") ||
        lower.contains("damage") || lower.contains("rejection") || lower.contains("گەیاندن") ||
        lower.contains("پێچانەوە") || lower.contains("ڕەتکردنەوە") || lower.contains("گەڕاندنەوە") ||
        lower.contains("زیان") || lower.contains("نەفرۆشراو") || lower.contains("نێردراو") -> GlossaryCategory.LOGISTICS_OPERATIONS

        lower.contains("cac") || lower.contains("ad") || lower.contains("ltv") ||
        lower.contains("discount") || lower.contains("gateway") || lower.contains("marketing") ||
        lower.contains("ڕیکلام") || lower.contains("کڕیار") || lower.contains("داشکاندن") ||
        lower.contains("دەروازە") || lower.contains("دووبارە") -> GlossaryCategory.MARKETING_ACQUISITION

        else -> GlossaryCategory.PROFIT_REVENUE
    }
}

/**
 * Builds an immutable, pre-indexed list of glossary items with pre-calculated
 * normalized search tokens, avoiding runtime allocations during scrolling.
 */
fun buildIndexedGlossary(isKurdish: Boolean): List<GlossaryItem> {
    val dict = if (isKurdish) KurdishTerms.dictionary else EnglishTerms.dictionary
    return dict.map { (term, desc) ->
        val altTerm = if (isKurdish) kurdishToEnglishTermMap[term] ?: "" else englishToKurdishTermMap[term] ?: ""
        val altDesc = if (isKurdish) {
            if (altTerm.isNotEmpty()) EnglishTerms.getDescription(altTerm) else ""
        } else {
            if (altTerm.isNotEmpty()) KurdishTerms.getDescription(altTerm) else ""
        }
        val formula = termFormulaMap[term]
            ?: (if (altTerm.isNotEmpty()) termFormulaMap[altTerm] else null)
        val category = categorizeGlossaryTerm(term)

        GlossaryItem(
            term = term,
            altTerm = altTerm,
            description = desc,
            altDescription = altDesc,
            formula = formula,
            category = category,
            normalizedTerm = normalizeKurdishSearch(term),
            normalizedAltTerm = normalizeKurdishSearch(altTerm),
            normalizedDesc = normalizeKurdishSearch(desc),
            normalizedAltDesc = normalizeKurdishSearch(altDesc)
        )
    }
}

/**
 * Highlights occurrences of the search query in the displayed text with character-offset mapping.
 */
fun highlightSearchMatch(
    text: String,
    query: String,
    highlightColor: Color,
    normalColor: Color
): AnnotatedString {
    val trimmed = query.trim()
    if (trimmed.isEmpty() || text.isEmpty()) return AnnotatedString(text)

    val normSb = StringBuilder(text.length)
    val normToOrigMap = IntArray(text.length)
    var normLen = 0

    for (i in text.indices) {
        val ch = text[i]
        if (!isStrippedKurdishChar(ch)) {
            normSb.append(normalizeKurdishChar(ch))
            normToOrigMap[normLen] = i
            normLen++
        }
    }
    val normText = normSb.toString()

    val tokens = trimmed.split("\\s+".toRegex())
        .map { normalizeKurdishSearch(it) }
        .filter { it.isNotEmpty() }

    if (tokens.isEmpty() || normLen == 0) return AnnotatedString(text)

    val matchRanges = mutableListOf<IntRange>()
    for (token in tokens) {
        var matchIdx = normText.indexOf(token, 0)
        while (matchIdx != -1) {
            val endIdx = matchIdx + token.length
            val origStart = normToOrigMap[matchIdx]
            val origEnd = normToOrigMap[endIdx - 1] + 1
            if (origStart in text.indices && origEnd in 1..text.length && origStart < origEnd) {
                matchRanges.add(origStart until origEnd)
            }
            matchIdx = normText.indexOf(token, matchIdx + 1)
        }
    }

    if (matchRanges.isEmpty()) {
        val lowerText = text.lowercase(Locale.ROOT)
        for (token in tokens) {
            val lowerToken = token.lowercase(Locale.ROOT)
            var matchIdx = lowerText.indexOf(lowerToken, 0)
            while (matchIdx != -1) {
                val endIdx = matchIdx + lowerToken.length
                if (matchIdx in text.indices && endIdx in 1..text.length && matchIdx < endIdx) {
                    matchRanges.add(matchIdx until endIdx)
                }
                matchIdx = lowerText.indexOf(lowerToken, matchIdx + 1)
            }
        }
    }

    if (matchRanges.isEmpty()) return AnnotatedString(text)

    val sortedRanges = matchRanges.sortedBy { it.first }
    val mergedRanges = mutableListOf<IntRange>()
    for (range in sortedRanges) {
        if (mergedRanges.isEmpty()) {
            mergedRanges.add(range)
        } else {
            val last = mergedRanges.last()
            if (range.first <= last.last + 1) {
                mergedRanges[mergedRanges.size - 1] = last.first..maxOf(last.last, range.last)
            } else {
                mergedRanges.add(range)
            }
        }
    }

    val builder = AnnotatedString.Builder()
    var currentIdx = 0
    val highlightStyle = SpanStyle(
        color = highlightColor,
        fontWeight = FontWeight.Bold,
        background = highlightColor.copy(alpha = 0.12f)
    )

    for (range in mergedRanges) {
        val start = range.first.coerceIn(0, text.length)
        val end = (range.last + 1).coerceIn(0, text.length)
        if (start > currentIdx) builder.append(text.substring(currentIdx, start))
        if (start < end) {
            builder.pushStyle(highlightStyle)
            builder.append(text.substring(start, end))
            builder.pop()
        }
        currentIdx = maxOf(currentIdx, end)
    }
    if (currentIdx < text.length) builder.append(text.substring(currentIdx))

    return builder.toAnnotatedString()
}

/**
 * Full-screen Financial Glossary & Knowledge Base Screen.
 * Streamlined zero-redundancy tactile Neumorphic reference screen with search highlighting,
 * zero-allocation pre-indexing, accessible contrast, adaptive wide-screen layout, progressive back handling,
 * debounced search, and formula viewer.
 */
@Composable
fun GlossaryScreen(
    isKurdish: Boolean = true,
    initialSelectedTerm: String? = null,
    onClearSelectedTerm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val accessiblePrimary = if (!neuColors.isDark) NeuLightAccessiblePrimary else neuColors.primary
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(GlossaryCategory.ALL) }

    // Full state restoration for accordion expansion across orientation and process death
    var expandedTerms by rememberSaveable(stateSaver = ExpandedTermsSaver) {
        mutableStateOf(emptySet<String>())
    }

    val listState = rememberLazyListState()

    // Pre-indexed immutable glossary items
    val indexedGlossary = remember(isKurdish) { buildIndexedGlossary(isKurdish) }

    // 200ms debounce on search typing for high-performance rendering
    LaunchedEffect(searchQuery) {
        if (searchQuery.isEmpty()) {
            debouncedQuery = ""
        } else {
            delay(200L)
            debouncedQuery = searchQuery
        }
    }

    // Progressive BackHandler for Search queries and Category filtering
    if (searchQuery.isNotEmpty()) {
        BackHandler {
            searchQuery = ""
            debouncedQuery = ""
            focusManager.clearFocus()
        }
    } else if (selectedCategory != GlossaryCategory.ALL) {
        BackHandler {
            selectedCategory = GlossaryCategory.ALL
        }
    }

    // Hoisted stable callback for accordion toggling
    val onToggleExpand = remember {
        { term: String ->
            expandedTerms = if (expandedTerms.contains(term)) {
                expandedTerms - term
            } else {
                expandedTerms + term
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthClass = WindowWidthSizeClass.fromWidth(maxWidth)
        val isTablet = widthClass != WindowWidthSizeClass.Compact

        // Deep-linking auto-scroll with frame synchronization & tablet 2-column index compensation
        LaunchedEffect(initialSelectedTerm, isKurdish, indexedGlossary, isTablet) {
            if (initialSelectedTerm != null) {
                val resolvedTerm = if (indexedGlossary.any { it.term == initialSelectedTerm }) {
                    initialSelectedTerm
                } else if (isKurdish && englishToKurdishTermMap.containsKey(initialSelectedTerm)) {
                    englishToKurdishTermMap[initialSelectedTerm]
                } else if (!isKurdish && kurdishToEnglishTermMap.containsKey(initialSelectedTerm)) {
                    kurdishToEnglishTermMap[initialSelectedTerm]
                } else {
                    initialSelectedTerm
                }

                if (resolvedTerm != null && indexedGlossary.any { it.term == resolvedTerm }) {
                    expandedTerms = expandedTerms + resolvedTerm
                    searchQuery = ""
                    debouncedQuery = ""
                    selectedCategory = GlossaryCategory.ALL

                    val targetIndex = indexedGlossary.indexOfFirst { it.term == resolvedTerm }
                    if (targetIndex >= 0) {
                        val targetLazyIndex = if (isTablet) (targetIndex / 2) + HEADER_ITEMS_COUNT else targetIndex + HEADER_ITEMS_COUNT
                        snapshotFlow { listState.layoutInfo.totalItemsCount }
                            .filter { count -> count > targetLazyIndex }
                            .first()
                        listState.animateScrollToItem(index = targetLazyIndex)
                    }
                }
                onClearSelectedTerm()
            }
        }

        // Category Item Counts
        val categoryCounts = remember(indexedGlossary) {
            GlossaryCategory.entries.associateWith { cat ->
                if (cat == GlossaryCategory.ALL) indexedGlossary.size
                else indexedGlossary.count { it.category == cat }
            }
        }

        // Filtered items based on multi-token debounced search query and category
        val filteredItems = remember(debouncedQuery, selectedCategory, indexedGlossary) {
            val categoryFiltered = if (selectedCategory == GlossaryCategory.ALL) {
                indexedGlossary
            } else {
                indexedGlossary.filter { it.category == selectedCategory }
            }

            if (debouncedQuery.isBlank()) {
                categoryFiltered
            } else {
                val tokens = debouncedQuery.trim().split("\\s+".toRegex())
                    .map { normalizeKurdishSearch(it) }
                    .filter { it.isNotEmpty() }

                if (tokens.isEmpty()) {
                    categoryFiltered
                } else {
                    categoryFiltered.filter { item ->
                        tokens.all { token ->
                            item.normalizedTerm.contains(token) ||
                            item.normalizedDesc.contains(token) ||
                            item.normalizedAltTerm.contains(token) ||
                            item.normalizedAltDesc.contains(token)
                        }
                    }
                }
            }
        }

        // Memoize tablet 2-column chunks
        val chunkedItems = remember(filteredItems) { filteredItems.chunked(2) }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = if (isTablet) 24.dp else 16.dp,
                end = if (isTablet) 24.dp else 16.dp,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar & Filter Chips (Index 0) - Streamlined, eliminating redundant in-screen Header Card
            item(key = "search_and_filter") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tactile Neumorphic Recessed Search Field (Fixed 50dp height to eliminate typing jitter)
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
                            .clip(ShapeRadius14)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = accessiblePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (isKurdish) "گەڕان لە زاراوە و ڕوونکردنەوەکان..." else "Search terms or definitions...",
                                        fontSize = 13.sp,
                                        color = neuColors.textMuted
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = neuColors.textMain
                                    ),
                                    cursorBrush = SolidColor(accessiblePrimary),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    )
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        debouncedQuery = ""
                                    },
                                    modifier = Modifier.size(48.dp)
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

                    // Category Filter Pills with selectableGroup for accessibility (6dp vertical padding preserves shadows)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryPill(
                            label = if (isKurdish) "هەمووی" else "All",
                            count = categoryCounts[GlossaryCategory.ALL] ?: 0,
                            isSelected = selectedCategory == GlossaryCategory.ALL,
                            onClick = { selectedCategory = GlossaryCategory.ALL },
                            isKurdish = isKurdish
                        )
                        CategoryPill(
                            label = if (isKurdish) "قازانج و داهات" else "Profit & Margins",
                            count = categoryCounts[GlossaryCategory.PROFIT_REVENUE] ?: 0,
                            isSelected = selectedCategory == GlossaryCategory.PROFIT_REVENUE,
                            onClick = { selectedCategory = GlossaryCategory.PROFIT_REVENUE },
                            isKurdish = isKurdish
                        )
                        CategoryPill(
                            label = if (isKurdish) "مارکێتینگ (CAC)" else "Marketing & CAC",
                            count = categoryCounts[GlossaryCategory.MARKETING_ACQUISITION] ?: 0,
                            isSelected = selectedCategory == GlossaryCategory.MARKETING_ACQUISITION,
                            onClick = { selectedCategory = GlossaryCategory.MARKETING_ACQUISITION },
                            isKurdish = isKurdish
                        )
                        CategoryPill(
                            label = if (isKurdish) "گەیاندن و ڕەتکردنەوە" else "Logistics & RTS",
                            count = categoryCounts[GlossaryCategory.LOGISTICS_OPERATIONS] ?: 0,
                            isSelected = selectedCategory == GlossaryCategory.LOGISTICS_OPERATIONS,
                            onClick = { selectedCategory = GlossaryCategory.LOGISTICS_OPERATIONS },
                            isKurdish = isKurdish
                        )
                        CategoryPill(
                            label = if (isKurdish) "سەرمایە و خەرجی" else "Capital & Cashflow",
                            count = categoryCounts[GlossaryCategory.CAPITAL_RISK] ?: 0,
                            isSelected = selectedCategory == GlossaryCategory.CAPITAL_RISK,
                            onClick = { selectedCategory = GlossaryCategory.CAPITAL_RISK },
                            isKurdish = isKurdish
                        )
                    }
                }
            }

            // Results counter (Index 1) with polite accessibility live region and tight Gestalt grouping
            item(key = "results_counter") {
                val countDisplay = if (isKurdish) formatKurdishDigits(filteredItems.size) else filteredItems.size.toString()
                Text(
                    text = if (isKurdish) "زاراوەکان ($countDisplay):" else "Terms ($countDisplay):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = neuColors.textMuted,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 0.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }

            // Terms List or Interactive Empty State (Index 2+)
            if (filteredItems.isEmpty()) {
                item(key = "empty_state") {
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = neuColors.textMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isKurdish) "هیچ زاراوەیەک نەدۆزرایەوە" else "No matching financial terms found",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMuted
                            )
                            if (searchQuery.isNotEmpty() || selectedCategory != GlossaryCategory.ALL) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (searchQuery.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .neuFlat(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    cornerRadius = 12.dp,
                                                    elevation = 2.dp
                                                )
                                                .clip(ShapeRadius12)
                                                .clickable(role = Role.Button) {
                                                    searchQuery = ""
                                                    debouncedQuery = ""
                                                    focusManager.clearFocus()
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isKurdish) "سڕینەوەی گەڕان" else "Clear Search",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accessiblePrimary
                                            )
                                        }
                                    }
                                    if (selectedCategory != GlossaryCategory.ALL) {
                                        Box(
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .neuFlat(
                                                    lightShadowColor = neuColors.shadowLight,
                                                    darkShadowColor = neuColors.shadowDark,
                                                    backgroundColor = neuColors.surface,
                                                    cornerRadius = 12.dp,
                                                    elevation = 2.dp
                                                )
                                                .clip(ShapeRadius12)
                                                .clickable(role = Role.Button) {
                                                    selectedCategory = GlossaryCategory.ALL
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isKurdish) "گەڕان لە هەموو بەشەکان" else "Search All Categories",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accessiblePrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isTablet) {
                // Adaptive 2-Column Grid on Tablets & Foldables
                items(chunkedItems, key = { chunk -> chunk.first().term }) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (item in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                GlossaryTermCard(
                                    item = item,
                                    searchQuery = debouncedQuery,
                                    isExpanded = expandedTerms.contains(item.term) || (item.altTerm.isNotEmpty() && expandedTerms.contains(item.altTerm)),
                                    onToggleExpand = onToggleExpand,
                                    isKurdish = isKurdish
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Single Column Flow on Compact Phones
                items(filteredItems, key = { it.term }) { item ->
                    GlossaryTermCard(
                        item = item,
                        searchQuery = debouncedQuery,
                        isExpanded = expandedTerms.contains(item.term) || (item.altTerm.isNotEmpty() && expandedTerms.contains(item.altTerm)),
                        onToggleExpand = onToggleExpand,
                        isKurdish = isKurdish
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    isKurdish: Boolean
) {
    val neuColors = LocalNeumorphicColors.current
    val accessiblePrimary = if (!neuColors.isDark) NeuLightAccessiblePrimary else neuColors.primary
    val countDisplay = if (isKurdish) formatKurdishDigits(count) else count.toString()

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .neuPressed(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 14.dp,
                            elevation = 2.dp
                        )
                        .background(
                            color = neuColors.primary.copy(alpha = 0.08f),
                            shape = ShapeRadius14
                        )
                } else {
                    Modifier.neuFlat(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 14.dp,
                        elevation = 2.dp
                    )
                }
            )
            .clip(ShapeRadius14)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) accessiblePrimary else neuColors.textMuted
            )
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) {
                            if (neuColors.isDark) accessiblePrimary.copy(alpha = 0.20f)
                            else accessiblePrimary.copy(alpha = 0.08f)
                        } else {
                            neuColors.textMuted.copy(alpha = 0.12f)
                        },
                        shape = ShapeRadius8
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = countDisplay,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accessiblePrimary else neuColors.textMuted
                )
            }
        }
    }
}

@Composable
private fun GlossaryTermCard(
    item: GlossaryItem,
    searchQuery: String,
    isExpanded: Boolean,
    onToggleExpand: (String) -> Unit,
    isKurdish: Boolean
) {
    val neuColors = LocalNeumorphicColors.current
    val accessiblePrimary = if (!neuColors.isDark) NeuLightAccessiblePrimary else neuColors.primary

    // Synchronized critically damped spring motion
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ArrowRotation"
    )

    val stateDesc = if (isExpanded) {
        if (isKurdish) "کراوەتەوە" else "Expanded"
    } else {
        if (isKurdish) "داخراوە" else "Collapsed"
    }

    // Memoize AnnotatedStrings to achieve zero allocation during scroll
    val highlightedTerm = remember(item.term, searchQuery, accessiblePrimary, neuColors.textMain, isExpanded) {
        if (searchQuery.isBlank()) null
        else highlightSearchMatch(
            text = item.term,
            query = searchQuery,
            highlightColor = accessiblePrimary,
            normalColor = if (isExpanded) accessiblePrimary else neuColors.textMain
        )
    }

    val highlightedAltTerm = remember(item.altTerm, searchQuery, accessiblePrimary, neuColors.textMuted) {
        if (searchQuery.isBlank() || item.altTerm.isBlank()) null
        else highlightSearchMatch(
            text = item.altTerm,
            query = searchQuery,
            highlightColor = accessiblePrimary,
            normalColor = neuColors.textMuted
        )
    }

    val highlightedDesc = remember(item.description, searchQuery, accessiblePrimary, neuColors.textMain) {
        if (searchQuery.isBlank()) null
        else highlightSearchMatch(
            text = item.description,
            query = searchQuery,
            highlightColor = accessiblePrimary,
            normalColor = neuColors.textMain
        )
    }

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
            .clip(ShapeRadius20)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accessible Toggle Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { stateDescription = stateDesc }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (isExpanded) {
                            if (isKurdish) "داخستنی زاراوە" else "Collapse term"
                        } else {
                            if (isKurdish) "کردنەوەی زاراوە" else "Expand term"
                        },
                        onClick = { onToggleExpand(item.term) }
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    if (highlightedTerm != null) {
                        Text(
                            text = highlightedTerm,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) accessiblePrimary else neuColors.textMain,
                            modifier = Modifier.semantics { heading() }
                        )
                    } else {
                        Text(
                            text = item.term,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) accessiblePrimary else neuColors.textMain,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    if (item.altTerm.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        if (highlightedAltTerm != null) {
                            Text(
                                text = highlightedAltTerm,
                                fontSize = 11.sp,
                                color = neuColors.textMuted
                            )
                        } else {
                            Text(
                                text = item.altTerm,
                                fontSize = 11.sp,
                                color = neuColors.textMuted
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .then(
                            if (isExpanded) {
                                Modifier.neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 16.dp,
                                    elevation = 2.dp
                                )
                            } else {
                                Modifier.neuConvex(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 16.dp,
                                    elevation = 2.dp
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isExpanded) accessiblePrimary else neuColors.textMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = arrowRotation }
                    )
                }
            }

            // Description Body
            if (highlightedDesc != null) {
                Text(
                    text = highlightedDesc,
                    fontSize = 12.sp,
                    color = neuColors.textMain,
                    lineHeight = 18.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = neuColors.textMain,
                    lineHeight = 18.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Physics-based Accordion Formula Container
            AnimatedVisibility(
                visible = isExpanded && item.formula != null,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.formula != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 12.dp,
                                    elevation = 2.dp
                                )
                                .clip(ShapeRadius12)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isKurdish) "هاوکێشەی یەکە ئابوورییەکان:" else "Unit Economics Formula:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accessiblePrimary
                                )
                                Text(
                                    text = item.formula,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = neuColors.textMain,
                                    lineHeight = 17.sp,
                                    style = TextStyle(textDirection = TextDirection.Content)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
