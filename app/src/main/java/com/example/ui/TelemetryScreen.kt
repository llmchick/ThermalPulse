package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PollingInterval
import com.example.data.PrimaryChannel
import com.example.data.TelemetryUiState
import com.example.data.TemperatureUnit
import com.example.data.ThemeMode
import com.example.ui.components.BatteryElectrochemistrySection
import com.example.ui.components.CpuCoresTableSection
import com.example.ui.components.HeroPrimaryReadoutSection
import com.example.ui.components.OscilloscopeSection
import com.example.ui.components.ThermalZonesTableSection
import com.example.ui.components.ThreeChannelMatrixStrip
import com.example.ui.theme.LocalTelemetryPalette

@Composable
fun TelemetryScreen(
  uiState: TelemetryUiState,
  onSelectChannel: (PrimaryChannel) -> Unit,
  onSetThemeMode: (ThemeMode) -> Unit,
  onCycleTemperatureUnit: () -> Unit,
  onSetPollingInterval: (PollingInterval) -> Unit,
  onTogglePause: () -> Unit,
  onResetPeaks: () -> Unit,
  onInspectHistoryIndex: (Int?) -> Unit,
  onToggleStressTest: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val focusedStats = uiState.statsFor(uiState.selectedChannel)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = palette.background,
    contentColor = palette.textPrimary,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      InstrumentHeaderBar(
        deviceSocModel = uiState.deviceSocModel,
        isPaused = uiState.isPaused,
        isStressTestRunning = uiState.isStressTestRunning,
        temperatureUnit = uiState.temperatureUnit,
        themeMode = uiState.themeMode,
        onCycleUnit = onCycleTemperatureUnit,
        onSelectTheme = onSetThemeMode,
      )
    },
    bottomBar = {
      InstrumentControlStrip(
        pollingInterval = uiState.pollingInterval,
        isPaused = uiState.isPaused,
        isStressTestRunning = uiState.isStressTestRunning,
        onSelectInterval = onSetPollingInterval,
        onTogglePause = onTogglePause,
        onResetPeaks = onResetPeaks,
        onToggleStressTest = onToggleStressTest,
      )
    },
  ) { innerPadding ->
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .background(palette.background)
    ) {
      val isExpandedLayout = maxWidth >= 720.dp

      if (isExpandedLayout) {
        // Canonical 2-Column Split Layout for Tablets / Foldables / Landscape
        Row(modifier = Modifier.fillMaxSize()) {
          Column(
            modifier =
              Modifier.weight(1.08f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
          ) {
            HeroPrimaryReadoutSection(
              stats = focusedStats,
              temperatureUnit = uiState.temperatureUnit,
              systemThermalStatusRu = uiState.systemThermalStatusRu,
            )

            ThreeChannelMatrixStrip(
              hotspotStats = uiState.hotspotStats,
              cpuStats = uiState.cpuStats,
              batteryStats = uiState.batteryStats,
              selectedChannel = uiState.selectedChannel,
              temperatureUnit = uiState.temperatureUnit,
              onSelectChannel = onSelectChannel,
            )

            OscilloscopeSection(
              history = uiState.history,
              selectedChannel = uiState.selectedChannel,
              temperatureUnit = uiState.temperatureUnit,
              inspectedIndex = uiState.inspectedHistoryIndex,
              onInspectIndexChange = onInspectHistoryIndex,
            )
          }

          VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = palette.hairline,
          )

          Column(
            modifier =
              Modifier.weight(1.0f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
          ) {
            CpuCoresTableSection(
              cores = uiState.cores,
              temperatureUnit = uiState.temperatureUnit,
            )

            BatteryElectrochemistrySection(
              battery = uiState.batteryDetails,
              temperatureUnit = uiState.temperatureUnit,
            )

            ThermalZonesTableSection(
              zones = uiState.thermalZones,
              temperatureUnit = uiState.temperatureUnit,
            )

            Spacer(modifier = Modifier.height(24.dp))
          }
        }
      } else {
        // Handheld Vertical Swiss Grid Layout
        Column(
          modifier =
            Modifier.fillMaxSize()
              .verticalScroll(rememberScrollState())
        ) {
          HeroPrimaryReadoutSection(
            stats = focusedStats,
            temperatureUnit = uiState.temperatureUnit,
            systemThermalStatusRu = uiState.systemThermalStatusRu,
          )

          ThreeChannelMatrixStrip(
            hotspotStats = uiState.hotspotStats,
            cpuStats = uiState.cpuStats,
            batteryStats = uiState.batteryStats,
            selectedChannel = uiState.selectedChannel,
            temperatureUnit = uiState.temperatureUnit,
            onSelectChannel = onSelectChannel,
          )

          OscilloscopeSection(
            history = uiState.history,
            selectedChannel = uiState.selectedChannel,
            temperatureUnit = uiState.temperatureUnit,
            inspectedIndex = uiState.inspectedHistoryIndex,
            onInspectIndexChange = onInspectHistoryIndex,
          )

          CpuCoresTableSection(
            cores = uiState.cores,
            temperatureUnit = uiState.temperatureUnit,
          )

          BatteryElectrochemistrySection(
            battery = uiState.batteryDetails,
            temperatureUnit = uiState.temperatureUnit,
          )

          ThermalZonesTableSection(
            zones = uiState.thermalZones,
            temperatureUnit = uiState.temperatureUnit,
          )

          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun InstrumentHeaderBar(
  deviceSocModel: String,
  isPaused: Boolean,
  isStressTestRunning: Boolean,
  temperatureUnit: TemperatureUnit,
  themeMode: ThemeMode,
  onCycleUnit: () -> Unit,
  onSelectTheme: (ThemeMode) -> Unit,
) {
  val palette = LocalTelemetryPalette.current

  Column(modifier = Modifier.fillMaxWidth().background(palette.background)) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Status Square Indicator (Dieter Rams / Braun instrument dot)
          Box(
            modifier =
              Modifier.size(8.dp)
                .background(
                  when {
                    isPaused -> palette.textMuted
                    isStressTestRunning -> palette.accent
                    else -> palette.nominalOlive
                  }
                )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.header_title),
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = deviceSocModel,
          style = MaterialTheme.typography.labelSmall,
          color = palette.textMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      // Unit Switcher + Theme Mode Switcher
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Unit button (°C / °F / K)
        Box(
          modifier =
            Modifier.minimumInteractiveComponentSize()
              .border(1.dp, palette.hairline)
              .background(palette.surface)
              .clickable(onClick = onCycleUnit)
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("unit_toggle_button"),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = temperatureUnit.symbol,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary,
          )
        }

        // Segmented Theme Selector (Тёмная / Светлая / Авто)
        Row(
          modifier =
            Modifier.border(1.dp, palette.hairline)
              .background(palette.surface)
              .height(IntrinsicSize.Min)
        ) {
          ThemeMode.entries.forEachIndexed { idx, mode ->
            val selected = themeMode == mode
            Box(
              modifier =
                Modifier.minimumInteractiveComponentSize()
                  .background(if (selected) palette.textPrimary else palette.surface)
                  .clickable { onSelectTheme(mode) }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
                  .testTag("theme_mode_${mode.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = mode.labelRu,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) palette.background else palette.textPrimary,
              )
            }
            if (idx < ThemeMode.entries.lastIndex) {
              VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = palette.hairline,
              )
            }
          }
        }
      }
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
  }
}

