package com.example.data

import java.util.Locale
import kotlin.math.roundToInt

enum class PrimaryChannel(val titleRu: String, val shortTitleRu: String, val codeLabel: String) {
  HOTSPOT("Hotspot кристалла ЦП", "Hotspot ЦП", "T_JMAX"),
  CPU("Температура процессора", "ЦП средн.", "T_PKG"),
  BATTERY("Температура батареи", "Батарея", "T_BAT"),
}

enum class TemperatureUnit(val symbol: String, val labelRu: String) {
  CELSIUS("°C", "Цельсий (°C)"),
  FAHRENHEIT("°F", "Фаренгейт (°F)"),
  KELVIN("K", "Кельвин (K)");

  fun convertFromCelsius(celsius: Float): Float =
    when (this) {
      CELSIUS -> celsius
      FAHRENHEIT -> celsius * 1.8f + 32f
      KELVIN -> celsius + 273.15f
    }

  fun formatValue(celsius: Float, decimals: Int = 1): String {
    val converted = convertFromCelsius(celsius)
    return if (decimals == 0) {
      "${converted.roundToInt()}"
    } else {
      String.format(Locale.US, "%.${decimals}f", converted)
    }
  }

  fun formatWithUnit(celsius: Float, decimals: Int = 1): String {
    return "${formatValue(celsius, decimals)}$symbol"
  }

  fun formatDeltaFromCelsius(deltaCelsius: Float): String {
    val scaled =
      when (this) {
        CELSIUS, KELVIN -> deltaCelsius
        FAHRENHEIT -> deltaCelsius * 1.8f
      }
    val sign = if (scaled >= 0f) "+" else ""
    return String.format(Locale.US, "%s%.1f%s", sign, scaled, symbol)
  }
}

enum class ThemeMode(val labelRu: String) {
  DARK("Тёмная"),
  LIGHT("Светлая"),
  SYSTEM("Авто"),
}

enum class PollingInterval(val delayMs: Long, val labelRu: String) {
  FAST_250(250L, "250 мс"),
  NORMAL_500(500L, "500 мс"),
  STANDARD_1000(1000L, "1.0 с"),
  SLOW_2000(2000L, "2.0 с"),
}

enum class ThermalSeverity(val labelRu: String, val thresholdHintRu: String) {
  NOMINAL("Номинальный режим", "В пределах рабочей нормы"),
  ELEVATED("Умеренный нагрев", "Активная вычислительная нагрузка"),
  HIGH("Высокий нагрев", "Близко к порогу терморегуляции"),
  CRITICAL("Критический нагрев", "Активен сброс частот (троттлинг)"),
}

data class ChannelStats(
  val channel: PrimaryChannel,
  val currentCelsius: Float,
  val minCelsius: Float,
  val maxCelsius: Float,
  val avgCelsius: Float,
  val deltaCelsiusPerMin: Float,
  val sensorSource: String,
  val severity: ThermalSeverity,
)

data class CpuCoreStatus(
  val coreIndex: Int,
  val clusterName: String,
  val currentFreqMhz: Int,
  val minFreqMhz: Int,
  val maxFreqMhz: Int,
  val loadFraction: Float,
  val estimatedTempCelsius: Float,
  val isHotspotCore: Boolean,
)

data class ThermalZoneReading(
  val id: String,
  val sensorType: String,
  val subsystemRu: String,
  val tempCelsius: Float,
  val kernelPath: String,
  val isPrimaryHotspot: Boolean,
)

data class BatteryTelemetry(
  val tempCelsius: Float,
  val voltageMv: Int,
  val currentMa: Int,
  val levelPercent: Int,
  val isCharging: Boolean,
  val statusRu: String,
  val powerSourceRu: String,
  val healthRu: String,
  val technology: String,
  val powerWatts: Float,
)

data class TelemetrySamplePoint(
  val timestampMs: Long,
  val sampleIndex: Long,
  val cpuCelsius: Float,
  val hotspotCelsius: Float,
  val batteryCelsius: Float,
)

data class TelemetryUiState(
  val selectedChannel: PrimaryChannel = PrimaryChannel.HOTSPOT,
  val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
  val themeMode: ThemeMode = ThemeMode.DARK,
  val pollingInterval: PollingInterval = PollingInterval.NORMAL_500,
  val isPaused: Boolean = false,
  val isStressTestRunning: Boolean = false,
  val stressWorkersCount: Int = 0,
  val cpuStats: ChannelStats,
  val hotspotStats: ChannelStats,
  val batteryStats: ChannelStats,
  val cores: List<CpuCoreStatus> = emptyList(),
  val thermalZones: List<ThermalZoneReading> = emptyList(),
  val batteryDetails: BatteryTelemetry,
  val history: List<TelemetrySamplePoint> = emptyList(),
  val inspectedHistoryIndex: Int? = null,
  val systemThermalStatusRu: String = "Номинальный статус ОС",
  val deviceSocModel: String = "",
  val totalSamplesCollected: Long = 0L,
) {
  fun statsFor(channel: PrimaryChannel): ChannelStats =
    when (channel) {
      PrimaryChannel.CPU -> cpuStats
      PrimaryChannel.HOTSPOT -> hotspotStats
      PrimaryChannel.BATTERY -> batteryStats
    }
}
