package com.uniteconomics.calculator

import androidx.compose.runtime.Immutable
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

enum class TimeframeOption {
    DAILY, WEEKLY, MONTHLY, TOTAL;

    fun toKurdishLabel(): String {
        return when (this) {
            DAILY -> "ڕۆژانە"
            WEEKLY -> "هەفتانە"
            MONTHLY -> "مانگانە"
            TOTAL -> "کۆی گشتی"
        }
    }
}

@Immutable
data class CalculatorInputs(
    val retailPrice: Double = 35000.0,
    val sourcingCost: Double = 10000.0,
    val aovMultiplier: Double = 1.0,
    val discounts: Double = 0.0,
    val cac: Double = 5000.0,
    val adFee: Double = 3.0,
    val ltvMultiplier: Double = 1.1,
    val shippingCost: Double = 4000.0,
    val opsCost: Double = 750.0,
    val rejectionRate: Double = 20.0,
    val rtsShippingFee: Double = 0.0,
    val refundRate: Double = 1.0,
    val refundPenalty: Double = 5000.0,
    val damageRate: Double = 5.0,
    val deadstockRate: Double = 2.0,
    val fixedMonthlyExpenses: Double = 450000.0,
    val targetProfitGoal: Double = 1000000.0,
    val projectDurationDays: Double = 30.0,
    val capitalRemittanceFrequency: Double = 7.0,
    val netTimeframe: TimeframeOption = TimeframeOption.TOTAL,
    val revTimeframe: TimeframeOption = TimeframeOption.TOTAL
) {
    // Forwarding alias properties for backward compatibility
    val effRetailPrice: Double get() = retailPrice
    val effSourcingCost: Double get() = sourcingCost
    val effAovMultiplier: Double get() = aovMultiplier
    val effDiscounts: Double get() = discounts
    val effLtvMultiplier: Double get() = ltvMultiplier
    val effShippingCost: Double get() = shippingCost
    val effOpsCost: Double get() = opsCost
    val effRejectionRate: Double get() = rejectionRate
    val effRtsShippingFee: Double get() = rtsShippingFee
    val effRefundRate: Double get() = refundRate
    val effDamageRate: Double get() = damageRate
    val effDeadstockRate: Double get() = deadstockRate
    val effFixedMonthlyExpenses: Double get() = fixedMonthlyExpenses
    val effTargetProfitGoal: Double get() = targetProfitGoal
    val effProjectDurationDays: Double get() = projectDurationDays
    val effCapitalRemittanceFrequency: Double get() = capitalRemittanceFrequency
}

enum class LossType {
    HEALTHY,           // Net margin >= 10%
    LOW_MARGIN,        // Net margin 0% - 10%
    OVERHEAD_DEFICIT,  // grossPerSuccess > 0, but totalNetProfit <= 0
    GROSS_LOSS         // grossPerSuccess <= 0 (negative unit economics)
}

@Immutable
data class LossDiagnostics(
    val lossType: LossType = LossType.HEALTHY,
    val breakEvenRetailPrice: Double = 0.0,
    val target15MarginPrice: Double = 0.0,
    val maxAllowableCac: Double = 0.0,
    val pureUnitMargin: Double = 0.0,
    val rtsLossPerSuccess: Double = 0.0,
    val primaryLossCulprit: String = ""
)

