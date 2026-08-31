---
name: Mono-Soft Neumorphic Financial Calculator
description: Tactile Neumorphic mobile design system featuring extruded surfaces, soft dual shadows, and high-density financial metrics.
colors:
  surface: "#E0E5EC"
  surface-dim: "#d5dae1"
  surface-bright: "#f6f9ff"
  surface-container-lowest: "#ffffff"
  surface-container-low: "#eff4fb"
  surface-container: "#e9eef5"
  surface-container-high: "#e4e9f0"
  surface-container-highest: "#dee3ea"
  on-surface: "#171C21"
  on-surface-variant: "#44474c"
  inverse-surface: "#2b3136"
  inverse-on-surface: "#ecf1f8"
  outline: "#75777d"
  outline-variant: "#c5c6cd"
  surface-tint: "#545f72"
  primary: "#2563EB"
  on-primary: "#ffffff"
  primary-container: "#333e50"
  on-primary-container: "#becae0"
  inverse-primary: "#bcc7dd"
  secondary: "#515f74"
  on-secondary: "#ffffff"
  secondary-container: "#d1e1fa"
  on-secondary-container: "#556479"
  tertiary: "#4b3b1f"
  on-tertiary: "#ffffff"
  tertiary-container: "#645234"
  on-tertiary-container: "#dfc6a0"
  error: "#DC2626"
  on-error: "#ffffff"
  error-container: "#ffdad6"
  on-error-container: "#93000a"
  background: "#E0E5EC"
  on-background: "#171C21"
  surface-variant: "#dee3ea"
  shadow-dark: "rgba(163, 177, 198, 0.45)"
  shadow-light: "rgba(255, 255, 255, 0.35)"
  dark-surface: "#171C21"
  dark-card: "#1E232B"
  dark-shadow-dark: "rgba(12, 15, 19, 0.8)"
  dark-shadow-light: "rgba(36, 43, 53, 0.5)"
  accent-text: "#2D3748"
  chart-gradient-start: "#f0f5fd"
  chart-gradient-end: "#caced4"
typography:
  display:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: "700"
    lineHeight: "1.2"
    letterSpacing: "-0.02em"
  headline:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: "700"
    lineHeight: "1.4"
  body:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: "500"
    lineHeight: "1.5"
  label:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: "600"
    lineHeight: "1.2"
    letterSpacing: "0.2em"
rounded:
  sm: 4px
  md: 8px
  lg: 16px
  xl: 24px
  full: 9999px
spacing:
  unit: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 40px
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.lg}"
    padding: "12px 24px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: "16px"
  input-row:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: "12px 16px"
---

# Design System: Mono-Soft Neumorphic Financial Calculator

## Overview

**Creative North Star: "The Tactile Hardware Instrument Cluster"**

This system embodies a sophisticated **Mono-Soft Neumorphic** aesthetic, characterized by soft, extruded surfaces, recessed input wells, and a monochromatic "ton-sur-ton" palette. Built for mobile financial analysts, e-commerce founders, and venture investors, the brand personality is technical yet organic, aiming to eliminate visual fatigue through simulated light physics (directional soft shadows and highlights) rather than harsh structural borders or loud, saturated fills.

The interface feels like high-end physical hardware: responsive sliders, extruded KPI cards, tactile toggle buttons, and smooth Compose Canvas financial curves that convey calm, precision, and futuristic efficiency.

**Key Characteristics:**
- **Tactile Depth Physics**: Surfaces raise (`neuFlat`), press inward (`neuPressed`), convex bulge (`neuConvex`), or concave bevel (`neuConcave`) through multi-directional soft dual shadows.
- **Monochromatic Serenity**: Primary palette centered on mid-gray-blue `#E0E5EC` (Light) and slate dark `#171C21` / `#1E232B` (Dark) to keep dense data readable.
- **Instrument-Cluster Precision**: Crisp Inter typography with wide uppercase tracking for metadata and dense numerical metrics.
- **Dual-Locale Harmony**: Universal layout geometry seamlessly mirroring across English (LTR) and Kurdish Sorani (RTL).

## Colors

The palette relies on mid-tone neutral bases where elevation and interaction are signaled by light simulation rather than heavy color contrast.

### Primary
- **Electric Cobalt Blue** (`#2563EB` / Dark `#60A5FA`): Reserved for active primary actions, chart accent lines, and key positive metric indicators. Used sparingly on ≤10% of any view.

### Secondary
- **Slate Steel Blue** (`#515f74` / Dark `#94A3B8`): Subdued structural tone for secondary actions, navigation container elements, and secondary labels.

### Tertiary
- **Bronze Amber** (`#4b3b1f` / Dark `#FBBF24`): Warning and secondary highlight accent for risk thresholds and caution states.

### Neutral
- **Light Base Canvas** (`#E0E5EC`): The foundational surface color for Light Mode screens and flat extruded containers.
- **Dark Base Canvas** (`#171C21`): Foundational background color for Dark Mode.
- **Dark Container Card** (`#1E232B`): Elevated card surface background in Dark Mode.
- **Charcoal Text Main** (`#171C21` / Dark `#ECF1F8`): Primary high-contrast typography color.
- **Muted Slate Text** (`#718096` / Dark `#94A3B8`): Auxiliary labels, units, and secondary metadata.

