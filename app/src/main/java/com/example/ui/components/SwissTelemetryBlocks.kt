package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BatteryTelemetry
import com.example.data.ChannelStats
import com.example.data.CpuCoreStatus
import com.example.data.PrimaryChannel
import com.example.data.TemperatureUnit
import com.example.data.ThermalSeverity
import com.example.data.ThermalZoneReading
import com.example.ui.theme.LocalTelemetryPalette

/**
 * Dominant visual statement ("Spend Your Boldness in One Place"):
 * Extreme typographic scale (68.sp tabular JetBrains Mono) paired with a precision linear
 * thermal ruler and Swiss tabular peak/min/delta summary.
 */
@Composable
fun HeroPrimaryReadoutSection(
  stats: ChannelStats,
  temperatureUnit: TemperatureUnit,
  systemThermalStatusRu: String,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val severityColor by
    animateColorAsState(
      targetValue = severityAccentColor(stats.severity, palette),
      label = "severity_color",
    )

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp)
        .testTag("hero_primary_readout")
  ) {
    // Channel Title + Code + Sensor source
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stats.channel.titleRu,
          style = MaterialTheme.typography.headlineMedium,
          color = palette.textPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Источник: ${stats.sensorSource}",
          style = MaterialTheme.typography.bodyMedium,
          color = palette.textMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Box(
        modifier =
          Modifier.border(1.dp, severityColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = stats.severity.labelRu,
          style = MaterialTheme.typography.labelMedium,
          color = severityColor,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Extreme scale typographic number + side tabular Min/Max/Avg/Delta column
    Row(
      modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.weight(1.25f),
      ) {
        Text(
          text = temperatureUnit.formatValue(stats.currentCelsius, decimals = 1),
          style = MaterialTheme.typography.displayLarge,
          color =
            if (stats.channel == PrimaryChannel.HOTSPOT) palette.accent
            else palette.textPrimary,
          modifier = Modifier.testTag("hero_temperature_value"),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.padding(top = 8.dp)) {
          Text(
            text = temperatureUnit.symbol,
            style = MaterialTheme.typography.displaySmall,
            color = palette.textMuted,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stats.channel.codeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
          )
        }
      }

      VerticalDivider(
        modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
        thickness = 1.dp,
        color = palette.hairline,
      )

      // Tabular Min / Avg / Peak / Rate-of-change block
      Column(
        modifier = Modifier.weight(0.95f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        TabularMetricRow(
          label = stringResource(R.string.metric_max),
          value = temperatureUnit.formatWithUnit(stats.maxCelsius, 1),
          highlight = true,
        )
        TabularMetricRow(
          label = stringResource(R.string.metric_avg),
          value = temperatureUnit.formatWithUnit(stats.avgCelsius, 1),
          highlight = false,
        )
        TabularMetricRow(
          label = stringResource(R.string.metric_min),
          value = temperatureUnit.formatWithUnit(stats.minCelsius, 1),
          highlight = false,
        )
        TabularMetricRow(
          label = stringResource(R.string.metric_delta),
          value = "${temperatureUnit.formatDeltaFromCelsius(stats.deltaCelsiusPerMin)}/мин",
          highlight = false,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Precision Linear Thermal Scale (20°C .. 100°C with throttling threshold tick at 80°C)
    PrecisionThermalScaleBar(
      currentCelsius = stats.currentCelsius,
      minCelsius = stats.minCelsius,
      maxCelsius = stats.maxCelsius,
      thresholdCelsius = if (stats.channel == PrimaryChannel.BATTERY) 45f else 80f,
      temperatureUnit = temperatureUnit,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stats.severity.thresholdHintRu,
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textMuted,
      )
      Text(
        text = systemThermalStatusRu,
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
    }
  }
}

@Composable
private fun TabularMetricRow(
  label: String,
  value: String,
  highlight: Boolean,
) {
  val palette = LocalTelemetryPalette.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = palette.textMuted,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
      color = if (highlight) palette.accent else palette.textPrimary,
    )
  }
}

@Composable
private fun PrecisionThermalScaleBar(
  currentCelsius: Float,
  minCelsius: Float,
  maxCelsius: Float,
  thresholdCelsius: Float,
  temperatureUnit: TemperatureUnit,
) {
  val palette = LocalTelemetryPalette.current
  val scaleMinC = 20f
  val scaleMaxC = 100f
  val span = scaleMaxC - scaleMinC

  val animatedCurrentFrac by
    animateFloatAsState(
      targetValue = ((currentCelsius - scaleMinC) / span).coerceIn(0f, 1f),
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "scale_pos",
    )
  val minFrac = ((minCelsius - scaleMinC) / span).coerceIn(0f, 1f)
  val maxFrac = ((maxCelsius - scaleMinC) / span).coerceIn(0f, 1f)
  val thresholdFrac = ((thresholdCelsius - scaleMinC) / span).coerceIn(0f, 1f)

  Column(modifier = Modifier.fillMaxWidth()) {
    Canvas(modifier = Modifier.fillMaxWidth().height(22.dp)) {
      val w = size.width
      val h = size.height
      val trackY = h * 0.55f

      // Baseline track
      drawLine(
        color = palette.hairline,
        start = Offset(0f, trackY),
        end = Offset(w, trackY),
        strokeWidth = 2.dp.toPx(),
      )

      // Session Min..Max range bar
      val startX = w * minFrac
      val endX = (w * maxFrac).coerceAtLeast(startX + 2.dp.toPx())
      drawRect(
        color = palette.textMuted.copy(alpha = 0.38f),
        topLeft = Offset(startX, trackY - 3.dp.toPx()),
        size = Size(endX - startX, 6.dp.toPx()),
      )

      // Active fill from 0 to current
      drawLine(
        color = palette.textPrimary,
        start = Offset(0f, trackY),
        end = Offset(w * animatedCurrentFrac, trackY),
        strokeWidth = 3.dp.toPx(),
      )

      // Ruler graduation ticks every 10°C
      for (step in 0..8) {
        val x = w * (step / 8f)
        val tickH = if (step % 2 == 0) 8.dp.toPx() else 4.dp.toPx()
        drawLine(
          color = palette.hairline,
          start = Offset(x, trackY - tickH / 2f),
          end = Offset(x, trackY + tickH / 2f),
          strokeWidth = 1.dp.toPx(),
        )
      }

      // Throttling threshold tick mark
      val threshX = w * thresholdFrac
      drawLine(
        color = palette.accent,
        start = Offset(threshX, 0f),
        end = Offset(threshX, h),
        strokeWidth = 1.5.dp.toPx(),
      )

      // Current value needle
      val curX = w * animatedCurrentFrac
      drawRect(
        color = palette.accent,
        topLeft = Offset((curX - 3.dp.toPx()).coerceIn(0f, w - 6.dp.toPx()), trackY - 8.dp.toPx()),
        size = Size(6.dp.toPx(), 16.dp.toPx()),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = temperatureUnit.formatWithUnit(scaleMinC, 0),
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
      Text(
        text = "Порог: ${temperatureUnit.formatWithUnit(thresholdCelsius, 0)}",
        style = MaterialTheme.typography.labelSmall,
        color = palette.accent,
      )
      Text(
        text = temperatureUnit.formatWithUnit(scaleMaxC, 0),
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
    }
  }
}

/**
 * 3-Column Swiss Comparative Matrix showing CPU Average, CPU Hotspot, and Battery simultaneously.
 * Separated by 1.dp structural lines (no rounded shadow cards).
 */
@Composable
fun ThreeChannelMatrixStrip(
  hotspotStats: ChannelStats,
  cpuStats: ChannelStats,
  batteryStats: ChannelStats,
  selectedChannel: PrimaryChannel,
  temperatureUnit: TemperatureUnit,
  onSelectChannel: (PrimaryChannel) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current

  Column(modifier = modifier.fillMaxWidth().testTag("three_channel_matrix")) {
    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      ChannelMatrixCell(
        stats = hotspotStats,
        isSelected = selectedChannel == PrimaryChannel.HOTSPOT,
        temperatureUnit = temperatureUnit,
        onClick = { onSelectChannel(PrimaryChannel.HOTSPOT) },
        modifier = Modifier.weight(1f).testTag("channel_cell_hotspot"),
      )

      VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        thickness = 1.dp,
        color = palette.hairline,
      )

      ChannelMatrixCell(
        stats = cpuStats,
        isSelected = selectedChannel == PrimaryChannel.CPU,
        temperatureUnit = temperatureUnit,
        onClick = { onSelectChannel(PrimaryChannel.CPU) },
        modifier = Modifier.weight(1f).testTag("channel_cell_cpu"),
      )

      VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        thickness = 1.dp,
        color = palette.hairline,
      )

      ChannelMatrixCell(
        stats = batteryStats,
        isSelected = selectedChannel == PrimaryChannel.BATTERY,
        temperatureUnit = temperatureUnit,
        onClick = { onSelectChannel(PrimaryChannel.BATTERY) },
        modifier = Modifier.weight(1f).testTag("channel_cell_battery"),
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
  }
}

@Composable
private fun ChannelMatrixCell(
  stats: ChannelStats,
  isSelected: Boolean,
  temperatureUnit: TemperatureUnit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val cellBg = if (isSelected) palette.surface else palette.background

  Column(
    modifier =
      modifier
        .background(cellBg)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 12.dp)
  ) {
    // Top structural selection bar
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(3.dp)
          .background(if (isSelected) palette.accent else Color.Transparent)
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stats.channel.shortTitleRu,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) palette.textPrimary else palette.textMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = temperatureUnit.formatWithUnit(stats.currentCelsius, 1),
      style = MaterialTheme.typography.displaySmall,
      fontWeight = FontWeight.Bold,
      color =
        when {
          stats.channel == PrimaryChannel.HOTSPOT && isSelected -> palette.accent
          isSelected -> palette.textPrimary
          else -> palette.textPrimary.copy(alpha = 0.88f)
        },
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Пик ${temperatureUnit.formatWithUnit(stats.maxCelsius, 1)}",
      style = MaterialTheme.typography.labelSmall,
      color = palette.textMuted,
    )
  }
}