@Immutable
data class CalculationResult(
    val netProfitPerProduct: Double = 0.0,
    val netProfitTotal: Double = 0.0,
    val revenue: Double = 0.0,
    val displayNetProfit: Double = 0.0,
    val displayRevenue: Double = 0.0,
    val targetDailyOrders: Double = 0.0,
    val dailyCashBurn: Double = 0.0,
    val targetSuccessfulOrders: Int = 0,
    val grossProfitPerOrder: Double = 0.0,
    val breakevenOrders: Int = 0,
    val totalDispatchedOrders: Int = 0,
    val totalProjectExpenses: Double = 0.0,
    val workingCapitalFloat: Double = 0.0,
    val burnRateDay: Double = 0.0,
    val burnRateWeek: Double = 0.0,
    val burnRateMonth: Double = 0.0,
    val isLoss: Boolean = false,
    val lossType: LossType = LossType.HEALTHY,
    val diagnostics: LossDiagnostics = LossDiagnostics()
) {
    // Forwarding alias properties
    val effNetProfitPerProduct: Double get() = netProfitPerProduct
    val effNetProfitTotal: Double get() = netProfitTotal
    val effRevenue: Double get() = revenue
    val effTargetDailyOrders: Double get() = targetDailyOrders
    val effDailyCashBurn: Double get() = dailyCashBurn
    val effTargetSuccessfulOrders: Int get() = targetSuccessfulOrders
    val effGrossProfitPerOrder: Double get() = grossProfitPerOrder
    val effBreakevenOrders: Int get() = breakevenOrders
    val effTotalDispatchedOrders: Int get() = totalDispatchedOrders
    val effTotalProjectExpenses: Double get() = totalProjectExpenses
    val effWorkingCapitalFloat: Double get() = workingCapitalFloat
    val effBurnRateDay: Double get() = burnRateDay
    val effBurnRateWeek: Double get() = burnRateWeek
    val effBurnRateMonth: Double get() = burnRateMonth
    val effIsLoss: Boolean get() = isLoss

    // Canonical forwarding alias getters
    val targetUnits: Int get() = targetSuccessfulOrders
    val grossPerSuccess: Double get() = grossProfitPerOrder
    val breakEvenUnits: Int get() = breakevenOrders
    val totalSent: Int get() = totalDispatchedOrders
    val totalExpenses: Double get() = totalProjectExpenses
    val dailyTargetOrders: Double get() = targetDailyOrders
    val dailyBurn: Double get() = dailyCashBurn
}

object CalculatorLogic {

    private fun safe(v: Double, fallback: Double, min: Double? = null, max: Double? = null): Double {
        var res = if (v.isNaN() || v.isInfinite()) fallback else v
        min?.let { res = max(it, res) }
        max?.let { res = min(it, res) }
        return res
    }

    private const val CEIL_EPSILON = 1e-9

