package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ThemeMode
import com.example.ui.TelemetryScreen
import com.example.ui.TelemetryViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val telemetryViewModel: TelemetryViewModel = viewModel()
      val uiState by telemetryViewModel.uiState.collectAsStateWithLifecycle()

      val useDarkTheme =
        when (uiState.themeMode) {
          ThemeMode.DARK -> true
          ThemeMode.LIGHT -> false
          ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

      MyApplicationTheme(darkTheme = useDarkTheme) {
        TelemetryScreen(
          uiState = uiState,
          onSelectChannel = telemetryViewModel::selectPrimaryChannel,
          onSetThemeMode = telemetryViewModel::setThemeMode,
          onCycleTemperatureUnit = telemetryViewModel::cycleTemperatureUnit,
          onSetPollingInterval = telemetryViewModel::setPollingInterval,
          onTogglePause = telemetryViewModel::togglePause,
          onResetPeaks = telemetryViewModel::resetPeaks,
          onInspectHistoryIndex = telemetryViewModel::setInspectedHistoryIndex,
          onToggleStressTest = telemetryViewModel::toggleStressTest,
        )
      }
    }
  }
}