/**
 * Architectural CPU Core Frequency & Junction Temperature Table.
 */
@Composable
fun CpuCoresTableSection(
  cores: List<CpuCoreStatus>,
  temperatureUnit: TemperatureUnit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current

  Column(modifier = modifier.fillMaxWidth().testTag("cpu_cores_section")) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = stringResource(R.string.section_cores),
        style = MaterialTheme.typography.titleLarge,
        color = palette.textPrimary,
      )
      Text(
        text = "${cores.size} вычислительных ядер",
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    // Table column headers
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(palette.surface)
          .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Ядро / Кластер",
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textMuted,
        modifier = Modifier.weight(1.25f),
      )
      Text(
        text = "Частота",
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textMuted,
        modifier = Modifier.weight(1.0f),
      )
      Text(
        text = "Загрузка",
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textMuted,
        modifier = Modifier.weight(1.1f),
      )
      Text(
        text = "Узел",
        style = MaterialTheme.typography.bodyMedium,
        color = palette.textMuted,
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    cores.forEach { core ->
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(
              if (core.isHotspotCore) palette.surface.copy(alpha = 0.65f) else palette.background
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Core ID + Hotspot indicator dot
        Row(
          modifier = Modifier.weight(1.25f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(6.dp)
                .background(if (core.isHotspotCore) palette.accent else palette.hairline)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text =
                if (core.isHotspotCore) "Ядро ${core.coreIndex} • Hotspot"
                else "Ядро ${core.coreIndex}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (core.isHotspotCore) FontWeight.Bold else FontWeight.Medium,
              color = if (core.isHotspotCore) palette.accent else palette.textPrimary,
            )
            Text(
              text = core.clusterName,
              style = MaterialTheme.typography.labelSmall,
              color = palette.textMuted,
            )
          }
        }

        // Frequency
        Column(modifier = Modifier.weight(1.0f)) {
          Text(
            text = "${core.currentFreqMhz} МГц",
            style = MaterialTheme.typography.labelMedium,
            color = palette.textPrimary,
          )
          Text(
            text = "макс ${core.maxFreqMhz}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
          )
        }

        // Linear precision load bar + %
        Row(
          modifier = Modifier.weight(1.1f).padding(end = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.weight(1f)
                .height(6.dp)
                .background(palette.hairline)
          ) {
            Box(
              modifier =
                Modifier.fillMaxHeight()
                  .fillMaxWidth(core.loadFraction.coerceIn(0.05f, 1f))
                  .background(if (core.isHotspotCore) palette.accent else palette.textPrimary)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${(core.loadFraction * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            modifier = Modifier.width(32.dp),
          )
        }

        // Core Junction Temp
        Text(
          text = temperatureUnit.formatWithUnit(core.estimatedTempCelsius, 1),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = if (core.isHotspotCore) FontWeight.Bold else FontWeight.Medium,
          color = if (core.isHotspotCore) palette.accent else palette.textPrimary,
        )
      }
      HorizontalDivider(thickness = 1.dp, color = palette.hairline)
    }
  }
}

/**
 * Battery Electrochemistry & Power Supply Swiss Grid.
 */
@Composable
fun BatteryElectrochemistrySection(
  battery: BatteryTelemetry,
  temperatureUnit: TemperatureUnit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current

  Column(modifier = modifier.fillMaxWidth().testTag("battery_details_section")) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = stringResource(R.string.section_battery_details),
        style = MaterialTheme.typography.titleLarge,
        color = palette.textPrimary,
      )
      Text(
        text = "${battery.technology} • ${battery.healthRu}",
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    // Row 1: Temperature | Voltage | Current & Power
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      BatteryGridCell(
        label = "Термистор АКБ",
        value = temperatureUnit.formatWithUnit(battery.tempCelsius, 1),
        sublabel = battery.healthRu,
        modifier = Modifier.weight(1f),
      )
      VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = palette.hairline)
      BatteryGridCell(
        label = stringResource(R.string.battery_voltage),
        value = "${battery.voltageMv} мВ",
        sublabel = String.format(java.util.Locale.US, "%.2f В ном.", battery.voltageMv / 1000f),
        modifier = Modifier.weight(1f),
      )
      VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = palette.hairline)
      BatteryGridCell(
        label = stringResource(R.string.battery_current),
        value = "${battery.currentMa} мА",
        sublabel = String.format(java.util.Locale.US, "Мощность %.2f Вт", battery.powerWatts),
        modifier = Modifier.weight(1f),
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    // Row 2: Charge Level | Power Source | Chemistry
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      BatteryGridCell(
        label = stringResource(R.string.battery_level),
        value = "${battery.levelPercent}%",
        sublabel = battery.statusRu,
        modifier = Modifier.weight(1f),
      )
      VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = palette.hairline)
      BatteryGridCell(
        label = stringResource(R.string.battery_status),
        value = battery.powerSourceRu,
        sublabel = if (battery.isCharging) "Внешний контур" else "Отдача тока",
        modifier = Modifier.weight(1f),
      )
      VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = palette.hairline)
      BatteryGridCell(
        label = stringResource(R.string.battery_tech),
        value = battery.technology,
        sublabel = "Датчик BatteryManager",
        modifier = Modifier.weight(1f),
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
  }
}