    fun calculate(inputs: CalculatorInputs): CalculationResult {
        val retail = safe(inputs.effRetailPrice, 35000.0, min = 0.0)
        val sourcing = safe(inputs.effSourcingCost, 10000.0, min = 0.0)
        val aov = safe(inputs.effAovMultiplier, 1.0, min = 0.01)
        val discount = safe(inputs.effDiscounts, 0.0, min = 0.0, max = 99.9)
        val cac = safe(inputs.cac, 5000.0, min = 0.0)
        val adFee = safe(inputs.adFee, 3.0, min = 0.0)
        val ltv = safe(inputs.effLtvMultiplier, 1.1, min = 0.01)
        val shipping = safe(inputs.effShippingCost, 4000.0, min = 0.0)
        val ops = safe(inputs.effOpsCost, 750.0, min = 0.0)
        val reject = safe(inputs.effRejectionRate, 20.0, min = 0.0, max = 99.9)
        val returnFeeRTS = safe(inputs.effRtsShippingFee, 0.0, min = 0.0)
        val refund = safe(inputs.effRefundRate, 1.0, min = 0.0, max = 99.9)
        val refundPenalty = safe(inputs.refundPenalty, 5000.0, min = 0.0)
        val damage = safe(inputs.effDamageRate, 5.0, min = 0.0, max = 99.9)
        val deadstock = safe(inputs.effDeadstockRate, 2.0, min = 0.0, max = 99.9)
        val fixedMonthly = safe(inputs.effFixedMonthlyExpenses, 450000.0, min = 0.0)
        val goal = safe(inputs.effTargetProfitGoal, 1000000.0, min = 0.0)
        val days = safe(inputs.effProjectDurationDays, 30.0, min = 1.0)
        val remit = safe(inputs.effCapitalRemittanceFrequency, 7.0, min = 0.0)

        // Unit Modifiers
        val discountFactor = 1.0 - (discount / 100.0)
        val effRetail = (retail * discountFactor) * aov
        val effCogs = sourcing * aov
        val effCac = (cac * (1.0 + (adFee / 100.0))) / ltv

        // Probabilities: Refunds are conditional on delivered parcels
        val rejectDec = reject / 100.0
        val rawRefundDec = refund / 100.0
        val actualRefundDec = (1.0 - rejectDec) * rawRefundDec
        val successDec = max(0.001, (1.0 - rejectDec) * (1.0 - rawRefundDec))
        val damageDec = damage / 100.0
        val deadDec = min(deadstock / 100.0, 0.999)

        // 1. Expected Value (EV) Math
        val profitSuccess = effRetail - effCogs - shipping - ops
        val lossRTS = shipping + ops + returnFeeRTS
        val lossRefund = shipping + ops + refundPenalty
        val damageLoss = damageDec * effCogs
        val consumedProb = min(1.0, successDec + damageDec)
        val deadstockLoss = consumedProb * effCogs * (deadDec / (1.0 - deadDec))

        val ev = (successDec * profitSuccess) - (rejectDec * lossRTS) - (actualRefundDec * lossRefund) - effCac - damageLoss - deadstockLoss
        val grossPerSuccess = ev / successDec

        // 2. Goal & Fixed Expense Math
        val fixedForTimeframe = fixedMonthly * (days / 30.0)
        val dailyFixedBurn = if (days > 0) fixedForTimeframe / days else fixedMonthly / 30.0
        val targetGrossReq = goal + fixedForTimeframe

        // 3. Diagnostic Levers Calculation
        val totalRiskCosts = (rejectDec * lossRTS) + (actualRefundDec * lossRefund) + effCac + damageLoss + deadstockLoss
        val reqEffRetailForBreakeven = effCogs + shipping + ops + (totalRiskCosts / successDec)
        val beRetailPrice = if (discountFactor * aov > 0) reqEffRetailForBreakeven / (discountFactor * aov) else 0.0
        val target15RetailPrice = beRetailPrice / 0.85

        val maxAllowableEffCac = (successDec * profitSuccess) - (rejectDec * lossRTS) - (actualRefundDec * lossRefund) - damageLoss - deadstockLoss
        val maxAllowableCac = max(0.0, (maxAllowableEffCac * ltv) / (1.0 + (adFee / 100.0)))

        // Classify Gross Loss
        val isGrossLoss = grossPerSuccess <= 0.0 || grossPerSuccess.isNaN() || grossPerSuccess.isInfinite()

        if (isGrossLoss) {
            val lossDailyNet = -dailyFixedBurn
            val lossTotalNet = -fixedForTimeframe
            val lossDisplayNet = when (inputs.netTimeframe) {
                TimeframeOption.DAILY -> lossDailyNet
                TimeframeOption.WEEKLY -> lossDailyNet * 7.0
                TimeframeOption.MONTHLY -> -fixedMonthly
                TimeframeOption.TOTAL -> lossTotalNet
            }

            val diagnostics = LossDiagnostics(
                lossType = LossType.GROSS_LOSS,
                breakEvenRetailPrice = beRetailPrice,
                target15MarginPrice = target15RetailPrice,
                maxAllowableCac = maxAllowableCac,
                pureUnitMargin = profitSuccess,
                rtsLossPerSuccess = (rejectDec * lossRTS) / successDec,
                primaryLossCulprit = if (retail <= sourcing) "PRICE_BELOW_COST" else if (effCac > profitSuccess) "HIGH_CAC" else "LOGISTICS_RISK"
            )

            return CalculationResult(
                netProfitPerProduct = (grossPerSuccess / aov),
                netProfitTotal = lossTotalNet,
                revenue = 0.0,
                displayNetProfit = lossDisplayNet,
                displayRevenue = 0.0,
                targetDailyOrders = 0.0,
                dailyCashBurn = dailyFixedBurn,
                targetSuccessfulOrders = 0,
                grossProfitPerOrder = if (grossPerSuccess.isNaN() || grossPerSuccess.isInfinite()) 0.0 else grossPerSuccess,
                breakevenOrders = 0,
                totalDispatchedOrders = 0,
                totalProjectExpenses = fixedForTimeframe,
                workingCapitalFloat = dailyFixedBurn * remit,
                burnRateDay = dailyFixedBurn,
                burnRateWeek = dailyFixedBurn * 7.0,
                burnRateMonth = fixedMonthly,
                isLoss = true,
                lossType = LossType.GROSS_LOSS,
                diagnostics = diagnostics
            )
        }

        // 4. Volume Targets & Net Profit Math with IEEE 754 Epsilon Tolerance
        val rawTargetUnits = targetGrossReq / grossPerSuccess
        val targetUnits = ceil(rawTargetUnits - CEIL_EPSILON).toLong().coerceIn(0L, 100_000_000L).toInt()
        val breakEvenUnits = ceil((fixedForTimeframe / grossPerSuccess) - CEIL_EPSILON).toLong().coerceIn(0L, 100_000_000L).toInt()
        val totalSent = ceil((targetUnits.toDouble() / successDec) - CEIL_EPSILON).toLong().coerceIn(0L, 100_000_000L).toInt()
        val dailyTargetOrders = if (days > 0.0) targetUnits.toDouble() / days else 0.0

        val netPerOrder = grossPerSuccess - if (targetUnits > 0) (fixedForTimeframe / targetUnits.toDouble()) else 0.0
        val netPerProduct = netPerOrder / aov

        // Dynamic Net Profit calculation based on dropdown selection
        val totalNetProfit = targetUnits.toDouble() * netPerOrder
        val dailyNetProfit = if (days > 0.0) totalNetProfit / days else 0.0

        val displayNet = when (inputs.netTimeframe) {
            TimeframeOption.DAILY -> dailyNetProfit
            TimeframeOption.WEEKLY -> dailyNetProfit * 7.0
            TimeframeOption.MONTHLY -> dailyNetProfit * 30.0
            TimeframeOption.TOTAL -> totalNetProfit
        }

        // 5. Revenue Calculation
        val totalRevenue = targetUnits.toDouble() * effRetail
        val dailyRevenue = if (days > 0.0) totalRevenue / days else 0.0

        val displayRevenue = when (inputs.revTimeframe) {
            TimeframeOption.DAILY -> dailyRevenue
            TimeframeOption.WEEKLY -> dailyRevenue * 7.0
            TimeframeOption.MONTHLY -> dailyRevenue * 30.0
            TimeframeOption.TOTAL -> totalRevenue
        }

        // 6. Total Project Expenses
        val invCash = ((totalSent.toDouble() * consumedProb) / (1.0 - deadDec)) * effCogs
        val adsCash = totalSent.toDouble() * effCac
        val shipCash = totalSent.toDouble() * shipping
        val returnCash = totalSent.toDouble() * ((rejectDec * returnFeeRTS) + (actualRefundDec * refundPenalty))
        val opsCash = totalSent.toDouble() * ops
        val totalCap = invCash + adsCash + shipCash + returnCash + opsCash + fixedForTimeframe

        // 7. Daily Expense Run-Rate
        val dailyDispatches = totalSent.toDouble() / days
        val invPerDispatch = (effCogs * consumedProb) / (1.0 - deadDec)
        val adPerDispatch = effCac
        val fixedPerDispatch = if (totalSent > 0) (fixedForTimeframe / totalSent.toDouble()) else 0.0
        val returnCostPerDispatch = (rejectDec * returnFeeRTS) + (actualRefundDec * refundPenalty)

        val dailyBurn = (invPerDispatch + shipping + returnCostPerDispatch + adPerDispatch + ops + fixedPerDispatch) * dailyDispatches

        // 8. Float Math
        val upfrontCashPerDispatch = effCogs + effCac + ops + fixedPerDispatch
        val floatReq = upfrontCashPerDispatch * dailyDispatches * remit

        val marginRatio = if (effRetail > 0) netPerOrder / effRetail else 0.0
        val lossType = when {
            totalNetProfit <= 0.0 || netPerOrder <= 0.0 -> LossType.OVERHEAD_DEFICIT
            marginRatio < 0.10 -> LossType.LOW_MARGIN
            else -> LossType.HEALTHY
        }

        val isLossState = lossType == LossType.OVERHEAD_DEFICIT || lossType == LossType.GROSS_LOSS

        val diagnostics = LossDiagnostics(
            lossType = lossType,
            breakEvenRetailPrice = beRetailPrice,
            target15MarginPrice = target15RetailPrice,
            maxAllowableCac = maxAllowableCac,
            pureUnitMargin = profitSuccess,
            rtsLossPerSuccess = (rejectDec * lossRTS) / successDec,
            primaryLossCulprit = if (marginRatio < 0.10) "TIGHT_MARGIN" else "OVERHEAD_PRESSURE"
        )

        return CalculationResult(
            netProfitPerProduct = netPerProduct,
            netProfitTotal = totalNetProfit,
            revenue = totalRevenue,
            displayNetProfit = displayNet,
            displayRevenue = displayRevenue,
            targetDailyOrders = dailyTargetOrders,
            dailyCashBurn = max(dailyFixedBurn, dailyBurn),
            targetSuccessfulOrders = targetUnits,
            grossProfitPerOrder = if (grossPerSuccess.isNaN() || grossPerSuccess.isInfinite()) 0.0 else grossPerSuccess,
            breakevenOrders = breakEvenUnits,
            totalDispatchedOrders = totalSent,
            totalProjectExpenses = totalCap,
            workingCapitalFloat = floatReq,
            burnRateDay = max(dailyFixedBurn, dailyBurn),
            burnRateWeek = max(dailyFixedBurn * 7.0, dailyBurn * 7.0),
            burnRateMonth = max(fixedMonthly, dailyBurn * 30.0),
            isLoss = isLossState,
            lossType = lossType,
            diagnostics = diagnostics
        )
    }

