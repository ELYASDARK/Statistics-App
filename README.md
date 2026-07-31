# 🤖 Native Android Tactile Neumorphic Financial Calculator

[![Platform](https://img.shields.io/badge/Platform-Android%20%28API%2024%2B%29-green)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Language](https://img.shields.io/badge/Language-Kotlin%20100%25-blue)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material3-purple)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite-lightgrey)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Localization](https://img.shields.io/badge/Localization-Kurdish%20%28Sorani%20RTL%29%20%7C%20English-orange)](file:///c:/Develop/Software%20Project/App%20Calculator/android)

An enterprise-grade, native Android financial modeling and unit-economics calculation suite built with 100% pure **Kotlin** and **Jetpack Compose**. Featuring a custom tactile **Neumorphic Design System**, dynamic OS dark mode synchronization, interactive Compose Canvas charts, local Room SQLite project persistence, and full bilingual (Kurdish Sorani RTL & English LTR) support.

---

## 📱 Application Overview

The application empowers e-commerce founders, financial analysts, and venture investors to compute unit economics, model expected-value (EV) operational risks, and calculate cash float requirements under real-world logistics constraints.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MAIN ACTIVITY & THEME HOST                         │
├───────────────────────────────────┬─────────────────────────────────────────┤
│         Dashboard Screen          │             Analysis Screen             │
│  • Bi-directional Sliders (19)    │  • Custom Compose Valuation Line Chart   │
│  • Instant Summary Metrics (13)   │  • Concave Revenue Growth Bar Chart     │
│  • Timeframe Selectors (D/W/M/T)  │  • Operational Risk Breakdowns          │
├───────────────────────────────────┴─────────────────────────────────────────┤
│                   ROOM SQLITE PERSISTENCE & DICTIONARY                      │
│  • Scenario Save/Load Modals      • 30-Term Kurdish/English Dictionary      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Technical Highlights

- **100% Jetpack Compose UI**: Pure declarative UI without legacy XML layouts, leveraging Material 3 and custom drawing modifiers.
- **Tactile Neumorphic Modifier Engine**: Custom Canvas drawing modifiers (`neuFlat`, `neuPressed`, `neuConvex`, `neuConcave`) creating extruded surfaces, inner bevels, and multi-directional soft dual shadows (`#E0E5EC` Light / `#171C21` Dark).
- **Pure Kotlin Math Engine**: Isolated, highly testable `CalculatorLogic.kt` model computing 19 inputs into 13 real-time risk outputs, cash burn rates, and COD float requirements.
- **Custom Compose Canvas Charts**:
  - `ValuationLineChart.kt`: Smooth cubic Bezier interpolation rendering 5-point valuation trajectories.
  - `RevenueGrowthBarChart.kt`: Height-scaled concave bar chart with interactive highlight states.
- **Room SQLite Scenario Database**: Local offline persistence (`ProjectSQLiteDatabase.kt`) allowing users to save, load, update, and audit financial modeling scenarios.
- **Bilingual RTL / LTR Architecture**: Native Kurdish Sorani (`ku`, RTL) and English (`en`, LTR) dictionary modal with dynamic layout direction toggling (`LayoutDirection.Rtl` vs. `LayoutDirection.Ltr`).

---

## 🛠 Project Structure & Package Layout

```
android/
├── app/
│   ├── build.gradle.kts                # Module build script (dependencies, SDK config)
│   └── src/
│       ├── main/
│       │   ├── java/com/uniteconomics/calculator/
│       │   │   ├── data/
│       │   │   │   └── CalculatorLogic.kt          # Math calculation engine & data models
│       │   │   ├── db/
│       │   │   │   └── ProjectSQLiteDatabase.kt    # Room SQLite database & DAO specs
│       │   │   ├── ui/
│       │   │   │   ├── components/                 # Neumorphic sliders, top bar, bottom nav, modals
│       │   │   │   ├── charts/                     # Custom Compose Canvas line & bar charts
│       │   │   │   ├── screens/                    # Dashboard, Analysis, Projects & Calculator screens
│       │   │   │   └── theme/                      # Neumorphic modifiers, colors & theme resolver
│       │   │   └── MainActivity.kt                 # Main Activity & root navigation state
│       │   ├── res/                                # Android app icons, splash & XML resources
│       │   └── AndroidManifest.xml                 # App manifest & permissions
│       └── test/java/com/uniteconomics/calculator/
│           ├── CalculatorLogicTest.kt              # Unit tests for financial calculations
│           ├── ChartMathTest.kt                    # Math assertions for chart scaling
│           ├── Milestone2ChallengerTest.kt         # Theme & modifier assertions
│           ├── Milestone3ChallengerTest.kt         # UI state & navigation assertions
│           ├── ProjectEntityTest.kt                # Database entity serialization tests
│           ├── RtlLayoutTest.kt                    # Sorani Kurdish RTL layout assertions
│           └── ThemeResolverTest.kt                # Dark mode resolver unit tests
├── build.gradle.kts                            # Root build configuration script
├── gradle.properties                           # JVM args & AndroidX properties
└── settings.gradle.kts                         # Included modules & plugin repositories
```

---

## ⚙️ Requirements & Technical Specifications

| Parameter | Specification |
|-----------|---------------|
| **Package Name** | `com.uniteconomics.calculator` |
| **Minimum SDK** | API Level 24 (Android 7.0 Nougat) |
| **Target / Compile SDK** | API Level 37 |
| **JDK Compatibility** | Java 17 / Kotlin JVM 17 |
| **Compose Compiler** | Jetpack Compose BOM 2024.x+ |
| **Database** | Room SQLite 2.6.x+ |

---

## 🧮 Mathematical Engine (Inputs & Outputs)

### Core Inputs (19 Parameters)
- **Unit Costs & Pricing**: Retail Price, Sourcing Cost, AOV Multiplier, Discount %
- **Marketing & Acquisition**: CAC (Customer Acquisition Cost), Platform Ad Fee %, LTV Multiplier
- **Logistics & Ops**: Shipping Cost, Operational Cost per Unit, RTS Rejection Rate %, Return Shipping Fee
- **Risk & Losses**: Refund Rate %, Refund Penalty Fee, Product Damage Rate %, Deadstock Loss Rate %
- **Overhead & Targets**: Fixed Monthly Overhead, Target Profit Goal, Project Duration (Days), Remittance Frequency (Days)

### Computed Outputs (13 Risk & Valuation Metrics)
- `netProfitPerProduct`: Net margin per delivered item after all risk deductions.
- `netProfitTotal`: Projected total net profit across project duration.
- `revenue`: Total gross revenue generated from fulfilled orders.
- `dailyTargetOrders`: Daily required dispatch volume to achieve profit goal.
- `dailyCashBurn`: Total daily operational cash burn rate.
- `targetSuccessfulOrders`: Required successful deliveries to reach profit target.
- `grossProfitPerOrder`: Order-level EV gross margin before fixed overhead.
- `breakEvenOrders`: Minimum successful orders required to cover fixed costs.
- `totalDispatchedOrders`: Total orders shipped accounting for RTS rejects and refunds.
- `totalProjectExpenses`: Total capital outlay across duration.
- `workingCapitalFloat`: Required cash buffer during COD remittance delay windows.
- `burnRates`: Prorated Daily (1d), Weekly (7d), and Monthly (30d) cash burn rates.
- `isLoss`: Loss safeguard flag rendering visual warning state when profit <= 0.

---

## ⚡ Building & Running the Application

### Command Line Execution

```bash
# Navigate to the android module directory
cd android

# 1. Compile and build Debug APK
./gradlew assembleDebug

# 2. Run unit tests
./gradlew test

# 3. Clean build artifacts
./gradlew clean

# 4. Install onto connected Android device or emulator
./gradlew installDebug
```

### Android Studio Setup
1. Open Android Studio (Jellyfish / Koala or newer).
2. Select **Open** and target the `android/` directory.
3. Allow Gradle sync to complete automatically.
4. Select the `app` run configuration and target an Android 7.0+ (API 24+) virtual or physical device.
5. Click **Run (`Shift + F10`)**.

---

## 🧪 Testing Suite

Run all unit and state tests using Gradle:

```bash
./gradlew test --info
```

The test suite covers:
- **`CalculatorLogicTest`**: Verifies unit economics formulas, loss state detection, and timeframe conversions.
- **`ChartMathTest`**: Validates point scaling and dynamic bounds math for Compose Canvas rendering.
- **`RtlLayoutTest`**: Asserts layout direction behavior and Kurdish Sorani text rendering logic.
- **`ThemeResolverTest`**: Asserts OS dark mode detection and Neumorphic color palette resolution.
- **`ProjectEntityTest`**: Tests Room entity mapping and serialization integrity.

---

## 📄 License & Attribution

Designed and developed as part of the Tactile Neumorphic Application Suite.  
All rights reserved © 2026.
