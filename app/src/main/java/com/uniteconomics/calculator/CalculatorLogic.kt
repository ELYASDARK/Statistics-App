package com.uniteconomics.calculator

import kotlin.math.ceil

enum class MetricStatus {
    GOOD, AVG, BAD
}

data class CalculatorInputs(
    val retail: Double = 35000.0,
    val sourcing: Double = 10000.0,
    val shipping: Double = 4000.0,
    val cac: Double = 5000.0,
    val adFee: Double = 3.0,
    val pack: Double = 500.0,
    val telecom: Double = 250.0,
    
    val reject: Double = 20.0,
    val returnFee: Double = 5000.0,
    val damage: Double = 5.0,
    val damageEnabled: Boolean = true,
    val deadstock: Double = 2.0,
    val deadstockEnabled: Boolean = true,
    
    val discount: Double = 0.0,
    val discountEnabled: Boolean = true,
    val ltv: Double = 1.1,
    val ltvEnabled: Boolean = true,
    val refund: Double = 1.0,
    val refundEnabled: Boolean = true,
    
    val aov: Double = 1.0,
    val opex: Double = 150000.0,
    val salary: Double = 300000.0,
    val remit: Double = 7.0,
    val goal: Double = 1000000.0,
    val days: Double = 30.0
)

data class CalculationResult(
    val netProfit: Double,
    val breakEven: Int,
    val totalUnits: Int,
    val totalSent: Int,
    val dailySales: Double,
    val totalCapital: Double,
    val floatCapital: Double,
    val trueNetProfitPerOrder: Double
)

object CalculatorLogic {

    fun runFinancialModel(inputs: CalculatorInputs): CalculationResult {
        // Enforce safety limits
        var days = inputs.days
        if (days <= 0) days = 1.0
        
        var ltv = if (inputs.ltvEnabled) inputs.ltv else 1.0
        if (ltv < 1.0) ltv = 1.0
        
        var deadstock = if (inputs.deadstockEnabled) inputs.deadstock else 0.0
        if (deadstock >= 100.0) deadstock = 99.0
        
        val discount = if (inputs.discountEnabled) inputs.discount else 0.0
        val refund = if (inputs.refundEnabled) inputs.refund else 0.0
        val damage = if (inputs.damageEnabled) inputs.damage else 0.0
        
        var reject = inputs.reject
        if ((reject + refund) >= 100.0) {
            reject = 99.0 - refund
        }

        val totalFixedCosts = inputs.opex + inputs.salary
        
        val effectiveRetail = inputs.retail * (1.0 - (discount / 100.0))
        val totalCacCost = inputs.cac * (1.0 + (inputs.adFee / 100.0))
        val effectiveCac = totalCacCost / ltv

        val retailPerOrder = effectiveRetail * inputs.aov
        val sourcingPerOrder = inputs.sourcing * inputs.aov

        val deadStockMarginLoss = sourcingPerOrder * (deadstock / (100.0 - deadstock))

        val rejectDecimal = reject / 100.0
        val refundDecimal = refund / 100.0
        var successDecimal = 1.0 - rejectDecimal - refundDecimal
        if (successDecimal <= 0.0) successDecimal = 0.01

        val profitIfSuccess = retailPerOrder - sourcingPerOrder - inputs.shipping - effectiveCac - inputs.pack - inputs.telecom - deadStockMarginLoss
        val lossIfReject = inputs.shipping + effectiveCac + inputs.pack + inputs.telecom + inputs.returnFee + (sourcingPerOrder * (damage / 100.0)) + deadStockMarginLoss
        val lossIfRefund = inputs.shipping + effectiveCac + inputs.pack + inputs.telecom + inputs.returnFee + (sourcingPerOrder * (damage / 100.0)) + deadStockMarginLoss

        val expectedProfitPerOrder = (successDecimal * profitIfSuccess) - (rejectDecimal * lossIfReject) - (refundDecimal * lossIfRefund)
        val netProfit = expectedProfitPerOrder / successDecimal

        var breakEven = 0
        var totalUnits = 0
        var totalSent = 0
        var dailySales = 0.0
        var totalCapital = 0.0
        var floatCapital = 0.0
        var trueNetProfitPerOrder = 0.0

        val effectiveGoal = inputs.goal + totalFixedCosts

        if (netProfit > 0.0) {
            breakEven = ceil(totalFixedCosts / netProfit).toInt()
            totalUnits = ceil(effectiveGoal / netProfit).toInt()
            totalSent = ceil(totalUnits / successDecimal).toInt()
            dailySales = totalUnits / days
            
            val upfrontCostPerOrder = sourcingPerOrder + inputs.shipping + effectiveCac + inputs.pack + inputs.telecom + deadStockMarginLoss
            totalCapital = totalSent * upfrontCostPerOrder
            
            val dailySentOrders = totalSent / days
            floatCapital = dailySentOrders * upfrontCostPerOrder * inputs.remit

            trueNetProfitPerOrder = netProfit - (totalFixedCosts / totalUnits)
        }

        return CalculationResult(
            netProfit = netProfit,
            breakEven = breakEven,
            totalUnits = totalUnits,
            totalSent = totalSent,
            dailySales = dailySales,
            totalCapital = totalCapital,
            floatCapital = floatCapital,
            trueNetProfitPerOrder = trueNetProfitPerOrder
        )
    }

