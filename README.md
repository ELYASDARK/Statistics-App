# 🤖 Native Android Tactile Neumorphic Financial Calculator

[![Platform](https://img.shields.io/badge/Platform-Android%20%28API%2024%2B%29-green)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Language](https://img.shields.io/badge/Language-Kotlin%20100%25-blue)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material3-purple)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Database](https://img.shields.io/badge/Database-SQLite%20Native-lightgrey)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Security](https://img.shields.io/badge/Security-OWASP%20Mobile%20Hardened-red)](file:///c:/Develop/Software%20Project/App%20Calculator/android)
[![Localization](https://img.shields.io/badge/Localization-Kurdish%20%28Sorani%20RTL%29%20%7C%20English-orange)](file:///c:/Develop/Software%20Project/App%20Calculator/android)

An enterprise-grade, native Android financial modeling and unit-economics calculation suite built with 100% pure **Kotlin** and **Jetpack Compose**. Featuring a custom tactile **Neumorphic Design System**, dynamic OS dark mode synchronization, interactive Compose Canvas charts, offline SQLite project persistence, full bilingual (Kurdish Sorani RTL & English LTR) support, and comprehensive OWASP Mobile security hardening.

---

## 📱 Application Overview

The application empowers e-commerce founders, financial analysts, and venture investors to compute unit economics, model expected-value (EV) operational risks, and calculate cash float requirements under real-world logistics constraints.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MAIN ACTIVITY & THEME HOST                         │
│  • FLAG_SECURE & Obscured Touch Filtering • Strict R8 Optimization          │
├───────────────────────────────────┬─────────────────────────────────────────┤
│         Dashboard Screen          │             Analysis Screen             │
│  • Bi-directional Sliders (19)    │  • Custom Compose Valuation Line Chart  │
│  • Instant Summary Metrics (13)   │  • Concave Revenue Growth Bar Chart     │
│  • Timeframe Selectors (D/W/M/T)  │  • Operational Risk Breakdowns          │
├───────────────────────────────────┴─────────────────────────────────────────┤
│                  SQLITE PERSISTENCE & DICTIONARY GLOSSARY                   │
│  • Mutex-Guarded Disk Queue       • 30-Term Kurdish/English Glossary        │
│  • Scenario Save/Clone/Search     • Directional BiDi & Trojan Source Shield │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Technical & Security Highlights

- **100% Jetpack Compose UI**: Pure declarative UI without legacy XML layouts, leveraging Material 3 and custom drawing modifiers.
- **Tactile Neumorphic Modifier Engine**: Custom Canvas drawing modifiers (`neuFlat`, `neuPressed`, `neuConvex`, `neuConcave`) creating extruded surfaces, inner bevels, and multi-directional soft dual shadows (`#E0E5EC` Light / `#171C21` Dark).
- **Hardened Platform Security**:
  - `setHideOverlayWindows(true)` and `filterTouchesWhenObscured = true` for anti-tapjacking protection.
  - `allowBackup="false"` and `<exclude>` backup rules preventing cloud & ADB data extraction.
  - `singleTask` and empty `taskAffinity` blocking StrandHogg task hijacking.
- **Pure Kotlin Math Engine**: Isolated, highly testable `CalculatorLogic.kt` model with `CEIL_EPSILON = 1e-9` floating-point tolerance and strict loss-state invariant enforcement.
- **Thread-Safe Localization & BiDi Defense**:
  - `KurdishUtils.kt` filtering Unicode Trojan Source controls (`\u202A`–`\u202E`, `\u2066`–`\u2069`), zero-width glyphs, and Arabic diacritics.
  - `formatBidiCurrency` with directional isolates for robust RTL number/currency rendering.
  - Arabic comma (`،` `\u060C`) thousands separator support.
- **Native SQLite Persistence Engine**: Local offline persistence (`ProjectSQLiteDatabase.kt`) with explicit 28-column projections, coroutine `Mutex` serialization, support for saving new products independently or updating active scenarios, active project badges, and an Android lifecycle observer (`ON_PAUSE`) for instant state flushing.

---

## 🛠 Project Structure & Package Layout

```
android/
├── app/
│   ├── build.gradle.kts                # Module build script (dependencies, SDK config, signing)
│   ├── proguard-rules.pro              # Production R8 ProGuard log stripping & class repackaging
│   └── src/
│       └── main/
│           ├── java/com/uniteconomics/calculator/
│           │   ├── CalculatorLogic.kt          # Math calculation engine & data models
│           │   ├── db/
│           │   │   └── ProjectSQLiteDatabase.kt # SQLite database with explicit column projections
│           │   ├── KurdishUtils.kt             # Kurdish Sorani normalizer & BiDi sanitizer
│           │   ├── SliderInputRow.kt           # Thread-safe slider & bounded numeric input row
│           │   ├── NeumorphicModifiers.kt      # Neumorphic tactile Canvas drawing modifiers
│           │   ├── ValuationLineChart.kt       # Custom Compose Canvas line chart
│           │   ├── RevenueGrowthBarChart.kt    # Custom Compose Canvas bar chart
│           │   ├── DashboardScreen.kt          # Primary parameter inputs & metrics dashboard
│           │   ├── AnalysisScreen.kt           # Financial deep-dive analytics layout
│           │   ├── ProjectsScreen.kt           # Scenario list, search, clone & restore interface
│           │   ├── SaveProjectModal.kt         # Sanitized project save & update dialog
│           │   ├── DictionaryDialog.kt         # 30-term Kurdish/English financial glossary
│           │   ├── CalculatorScreen.kt         # Root coordinator, tab nav & Mutex persistence
│           │   └── MainActivity.kt             # Hardened entry point (FLAG_SECURE, anti-tapjacking)
│           ├── res/                            # Android app icons, splash & XML resources
│           │   └── xml/
│           │       ├── backup_rules.xml        # Locked down backup exclusions
│           │       └── data_extraction_rules.xml # Locked down cloud/device transfer rules
│           └── AndroidManifest.xml             # Hardened app manifest (allowBackup=false, singleTask)
├── build.gradle.kts                            # Root build configuration script
├── gradle.properties                           # Strict R8 full mode & JVM properties
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
| **Compose Compiler** | Jetpack Compose BOM 2026.03.01 |
| **Database** | Native SQLite 3.x with Transaction Isolation |

## 📄 License & Attribution

Designed and developed as part of the Tactile Neumorphic Application Suite.  
All rights reserved © 2026.
