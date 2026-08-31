# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
Primary users are e-commerce founders, financial analysts, and venture investors who need to evaluate unit economics, cash burn, and operational risks under real-world logistics and remittance constraints.

## Product Purpose
An enterprise-grade native Android financial modeling and unit-economics calculation suite built to calculate unit profitability, expected-value (EV) risk metrics, cash burn rates, break-even thresholds, and Cash-on-Delivery (COD) working capital float requirements.

## Positioning
Combines real-time 19-parameter stochastic risk math (RTS rejections, return fees, deadstock, CAC/LTV multipliers) with offline native local SQLite scenario persistence, interactive Compose Canvas valuation/revenue visualization, and native Kurdish Sorani (RTL) & English (LTR) localization.

## Operating Context
- Mobile Android environment (API level 24+ / Android 7.0+ Nougat up to compile SDK 37).
- Offline-first execution utilizing Room SQLite persistence (`ProjectSQLiteDatabase.kt`).
- Dual layout environments supporting LTR (English) and RTL (Kurdish Sorani) with a 30-term financial dictionary modal.
- Light (`#E0E5EC`) and Dark (`#171C21`) mode operation.

## Capabilities and Constraints
- **Mathematical Engine**: 19 input parameters computing 13 output risk and valuation metrics in real-time (`CalculatorLogic.kt`).
- **Tactile Neumorphic Design System**: Custom Jetpack Compose Canvas modifiers (`neuFlat`, `neuPressed`, `neuConvex`, `neuConcave`) providing dual soft shadows, inner bevels, and extruded surfaces.
- **Custom Visualizations**: Compose Canvas `ValuationLineChart.kt` (cubic Bezier interpolation) and `RevenueGrowthBarChart.kt` (height-scaled concave bars).
- **Localization**: Full Kurdish Sorani (RTL) and English (LTR) language support with dynamic layout direction switching (`LayoutDirection.Rtl` vs. `LayoutDirection.Ltr`).
- **Offline Storage**: Room SQLite database (`ProjectSQLiteDatabase.kt`) for scenario save, load, update, and deletion.

## Brand Commitments
- **Name**: Native Android Tactile Neumorphic Financial Calculator (`com.uniteconomics.calculator`).
- **Visual Identity**: Custom Tactile Neumorphism (soft dual shadows, extruded/concave surfaces, light `#E0E5EC` / dark `#171C21`).
- **Tone & Voice**: Authoritative, precise, enterprise-grade financial modeling.

## Evidence on Hand
- Full codebase with 100% Kotlin & Jetpack Compose UI architecture.
- Unit tests covering `CalculatorLogicTest.kt`, `ChartMathTest.kt`, `RtlLayoutTest.kt`, `ThemeResolverTest.kt`, `ProjectEntityTest.kt`, `Milestone2ChallengerTest.kt`, and `Milestone3ChallengerTest.kt`.
- Comprehensive technical documentation in `README.md`.

## Product Principles
1. **Empirical Precision**: Math engine must rigorously account for logistics leakages (RTS rejection rate, return fees, damage, deadstock) rather than idealized margins.
2. **Tactile & Responsive Feedback**: Neumorphic surface elevations and press states must give physical, tactile feel to numerical inputs and sliders.
3. **Offline & Privacy-First**: All scenario calculations, historical saved models, and dictionary references execute locally without remote server dependencies.
4. **First-Class Regional Localization**: Sorani Kurdish (RTL) must be treated as a peer primary language with seamless layout mirror switching.

## Accessibility & Inclusion
- Full RTL layout support (`LayoutDirection.Rtl`) for Kurdish Sorani speakers.
- Dynamic OS dark mode synchronization.
