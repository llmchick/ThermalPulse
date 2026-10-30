package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Strict 5-token palette per Anti-AI Design Guide (Technical Utilitarian & Swiss Grid)
// Dark Theme: Deep Graphite + Warm Chalk + International Signal Orange
val DarkBackground = Color(0xFF121316)
val DarkSurface = Color(0xFF1A1C20)
val DarkTextPrimary = Color(0xFFEAE6DF)
val DarkTextMuted = Color(0xFF8A867E)
val DarkGridHairline = Color(0xFF2B2E35)
val DarkSignalAccent = Color(0xFFE84A27)

// Light Theme: Archival Drafting Paper + Carbon Ink + Signal Vermilion
val LightBackground = Color(0xFFF2EFE9)
val LightSurface = Color(0xFFE5E0D5)
val LightTextPrimary = Color(0xFF141413)
val LightTextMuted = Color(0xFF6B675E)
val LightGridHairline = Color(0xFFCBC5B8)
val LightSignalAccent = Color(0xFFD9381E)

// Functional telemetry trace colors (non-neon, precision instrument inks)
val TraceCpuDark = Color(0xFFEAE6DF)
val TraceHotspotDark = Color(0xFFE84A27)
val TraceBatteryDark = Color(0xFFD49B27)

val TraceCpuLight = Color(0xFF141413)
val TraceHotspotLight = Color(0xFFD9381E)
val TraceBatteryLight = Color(0xFF8C6210)

@Immutable
data class TelemetryPalette(
  val background: Color,
  val surface: Color,
  val textPrimary: Color,
  val textMuted: Color,
  val hairline: Color,
  val accent: Color,
  val traceCpu: Color,
  val traceHotspot: Color,
  val traceBattery: Color,
  val warningAmber: Color,
  val nominalOlive: Color,
  val isDark: Boolean,
)

val DarkTelemetryPalette =
  TelemetryPalette(
    background = DarkBackground,
    surface = DarkSurface,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    hairline = DarkGridHairline,
    accent = DarkSignalAccent,
    traceCpu = TraceCpuDark,
    traceHotspot = TraceHotspotDark,
    traceBattery = TraceBatteryDark,
    warningAmber = Color(0xFFD49B27),
    nominalOlive = Color(0xFF7B9A6D),
    isDark = true,
  )

val LightTelemetryPalette =
  TelemetryPalette(
    background = LightBackground,
    surface = LightSurface,
    textPrimary = LightTextPrimary,
    textMuted = LightTextMuted,
    hairline = LightGridHairline,
    accent = LightSignalAccent,
    traceCpu = TraceCpuLight,
    traceHotspot = TraceHotspotLight,
    traceBattery = TraceBatteryLight,
    warningAmber = Color(0xFFA87008),
    nominalOlive = Color(0xFF3E6334),
    isDark = false,
  )

val LocalTelemetryPalette = staticCompositionLocalOf { DarkTelemetryPalette }
