package com.uniteconomics.calculator

import com.calculator.app.data.TimeframeOption as DataTimeframeOption
import com.calculator.app.data.CalculatorInputs as DataCalculatorInputs
import com.calculator.app.data.CalculationResult as DataCalculationResult
import com.calculator.app.data.CalculatorLogic as DataCalculatorLogic
import com.calculator.app.data.KurdishTerms as DataKurdishTerms
import com.calculator.app.data.EnglishTerms as DataEnglishTerms

typealias TimeframeOption = DataTimeframeOption
typealias CalculatorInputs = DataCalculatorInputs
typealias CalculationResult = DataCalculationResult

object CalculatorLogic {
    fun calculate(inputs: CalculatorInputs): CalculationResult = DataCalculatorLogic.calculate(inputs)
    fun runFinancialModel(inputs: CalculatorInputs): CalculationResult = DataCalculatorLogic.runFinancialModel(inputs)
}

object KurdishTerms {
    val dictionary: Map<String, String> get() = DataKurdishTerms.dictionary
    fun getDescription(term: String): String = DataKurdishTerms.getDescription(term)
}

object EnglishTerms {
    val dictionary: Map<String, String> get() = DataEnglishTerms.dictionary
    fun getDescription(term: String): String = DataEnglishTerms.getDescription(term)
}
