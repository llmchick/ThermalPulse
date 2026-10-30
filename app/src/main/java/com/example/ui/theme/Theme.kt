package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val StrictDarkColorScheme =
  darkColorScheme(
    primary = DarkSignalAccent,
    onPrimary = Color(0xFFFFFFFF),
    secondary = DarkTextPrimary,
    onSecondary = DarkBackground,
    tertiary = TraceBatteryDark,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkGridHairline,
    outlineVariant = DarkGridHairline,
  )

private val StrictLightColorScheme =
  lightColorScheme(
    primary = LightSignalAccent,
    onPrimary = Color(0xFFFFFFFF),
    secondary = LightTextPrimary,
    onSecondary = LightBackground,
    tertiary = TraceBatteryLight,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextMuted,
    outline = LightGridHairline,
    outlineVariant = LightGridHairline,
  )

// Technical architectural shapes: crisp 0dp - 2dp corners instead of generic 16dp "card soup"
val TechnicalShapes =
  Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(4.dp),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) StrictDarkColorScheme else StrictLightColorScheme
  val palette = if (darkTheme) DarkTelemetryPalette else LightTelemetryPalette

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  CompositionLocalProvider(LocalTelemetryPalette provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      shapes = TechnicalShapes,
      content = content,
    )
  }
}