    fun evaluateMetric(inputs: CalculatorInputs, key: String): MetricStatus {
        val safeRetail = if (inputs.retail > 0.0) inputs.retail else 1.0
        val safeGoal = if (inputs.goal > 0.0) inputs.goal else 1.0

        return when (key) {
            "sourcing" -> {
                val cogsPct = (inputs.sourcing / safeRetail) * 100.0
                if (cogsPct < 30.0) MetricStatus.GOOD else if (cogsPct <= 40.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "retail" -> {
                val cogsPct = (inputs.sourcing / safeRetail) * 100.0
                val gmPct = 100.0 - cogsPct
                if (gmPct >= 60.0) MetricStatus.GOOD else if (gmPct >= 40.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "shipping" -> {
                val shipPct = (inputs.shipping / safeRetail) * 100.0
                if (shipPct < 10.0) MetricStatus.GOOD else if (shipPct <= 15.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "cac" -> {
                val cacPct = (inputs.cac / safeRetail) * 100.0
                if (cacPct < 15.0) MetricStatus.GOOD else if (cacPct <= 30.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "ad-fee" -> {
                if (inputs.adFee < 2.5) MetricStatus.GOOD else if (inputs.adFee <= 4.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "pack" -> {
                val packPct = (inputs.pack / safeRetail) * 100.0
                if (packPct < 2.0) MetricStatus.GOOD else if (packPct <= 5.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "telecom" -> {
                val telPct = (inputs.telecom / safeRetail) * 100.0
                if (telPct < 1.0) MetricStatus.GOOD else if (telPct <= 2.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "reject" -> {
                if (inputs.reject < 20.0) MetricStatus.GOOD else if (inputs.reject <= 30.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "return" -> {
                if (inputs.returnFee <= inputs.shipping) MetricStatus.GOOD else if (inputs.returnFee <= inputs.shipping * 1.5) MetricStatus.AVG else MetricStatus.BAD
            }
            "damage" -> {
                val damage = if (inputs.damageEnabled) inputs.damage else 0.0
                if (damage < 2.0) MetricStatus.GOOD else if (damage <= 5.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "deadstock" -> {
                val deadstock = if (inputs.deadstockEnabled) inputs.deadstock else 0.0
                if (deadstock < 5.0) MetricStatus.GOOD else if (deadstock <= 10.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "discount" -> {
                val discount = if (inputs.discountEnabled) inputs.discount else 0.0
                if (discount <= 5.0) MetricStatus.GOOD else if (discount <= 15.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "ltv" -> {
                val ltv = if (inputs.ltvEnabled) inputs.ltv else 1.0
                if (ltv > 1.5) MetricStatus.GOOD else if (ltv >= 1.1) MetricStatus.AVG else MetricStatus.BAD
            }
            "refund" -> {
                val refund = if (inputs.refundEnabled) inputs.refund else 0.0
                if (refund < 5.0) MetricStatus.GOOD else if (refund <= 15.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "aov" -> {
                if (inputs.aov >= 1.5) MetricStatus.GOOD else if (inputs.aov > 1.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "remit" -> {
                if (inputs.remit <= 3.0) MetricStatus.GOOD else if (inputs.remit <= 7.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "opex" -> {
                val opexPct = (inputs.opex / safeGoal) * 100.0
                if (opexPct < 15.0) MetricStatus.GOOD else if (opexPct <= 30.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "salary" -> {
                val salPct = (inputs.salary / safeGoal) * 100.0
                if (salPct < 15.0) MetricStatus.GOOD else if (salPct <= 30.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "goal" -> {
                if (inputs.goal >= 2000000.0) MetricStatus.GOOD else if (inputs.goal >= 500000.0) MetricStatus.AVG else MetricStatus.BAD
            }
            "days" -> {
                if (inputs.days < 30.0) MetricStatus.GOOD else if (inputs.days <= 60.0) MetricStatus.AVG else MetricStatus.BAD
            }
            else -> MetricStatus.GOOD
        }
    }

    val businessTerms = mapOf(
        "Unit Retail Price (Gross)" to "ئەو نرخە کۆتاییەی کە کڕیار دەیبینێت و کاڵاکەی پێ دەفرۆشیت.",
        "Cost of Goods Sold (COGS)" to "تێچووی کڕین یان دروستکردنی یەک دانە لە کاڵاکەت بە کۆمەڵ.",
        "Units Per Transaction (UPT)" to "تێکڕای ژمارەی ئەو کاڵایانەی کە کڕیارێک لە یەک داواکارییدا دەیکڕێت. ئەگەر کڕیارەکانت زیاتر لە یەک کاڵا پێکەوە بکڕن، قازانجت زۆر زیاد دەکات.",
        "Trade Discount Rate" to "ئەو داشکاندنەی کە وەک ئۆفەر دەیدەیت بە کڕیارەکانت. ئەگەر داشکاندن ناکەیت، ئەمە بکوژێنەوە.",
        "Customer Acquisition Cost (CAC)" to "تێچووی ڕیکلام کردن (وەک سپۆنسەری فەیسبووک و تیکتۆک) بۆ دەستکەوتنی یەک داواکاریی سەرکەوتوو.",
        "Payment Gateway / FX Fee" to "ئەو بڕە پارە زیادیەی کە بانکەکان یان کۆمپانیاکانی ڕیکلام لێتی دەبڕن لە کاتی پارەدان بە کارت.",
        "Customer Lifetime Value (LTV)" to "پێشبینی ئەوەی کە کڕیارێک لە داهاتوودا چەند جارێکی تر کاڵات لێ دەکڕێتەوە، کە تێچووی ڕیکلامت لەسەر کەم دەکاتەوە. ئەگەر کڕیارەکانت تەنها یەک جار شت دەکڕن، ئەمە بکوژێنەوە.",
        "Outbound Freight / Shipping Cost" to "ئەو پارەیەی دەیدەیت بە کۆمپانیای گەیاندن بۆ گەیاندنی کاڵاکە. ئەمە دەبێت بدرێت تەنانەت ئەگەر کڕیارەکە کاڵاکە ڕەتیش بکاتەوە.",
        "Fulfillment (Pick & Pack) Cost" to "تێچووی کڕینی کارتۆن، کیسە، و ئەو ماندووبوونەی پێویستە بۆ ئامادەکردنی کاڵاکە بۆ ناردن.",
        "Order Verification Cost" to "تێچووی تەلەفۆنکردن یان ناردنی نامە بە وەتسئاپ بۆ کڕیار بۆ دڵنیابوونەوە لە داواکارییەکەی پێش ناردنی.",
        "Order Refusal (RTS) Rate" to "ڕێژەی ئەو داواکارییانەی کە کڕیار لە کاتی گەیاندن وەریان ناگرێت و دەگەڕێنەوە. ئەمە گەورەترین هۆکاری زیانە لە کاری ئۆنلایندا.",
        "Reverse Logistics (RTS) Fee" to "ئەو پارەیەی کۆمپانیای گەیاندن لێتی وەردەگرێت بۆ گەڕاندنەوەی ئەو کاڵایەی کە کڕیار وەری نەگرتووە و گەڕاوەتەوە.",
        "Post-Delivery Return Rate" to "ڕێژەی ئەو کڕیارانەی کە دوای وەرگرتنی کاڵاکە پەشیمان دەبنەوە و دەیگەڕێننەوە. ئەگەر تۆ کاڵا وەرناگریتەوە، ئەمە بکوژێنەوە.",
        "Inventory Shrinkage Rate" to "ڕێژەی ئەو کاڵایانەی لە کۆگادا یان لە کاتی گەیاندن ون دەبن یان دەشکێن و تێکدەچن. ئەگەر ئەمە لەسەر تۆ نییە، بکوژێنەوە.",
        "Inventory Obsolescence Rate" to "ڕێژەی ئەو کاڵایانەی مۆدێلیان بەسەردەچێت یان تێکدەچن و نافرۆشرێن. ئەگەر کاڵاکەت بەسەرچوونی نییە، بکوژێنەوە.",
        "Contribution Margin per Unit" to "ئەو قازانجەی کە لە فرۆشتنی یەک کاڵادا دەمێنێتەوە دوای دەرکردنی تێچووی کاڵا، ڕیکلام، و گەیاندن، بەڵام پێش دەرکردنی خەرجییە مانگانەکانی وەک کرێ.",
        "Fixed Operating Expenses (OPEX)" to "خەرجییە جێگیرەکانی مانگانە وەک کرێی شوێن، سیستەم، غاز و بەنزین کە دەبێت بیدەیت بێ گوێدانە ئەوەی چەندت فرۆشتووە.",
        "Fixed Payroll & Labor Costs" to "کۆی گشتی ئەو مووچەیەی کە مانگانە دەیدەیت بە کارمەندە جێگیرەکانت.",
        "Fully Allocated Net Profit per Unit" to "قازانجی ڕاستەقینە و سافی تۆ لە فرۆشتنی هەر داواکارییەکدا، دوای لێدەرکردنی تەواوی خەرجییەکان، زیانەکان، کرێ و مووچە.",
        "Target Net Income" to "ئەو بڕە پارەیەی کە دەتەوێت وەک قازانجی سافی لە کۆتایی مانگدا یان ماوەکەدا بیخەیتە گیرفانتەوە.",
        "Operational Period" to "ئەو ماوەیەی کە دیاریت کردووە بۆ گەیشتن بەم ئامانجە (بە شێوەیەکی گشتی ٣٠ ڕۆژە).",
        "Cash Conversion Cycle" to "ئەو ژمارە ڕۆژەی کە کۆمپانیای گەیاندن پارەکەت لای خۆی دەهێڵێتەوە پێش ئەوەی ڕادەستی تۆی بکاتەوە.",
        "Unit Sales Break-Even Point" to "ئەو ژمارە داواکارییەی کە پێویستە بیفرۆشیت تەنها بۆ ئەوەی خەرجییە مانگانەکانت (کرێ و مووچە) دابین بکەیت بەبێ هیچ قازانج یان زیانێک.",
        "Required Successful Conversions" to "ئەو ژمارە داواکارییە سەرکەوتووانەی کە پێویستە بیفرۆشیت بۆ ئەوەی بگەیت بەو ئامانجەی داتناوە بۆ قازانج.",
        "Gross Shipped Volume" to "کۆی گشتی ئەو داواکارییانەی کە دەبێت بیاننێریت لەگەڵ گەیاندن. ئەم ژمارەیە بەرزترە لە داواکارییە سەرکەوتووەکان چونکە ڕێژەی ئەو داواکارییانەش لەخۆ دەگرێت کە دەگەڕێنەوە.",
        "Total Capital Deployment" to "کۆی گشتی ئەو سەرمایە و پارەیەی کە پێویستە خەرجی بکەیت بۆ کڕینی کاڵا, ڕیکلام کردن، و گەیاندن بۆ گەیشتن بەم ئامانجە.",
        "Required Working Capital Float" to "ئەو پارە نەختینەیەی کە پێویستە بەردەوام لە گیرفانتدا یان بانکتدا هەبێت بۆ بەڕێوەبردنی کارەکانت لەو ماوەیەی کە پارەکەت لای کۆمپانیای گەیاندنە."
    )
}