@Composable
private fun InstrumentControlStrip(
  pollingInterval: PollingInterval,
  isPaused: Boolean,
  isStressTestRunning: Boolean,
  onSelectInterval: (PollingInterval) -> Unit,
  onTogglePause: () -> Unit,
  onResetPeaks: () -> Unit,
  onToggleStressTest: () -> Unit,
) {
  val palette = LocalTelemetryPalette.current

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(palette.surface)
        .navigationBarsPadding()
  ) {
    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    Row(
      modifier =
        Modifier.fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Pause / Resume telemetry button
      TechnicalActionButton(
        text =
          if (isPaused) stringResource(R.string.action_resume)
          else stringResource(R.string.action_pause),
        isActive = isPaused,
        activeColor = palette.warningAmber,
        onClick = onTogglePause,
        modifier = Modifier.testTag("toggle_pause_button"),
      )

      // Reset session peaks button
      TechnicalActionButton(
        text = stringResource(R.string.action_reset_peaks),
        isActive = false,
        activeColor = palette.accent,
        onClick = onResetPeaks,
        modifier = Modifier.testTag("reset_peaks_button"),
      )

      // Multi-core Stress Test button to watch real-time Hotspot & CPU temperature rise
      TechnicalActionButton(
        text =
          if (isStressTestRunning) stringResource(R.string.action_stress_stop)
          else stringResource(R.string.action_stress_test),
        isActive = isStressTestRunning,
        activeColor = palette.accent,
        onClick = onToggleStressTest,
        modifier = Modifier.testTag("stress_test_button"),
      )

      // Polling interval selector
      Row(
        modifier =
          Modifier.border(1.dp, palette.hairline)
            .background(palette.background)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PollingInterval.entries.forEachIndexed { idx, interval ->
          val isSelected = pollingInterval == interval
          Box(
            modifier =
              Modifier.minimumInteractiveComponentSize()
                .background(if (isSelected) palette.textPrimary else palette.background)
                .clickable { onSelectInterval(interval) }
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("interval_${interval.delayMs}"),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = interval.labelRu,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) palette.background else palette.textMuted,
            )
          }
          if (idx < PollingInterval.entries.lastIndex) {
            VerticalDivider(
              modifier = Modifier.fillMaxHeight(),
              thickness = 1.dp,
              color = palette.hairline,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TechnicalActionButton(
  text: String,
  isActive: Boolean,
  activeColor: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val borderColor = if (isActive) activeColor else palette.hairline
  val bgColor = if (isActive) activeColor else palette.background
  val contentColor = if (isActive) androidx.compose.ui.graphics.Color.White else palette.textPrimary

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .border(1.dp, borderColor)
        .background(bgColor)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = contentColor,
    )
  }
}