    fun runFinancialModel(inputs: CalculatorInputs): CalculationResult {
        return calculate(inputs)
    }
}

object KurdishTerms {
    val dictionary = mapOf(
        "کۆی قازانجی پوختە" to "کۆی گشتی ئەو قازانجە سافییەی دەمێنێتەوە دوای لێدەرکردنی هەموو خەرجییەکان، کە دەتوانیت بەپێی کات بیگۆڕیت (ڕۆژانە، هەفتانە، مانگانە، کۆی گشتی).",
        "داهات" to "کۆی گشتی داهاتی بەدەستهاتوو لە کڕیارەکانەوە پێش لێدەرکردنی هەر خەرجییەک (بەرهەم، ڕیکلام، گواستنەوە، کرێ).",
        "قازانجی پوختەی هەر بەرهەمێک" to "ئەو قازانجە سافییەی لە یەک کاڵای فیزیکی دەست دەکەوێت دوای لێدەرکردنی هەموو خەرجییەکان (تێچووی کاڵا، ڕیکلام، گواستنەوە، گەڕانەوە، زیانەکان و بەشێک لە کرێ/مووچەی مانگانە).",
        "ئامانجی داواکارییەکانی ڕۆژانە" to "ئەو ژمارە دروستەی فرۆشتنە سەرکەوتووەکان کە پێویستە ڕۆژانە ئەنجامی بدەیت بۆ ئەوەی لە کاتی خۆیدا بگەیتە ئامانجەکەت.",
        "کۆی گشتی خەرجییەکانی پڕۆژە" to "کۆی گشتی هەموو ئەو خەرجییانەی لە تەواوی ماوەکەدا دروست بوون (ڕیکلام، هەموو کاڵا بەکارهاتووەکان، گواستنەوە، پێچانەوە). تێبینی: پێویست ناکات هەموو ئەم بڕە پارەیەت بە یەکجار لە بانکدا هەبێت چونکە پارەکان دووبارە بەکاردەهێنیتەوە. سەیری 'سەرمایەی کارپێکردنی پێویست' بکە بۆ زانینی باڵانسی پێویست.",
        "خەرجییە ڕۆژانەییەکان" to "تێکڕای بەهای ڕۆژانەی هەموو خەرجییە دروستبووەکان (کاڵای بەکارهاتوو، گواستنەوە، ڕیکلام، پێچانەوە، سزای دارایی گەڕانەوە، خەرجییە جێگیرەکان) چ پێشوەختە درابێت یان دواتر لە پارەی وەرگیراو ببڕدرێت.",
        "ئامانجی داواکارییە سەرکەوتووەکان" to "ئەو ژمارە تەواوەی داواکارییە گەیەندراو و پارە بۆدراوانەی کە پێویستن بۆ گەیشتن بە ئامانجی قازانجی پوختە.",
        "نرخی فرۆشتن بە کڕیار" to "ئەو نرخەی کڕیار بۆ یەک دانە کاڵا دەیدات.",
        "تێچووی گەیشتنی بەرهەم" to "نرخی دابینکەر + تێچووی گەشتکردن/گواستنەوە کە پێویستە بۆ چوونە شارێکی تر و هێنانەوەی کاڵاکان بۆ ماڵەوە.",
        "ژمارەی کاڵا لە هەر داواکارییەکدا (AOV)" to "تێکڕای ژمارەی ئەو کاڵا فیزیکییانەی کڕیارێک لە یەک داواکاریدا دەیکڕێت.",
        "داشکاندنی پێدراو (%)" to "ڕێژەی سەدی ئەو داشکاندنەی بە کڕیار دراوە.",
        "تێچووی بەدەستهێنانی کڕیار (CAC)" to "ئەو خەرجییەی ڕیکلام کە پێویستە بۆ بەدەستهێنانی یەک کڕیاری پشتڕاستکراوە.",
        "کرێی دەروازەی پارەدان (%)" to "ڕێژەی سەدی ئەو کرێیەی کە لەلایەن دەروازەکانی پارەدان/جزدانەکانەوە (وەک ZainCash، QiCard) وەردەگیرێت بۆ پڕکردنەوەی کارتی تۆڕەکانی ڕیکلام.",
        "ڕێژەی دووبارە کڕینەوە (LTV)" to "تێکڕای کۆی ئەو کڕینانەی کڕیارێک لە ماوەی مامەڵەکردنیدا دەیکات.",
        "کرێی گواستنەوە" to "ئەو بڕە پارەیەی کۆمپانیای گەیاندن وەریدەگرێت بۆ گەیاندنی پاکەتەکە بە کڕیار.",
        "تێچووی پێچانەوە و گەیاندن" to "تێچووی کەرەستەی پێچانەوە، پشتڕاستکردنەوەی تەلەفۆنی، و تێچووی گواستنەوە/بەنزین بۆ لێخوڕینی ١٠ خولەک بۆ گەیاندنی داواکارییەکان بە کۆمپانیای گەیاندن.",
        "ڕێژەی داواکارییە ڕەتکراوەکان (RTS)" to "ڕێژەی سەدی ئەو پاکەتانەی کە لەلایەن کڕیارەوە لە کاتی گەیاندندا ڕەت دەکرێنەوە.",
        "سزای دارایی داواکارییە ڕەتکراوەکان" to "ئەو سزایە داراییەی کە کۆمپانیای گەیاندن وەریدەگرێت بۆ گەڕاندنەوەی ئەو پاکەتەی کە لە کاتی گەیاندندا ڕەتکراوەتەوە (زۆرجار سفرە ئەگەر لێی خۆش بن).",
        "ڕێژەی پارە گەڕاندنەوە" to "ڕێژەی سەدی ئەو کەسانەی کە دوای وەرگرتنی کاڵاکە داوای گەڕاندنەوەی پارەکەیان دەکەن.",
        "سزای پارە گەڕاندنەوەی دوای گەیاندن" to "ئەو سزایە داراییەی کۆمپانیای گەیاندن وەریدەگرێت بۆ هێنانەوەی کاڵایەک کاتێک کڕیار چەند ڕۆژێک دوای گەیاندنی سەرکەوتوو داوای گەڕاندنەوەی پارە دەکات.",
        "ڕێژەی زیان/لەناوچوون" to "ڕێژەی سەدی ئەو کاڵایانەی لە کاتی گواستنەوەدا زیانیان پێدەگات یان ون دەبن.",
        "ڕێژەی کاڵای نەفرۆشراو" to "ڕێژەی سەدی ئەو کاڵایانەی کە نافرۆشرێن یان بەسەردەچن.",
        "خەرجییە جێگیرەکانی مانگانە" to "تێچووە جێگیرەکانی مانگانە (کرێ، بەرنامە) + مووچەی کارمەندان و تێچووە جێگیرەکانی کارکردن.",
        "ئامانجی قازانجی پوختە" to "ئامانجی قازانجی پوختەی داواکراو.",
        "ماوەی کات (بە ڕۆژ)" to "ئەو ڕۆژانەی تەرخانکراون بۆ گەیشتن بە قازانجی ئامانج.",
        "دواکەوتنی گەڕانەوەی پارە (بە ڕۆژ)" to "ئەو ڕۆژانەی کە کۆمپانیای گەیاندن پارەکەت لای خۆی دەهێڵێتەوە پێش ئەوەی پێت بدات.",
        "قازانجی گشتی لە هەر داواکارییەک" to "قازانجی هەر فرۆشتنێکی سەرکەوتوو دوای دانی پارەی کاڵاکە، گواستنەوە، ڕیکلام، و هەژمارکردنی مەترسییەکانی شکست/گەڕانەوە. (کرێی مانگانە/مووچە ناگرێتەوە).",
        "خاڵی یەکسانبوونەوە (داواکارییەکان)" to "ئەو ژمارە داواکارییەی کە تەنها بۆ دانەوەی خەرجییە جێگیرەکانی مانگانە پێویستە.",
        "کۆی گشتی داواکارییە نێردراوەکان" to "کۆی گشتی ژمارەی پاکەتە نێردراوەکان (بەوانەشەوە کە لە کۆتاییدا ڕەت دەکرێنەوە).",
        "سەرمایەی کارپێکردنی پێویست" to "زۆر گرنگ: ئەو نەختینە ڕاستەقینەیەی لە ڕۆژی یەکەمدا لە هەژماری بانکییەکەتدا پێویستە بۆ کڕینی کاڵاکان، دانی پارەی ڕیکلام، و دابینکردنی خەرجییەکانی گەیاندن لەکاتێکدا چاوەڕێی کۆمپانیای گەیاندن دەکەیت یەکەمین پارەت بۆ بگەڕێنێتەوە. (وا دادەنێت کە پارەی گواستنەوە/سزاکان لە پارەی وەرگیراو دەبڕدرێن، نەک پێشوەختە بدرێن)."
    )

