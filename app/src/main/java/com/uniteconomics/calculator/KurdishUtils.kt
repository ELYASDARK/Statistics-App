package com.uniteconomics.calculator

import java.text.SimpleDateFormat
import java.util.Locale

val DATE_TIME_FORMATTER: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
}

private val KURDISH_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/**
 * Checks whether a character is a BiDi control, zero-width, invisible Trojan Source character,
 * or directional formatting marker.
 */
fun isBiDiOrInvisibleChar(ch: Char): Boolean = when (ch) {
    // Unicode BiDi Formatting & Trojan Source Controls
    '\u202A', '\u202B', '\u202C', '\u202D', '\u202E', // LRE, RLE, PDF, LRO, RLO
    '\u2066', '\u2067', '\u2068', '\u2069',           // LRI, RLI, FSI, PDI
    '\u200E', '\u200F', '\u061C',                     // LRM, RLM, ALM
    // Zero-Width & Invisible Characters
    '\u200B', '\u200C', '\u200D', '\uFEFF', '\u2060', '\u00AD' -> true
    else -> false
}

/**
 * Normalizes Eastern Arabic-Indic (٠-٩), Persian (۰-۹), Arabic decimal comma (٫),
 * commas, and strips thousand separators and non-numeric characters for reliable Double parsing.
 */
fun normalizeNumericInput(input: String): String {
    if (input.isBlank()) return ""
    val limited = if (input.length > 30) input.take(30) else input
    val sb = StringBuilder(limited.length)
    var hasDecimal = false
    var hasSign = false

    // Strip BiDi controls & Trojan Source characters before processing
    val cleaned = limited.filterNot { isBiDiOrInvisibleChar(it) }.trim()
    for (i in cleaned.indices) {
        val ch = cleaned[i]
        when (ch) {
            in '0'..'9' -> sb.append(ch)
            // Eastern Arabic-Indic Digits (٠-٩)
            '٠' -> sb.append('0')
            '١' -> sb.append('1')
            '٢' -> sb.append('2')
            '٣' -> sb.append('3')
            '٤' -> sb.append('4')
            '٥' -> sb.append('5')
            '٦' -> sb.append('6')
            '٧' -> sb.append('7')
            '٨' -> sb.append('8')
            '٩' -> sb.append('9')
            // Persian / Extended Arabic-Indic Digits (۰-۹)
            '۰' -> sb.append('0')
            '۱' -> sb.append('1')
            '۲' -> sb.append('2')
            '۳' -> sb.append('3')
            '۴' -> sb.append('4')
            '۵' -> sb.append('5')
            '۶' -> sb.append('6')
            '۷' -> sb.append('7')
            '۸' -> sb.append('8')
            '۹' -> sb.append('9')
            // Decimal points: ASCII dot, Arabic decimal separator (٫ \u066B)
            '.', '٫' -> {
                if (!hasDecimal) {
                    sb.append('.')
                    hasDecimal = true
                }
            }
            // Thousands separators: ignore/skip comma (,), Arabic thousands separator (٬ \u066C), Arabic comma (، \u060C), apostrophe, space
            ',', '٬', '،', '\'', ' ' -> {
                // Ignore thousand separator
            }
            '-', '−' -> {
                if (!hasSign && sb.isEmpty()) {
                    sb.append('-')
                    hasSign = true
                }
            }
        }
    }
    return sb.toString()
}

/**
 * Strips Tatweel/Kashida, zero-width characters, Trojan Source controls, and all Arabic/Kurdish diacritics.
 */
fun isStrippedKurdishChar(ch: Char): Boolean = when {
    isBiDiOrInvisibleChar(ch) -> true
    ch == 'ـ' -> true
    // Arabic & Extended Diacritics / Harakat
    ch in '\u064B'..'\u065F' || ch == '\u0670' || ch in '\u06D6'..'\u06ED' -> true
    else -> false
}

/**
 * Maps Kurdish (Sorani & Kurmanji), Arabic, and Persian phonetic glyph variations.
 */