@Composable
private fun BatteryGridCell(
  label: String,
  value: String,
  sublabel: String,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = palette.textMuted,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge,
      fontFamily = com.example.ui.theme.JetBrainsMonoFamily,
      fontWeight = FontWeight.Bold,
      color = palette.textPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = sublabel,
      style = MaterialTheme.typography.labelSmall,
      color = palette.textMuted,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Kernel Thermal Zones (/sys/class/thermal) breakdown table.
 */
@Composable
fun ThermalZonesTableSection(
  zones: List<ThermalZoneReading>,
  temperatureUnit: TemperatureUnit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current

  Column(modifier = modifier.fillMaxWidth().testTag("thermal_zones_section")) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      Text(
        text = stringResource(R.string.section_thermal_zones),
        style = MaterialTheme.typography.titleLarge,
        color = palette.textPrimary,
      )
      Text(
        text = "/sys/class/thermal",
        style = MaterialTheme.typography.labelSmall,
        color = palette.textMuted,
      )
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)

    zones.forEach { zone ->
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .background(if (zone.isPrimaryHotspot) palette.surface else palette.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = zone.id,
              style = MaterialTheme.typography.labelSmall,
              color = if (zone.isPrimaryHotspot) palette.accent else palette.textMuted,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = zone.sensorType,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = palette.textPrimary,
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${zone.subsystemRu} • ${zone.kernelPath}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Text(
          text = temperatureUnit.formatWithUnit(zone.tempCelsius, 1),
          style = MaterialTheme.typography.titleLarge,
          fontFamily = com.example.ui.theme.JetBrainsMonoFamily,
          fontWeight = FontWeight.Bold,
          color = if (zone.isPrimaryHotspot) palette.accent else palette.textPrimary,
        )
      }
      HorizontalDivider(thickness = 1.dp, color = palette.hairline)
    }
  }
}

private fun severityAccentColor(
  severity: ThermalSeverity,
  palette: com.example.ui.theme.TelemetryPalette,
): Color =
  when (severity) {
    ThermalSeverity.NOMINAL -> palette.nominalOlive
    ThermalSeverity.ELEVATED -> palette.warningAmber
    ThermalSeverity.HIGH -> palette.accent
    ThermalSeverity.CRITICAL -> palette.accent
  }