    fun getDescription(term: String): String {
        return dictionary[term] ?: "ڕوونکردنەوە نەدۆزرایەوە."
    }
}

object EnglishTerms {
    val dictionary = mapOf(
        "Total Net Profit" to "Total net profit remaining after deducting all expenses, viewable across daily, weekly, monthly, or total campaign timeframes.",
        "Revenue" to "Total gross top-line revenue collected from customers before deducting any expenses.",
        "Net Profit Per Product" to "Net profit earned per single physical product unit sold after deducting all costs including inventory, ads, shipping, returns, damage, and prorated overhead.",
        "Target Daily Orders" to "Required count of successful delivered orders per day to reach your net profit target on schedule.",
        "Total Project Expenses" to "Total cumulative operational expenses over the project duration (ads, inventory, shipping, fulfillment, penalties). Note: You do not need this full cash upfront; see Working Capital Float.",
        "Daily Cash Burn" to "Average daily capital expenditure rate across inventory, advertising, shipping, return penalties, and fixed monthly overhead.",
        "Target Successful Orders" to "Total count of successfully delivered and paid orders required to hit your net profit goal.",
        "Retail Price" to "Selling price paid by the customer for a single product unit.",
        "Sourcing Cost" to "Supplier unit price plus transportation and landing costs to bring the product to inventory.",
        "Basket Size / Items per Order (UPT)" to "Average number of physical units purchased by a customer per single order (Units Per Transaction).",
        "Average Order Value (AOV)" to "Average number of physical units purchased by a customer per single order (Units Per Transaction).",
        "Items / Order (AOV)" to "Average number of physical units purchased by a customer per single order (Units Per Transaction).",
        "Discounts (%)" to "Percentage discount offered to customers.",
        "Customer Acquisition Cost (CAC)" to "Ad spend required to acquire one confirmed purchasing customer.",
        "Payment Gateway Fee (%)" to "Percentage transaction fee charged by payment gateways/wallets (e.g., ZainCash, QiCard) to top up ad networks.",
        "Ad Payment Gateway Fee (%)" to "Percentage transaction fee charged by payment gateways/wallets (e.g., ZainCash, QiCard) to top up ad networks.",
        "Repeat Purchase Rate (LTV)" to "Average multiplier of total repeat orders a customer places over their relationship.",
        "Shipping Cost" to "Outbound delivery fee charged by courier companies to deliver the parcel.",
        "Packaging & Handling" to "Fulfillment costs including boxes, tape, phone call confirmations, and local transport.",
        "Rejection Rate (RTS %)" to "Percentage of dispatched parcels rejected by customers upon delivery.",
        "RTS Penalty Fee" to "Penalty fee charged by delivery companies to return rejected parcels.",
        "Refund Rate (%)" to "Percentage of delivered orders resulting in post-delivery customer refunds.",
        "Post-Delivery Refund Penalty" to "Penalty fee charged by courier to retrieve returned items after successful delivery.",
        "Damage/Loss Rate (%)" to "Percentage of inventory damaged or lost during transit.",
        "Deadstock Rate (%)" to "Percentage of inventory that remains unsold or expires.",
        "Fixed Monthly Overhead (Rent/Salaries)" to "Fixed monthly recurring costs including rent, software, utilities, and staff salaries.",
        "Fixed Monthly Overhead" to "Fixed monthly recurring costs including rent, software, utilities, and staff salaries.",
        "Net Profit Target" to "Target bottom-line net profit goal for the campaign.",
        "Time Horizon (Days)" to "Project timeframe in days allocated to reach the profit goal.",
        "Remittance Payout Delay (Days)" to "Number of days courier companies hold collected cash before remitting to your account.",
        "Remittance Delay (Days)" to "Number of days courier companies hold collected cash before remitting to your account.",
        "Gross Profit Per Order" to "Profit per successful order after product cost, shipping, ads, and risk factors (excluding fixed overhead).",
        "Break-even Orders" to "Minimum number of orders needed solely to cover fixed monthly overhead.",
        "Total Dispatched Orders" to "Total count of parcels shipped out including those eventually rejected.",
        "Working Capital Float" to "CRITICAL: Actual liquid cash required on Day 1 in your bank account to pay for inventory, ads, and shipping while awaiting courier cash remittance."
    )

    fun getDescription(term: String): String {
        return dictionary[term] ?: "Explanation not found."
    }
}