fun normalizeKurdishChar(ch: Char): Char = when (ch.lowercaseChar()) {
    // Eastern Arabic-Indic Digits (٠-٩) & Persian Digits (۰-۹)
    '٠', '۰' -> '0'; '١', '۱' -> '1'; '٢', '۲' -> '2'; '٣', '۳' -> '3'; '٤', '۴' -> '4'
    '٥', '۵' -> '5'; '٦', '۶' -> '6'; '٧', '۷' -> '7'; '٨', '۸' -> '8'; '٩', '۹' -> '9'

    // Alef & Hamza variations (Maps Hamza ئ to ا for search compatibility)
    'أ', 'إ', 'آ', 'ٱ', 'ٲ', 'ٳ', 'ئ' -> 'ا'

    // Yeh variations
    'ي', 'ى', 'ێ', 'ؽ', 'ؾ', 'ؿ' -> 'ی'

    // Kaf variations
    'ك', 'ڪ', 'ګ', 'ڬ' -> 'ک'

    // Waw variations
    'ۆ', 'ۇ', 'ۈ', 'ۉ', 'ۊ', 'ۋ' -> 'و'

    // Re variations
    'ڕ', 'ڑ', 'ڒ' -> 'ر'

    // Lam variations
    'ڵ', 'ڶ', 'ڷ' -> 'ل'

    // Heh & Teh Marbuta variations
    'ھ', 'ە', 'ة', 'ۃ', 'ہ', 'ۂ' -> 'ه'

    // Noon variations
    'ڼ', 'ں' -> 'ن'

    else -> ch.lowercaseChar()
}

/**
 * Comprehensive normalizer for Kurdish search tokens and pre-indexed strings,
 * fully sanitized against BiDi and Trojan Source tampering.
 */
fun normalizeKurdishSearch(input: String): String {
    if (input.isEmpty()) return ""
    val limited = if (input.length > 50) input.take(50) else input
    val sb = StringBuilder(limited.length)
    for (ch in limited) {
        if (!isStrippedKurdishChar(ch)) {
            sb.append(normalizeKurdishChar(ch))
        }
    }
    return sb.toString().trim()
}

/**
 * Formats integer into Eastern Arabic-Indic numerals for Kurdish localized UI display without runtime array allocation.
 */
fun formatKurdishDigits(number: Int): String {
    val str = number.toString()
    val sb = StringBuilder(str.length)
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(KURDISH_DIGITS[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Wraps formatted currency in directional isolates (\u2066 and \u2069) to guarantee
 * correct RTL rendering of negative signs, numbers, and currency suffixes.
 */
fun formatBidiCurrency(amountFormatted: String, currencySuffix: String = "IQD", isNegative: Boolean = false): String {
    val sign = if (isNegative) "-" else ""
    return "\u2066$sign$amountFormatted $currencySuffix\u2069"
}

private val US_DECIMAL_SYMBOLS = java.text.DecimalFormatSymbols(Locale.US)
val INTEGER_FORMAT = java.text.DecimalFormat("#,##0", US_DECIMAL_SYMBOLS)
val DECIMAL_FORMAT = java.text.DecimalFormat("#,##0.0", US_DECIMAL_SYMBOLS)

fun formatCurrencyAmount(
    amount: Double,
    isKurdish: Boolean = true,
    isDecimal: Boolean = false
): String {
    if (!amount.isFinite()) return if (isKurdish) "نەزانراو" else "N/A"
    val formatter = if (isDecimal) DECIMAL_FORMAT else INTEGER_FORMAT
    val formatted = synchronized(formatter) { formatter.format(kotlin.math.abs(amount)) }
    val sign = if (amount < 0) "-" else ""
    val currencySuffix = if (isKurdish) "IQD" else "IQD"
    return "\u2066$sign$formatted $currencySuffix\u2069"
}

fun isolateBiDiText(text: String): String = "\u2068$text\u2069"