### Named Rules
**The One Voice Rule.** Primary cobalt accent is used on ≤10% of any given screen. Elevation and depth, not loud color, dictate structural hierarchy.

## Typography

**Display Font:** Inter  
**Body Font:** Inter  
**Label/Mono Font:** Inter  

**Character:** Utilitarian, crisp, and high-density. Numerical legibility and technical hierarchy take precedence over decorative flair.

### Hierarchy
- **Display** (Bold 700, 30px, line-height 1.2, letter-spacing -0.02em): Large financial KPI totals and header figures.
- **Headline** (Bold 700, 18px, line-height 1.4): Section headers, modal titles, and chart card titles.
- **Body** (Medium 500, 14px, line-height 1.5): Standard parameter descriptions, dictionary term definitions, and input labels.
- **Label** (SemiBold 600, 10px, line-height 1.2, letter-spacing 0.2em, Uppercase): Technical metadata, slider unit tags, timeframe pill selectors, and instrument tags.

### Named Rules
**The Instrument Tracking Rule.** All auxiliary labels and uppercase metadata must use explicit tracking (0.2em) to evoke physical instrument cluster displays.

## Layout

The layout is optimized for mobile touch interaction (container width capped for single-hand reach) with 4px grid rhythm.

- **Grid & Rhythm**: 4px base spacing unit. Component paddings use 8px (`sm`), 16px (`md`), and 24px (`lg`).
- **Section Separation**: Large vertical gaps (40px / 2.5rem) separate major content blocks to prevent overlapping of soft dual shadows.
- **Margins & Touch Safety**: 16px horizontal margins preserve edge safety on mobile viewports.

## Elevation & Depth

Elevation is the primary visual engine of this system. Rather than using stroke borders, depth is created using four custom Neumorphic modifiers:

1. **Flat (Elevated)** (`neuFlat`): Standard cards and tiles. Uses dual soft shadows: light top-left shadow (`rgba(255,255,255,0.35)` / Dark `rgba(36,43,53,0.5)`) and dark bottom-right shadow (`rgba(163,177,198,0.45)` / Dark `rgba(12,15,19,0.8)`).
2. **Pressed (Inverted)** (`neuPressed`): Inset wells for sliders, active toggle states, and text fields. Uses inset stroke masks simulating pressed depth.
3. **Convex (Curved Out)** (`neuConvex`): Primary action buttons and floating badges. Renders a 145° linear gradient from light-top to dark-bottom.
4. **Concave (Curved In)** (`neuConcave`): Interactive bar charts and recessed data containers. Renders a 145° linear gradient from dark-top to light-bottom.

### Named Rules
**The Soft Lighting Rule.** Light source is locked at top-left (-45° angle). All extruded surfaces push down-right shadows and reflect top-left highlights uniformly.

## Shapes

- **Cards**: Large 16px (`lg`) corner radius for smooth, organic tactile extrusion.
- **Sliders & Input Wells**: 8px (`md`) corner radius.
- **Buttons & Floating Nav**: Fully rounded pill shapes (`full` / 9999px) or 16px rounded corners.
- **Chart Bars**: Top-corner rounded (12px) concave bars to maintain physical soft geometry.

## Components

### Buttons
- **Shape**: Fully rounded pill or 16px corner shape.
- **Primary**: `neuConvex` gradient with Cobalt blue or clear charcoal text, transitioning to `neuPressed` on click.
- **Floating Action Button**: Centered floating `neuConvex` circle on bottom nav bar with elevated 8.dp shadow.

### Cards / Containers
- **Corner Style**: 16px corner radius (`neuFlat`).
- **Background**: `#E0E5EC` (Light) / `#1E232B` (Dark).
- **Internal Padding**: 16px to 24px.

### Inputs / Sliders
- **Style**: `neuPressed` inset well containing smooth bi-directional slider track.
- **Thumb**: Extruded `neuFlat` thumb with active value tooltip.

### Navigation
- **Floating Bottom Bar**: Floating `neuFlat` surface with rounded pill shape, harboring 4 item slots and a central elevated action button. Active tab uses `neuPressed` inset highlight.

### Signature Component: Compose Canvas Financial Charts
- **Valuation Line Chart**: Custom Canvas rendering smooth cubic Bezier curves with dual-tone gradient fill (`#f0f5fd` to `#caced4`).
- **Revenue Growth Bar Chart**: Height-scaled concave Neumorphic bars with interactive tap highlights.

## Do's and Don'ts

### Do:
- **Do** maintain a consistent 45° top-left light source for all Neumorphic shadow offsets.
- **Do** allow at least 24px-40px vertical breathing room between adjacent Neumorphic cards so shadows never collide.
- **Do** use `neuPressed` for inset input fields and active toggle states.
- **Do** support both LTR and RTL directionality natively using Jetpack Compose `CompositionLocalProvider`.

### Don't:
- **Don't** use hard 1px black stroke borders around cards or buttons; rely on Neumorphic light physics for separation.
- **Don't** flood surfaces with high-saturation background colors; keep canvas surfaces monochromatic `#E0E5EC` or `#171C21`.
- **Don't** mix conflicting light angles across different components on the same screen.
