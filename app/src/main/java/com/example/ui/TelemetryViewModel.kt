package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BatteryTelemetry
import com.example.data.ChannelStats
import com.example.data.PollingInterval
import com.example.data.PrimaryChannel
import com.example.data.TelemetrySamplePoint
import com.example.data.TelemetryUiState
import com.example.data.TemperatureUnit
import com.example.data.ThemeMode
import com.example.data.ThermalHardwareReader
import com.example.data.ThermalSeverity
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TelemetryViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs =
    application.getSharedPreferences("thermometry_prefs", Context.MODE_PRIVATE)
  private val hardwareReader = ThermalHardwareReader(application.applicationContext)

  private data class RunningAccumulator(
    var min: Float = Float.POSITIVE_INFINITY,
    var max: Float = Float.NEGATIVE_INFINITY,
    var sum: Double = 0.0,
    var count: Long = 0L,
  ) {
    fun record(value: Float) {
      if (value < min) min = value
      if (value > max) max = value
      sum += value.toDouble()
      count++
    }

    fun reset(currentValue: Float) {
      min = currentValue
      max = currentValue
      sum = currentValue.toDouble()
      count = 1L
    }

    fun average(fallback: Float): Float =
      if (count > 0L) (sum / count).toFloat() else fallback
  }

  private val cpuAcc = RunningAccumulator()
  private val hotspotAcc = RunningAccumulator()
  private val batteryAcc = RunningAccumulator()

  private val historyBuffer = ArrayDeque<TelemetrySamplePoint>(MAX_HISTORY_POINTS)
  private var sampleCounter = 0L
  private var stressJob: Job? = null

  private val _uiState: MutableStateFlow<TelemetryUiState>
  val uiState: StateFlow<TelemetryUiState>

  init {
    val savedTheme =
      prefs.getString(KEY_THEME, ThemeMode.DARK.name)?.let {
        runCatching { ThemeMode.valueOf(it) }.getOrNull()
      } ?: ThemeMode.DARK

    val savedUnit =
      prefs.getString(KEY_UNIT, TemperatureUnit.CELSIUS.name)?.let {
        runCatching { TemperatureUnit.valueOf(it) }.getOrNull()
      } ?: TemperatureUnit.CELSIUS

    val savedInterval =
      prefs.getString(KEY_INTERVAL, PollingInterval.NORMAL_500.name)?.let {
        runCatching { PollingInterval.valueOf(it) }.getOrNull()
      } ?: PollingInterval.NORMAL_500

    val initialFrame = hardwareReader.readFrame(isStressActive = false)
    cpuAcc.record(initialFrame.cpuTempC)
    hotspotAcc.record(initialFrame.hotspotTempC)
    batteryAcc.record(initialFrame.battery.tempCelsius)

    val firstPoint =
      TelemetrySamplePoint(
        timestampMs = System.currentTimeMillis(),
        sampleIndex = ++sampleCounter,
        cpuCelsius = initialFrame.cpuTempC,
        hotspotCelsius = initialFrame.hotspotTempC,
        batteryCelsius = initialFrame.battery.tempCelsius,
      )
    historyBuffer.addLast(firstPoint)

    _uiState =
      MutableStateFlow(
        TelemetryUiState(
          selectedChannel = PrimaryChannel.HOTSPOT,
          temperatureUnit = savedUnit,
          themeMode = savedTheme,
          pollingInterval = savedInterval,
          isPaused = false,
          isStressTestRunning = false,
          stressWorkersCount = 0,
          cpuStats =
            buildChannelStats(
              channel = PrimaryChannel.CPU,
              current = initialFrame.cpuTempC,
              acc = cpuAcc,
              deltaPerMin = 0f,
              source = initialFrame.cpuSensorName,
            ),
          hotspotStats =
            buildChannelStats(
              channel = PrimaryChannel.HOTSPOT,
              current = initialFrame.hotspotTempC,
              acc = hotspotAcc,
              deltaPerMin = 0f,
              source = initialFrame.hotspotSensorName,
            ),
          batteryStats =
            buildChannelStats(
              channel = PrimaryChannel.BATTERY,
              current = initialFrame.battery.tempCelsius,
              acc = batteryAcc,
              deltaPerMin = 0f,
              source = "BatteryManager (${initialFrame.battery.technology})",
            ),
          cores = initialFrame.cores,
          thermalZones = initialFrame.thermalZones,
          batteryDetails = initialFrame.battery,
          history = historyBuffer.toList(),
          inspectedHistoryIndex = null,
          systemThermalStatusRu = initialFrame.systemThermalStatusRu,
          deviceSocModel = initialFrame.deviceHardwareLabel,
          totalSamplesCollected = sampleCounter,
        )
      )
    uiState = _uiState.asStateFlow()

    startTelemetryLoop()
  }

  private fun startTelemetryLoop() {
    viewModelScope.launch(Dispatchers.IO) {
      while (isActive) {
        val state = _uiState.value
        delay(state.pollingInterval.delayMs)
        if (_uiState.value.isPaused) continue

        val frame = hardwareReader.readFrame(isStressActive = _uiState.value.isStressTestRunning)
        cpuAcc.record(frame.cpuTempC)
        hotspotAcc.record(frame.hotspotTempC)
        batteryAcc.record(frame.battery.tempCelsius)

        val nowMs = System.currentTimeMillis()
        val point =
          TelemetrySamplePoint(
            timestampMs = nowMs,
            sampleIndex = ++sampleCounter,
            cpuCelsius = frame.cpuTempC,
            hotspotCelsius = frame.hotspotTempC,
            batteryCelsius = frame.battery.tempCelsius,
          )
        if (historyBuffer.size >= MAX_HISTORY_POINTS) {
          historyBuffer.removeFirst()
        }
        historyBuffer.addLast(point)
        val snapshotHistory = historyBuffer.toList()

        val cpuDelta = computeDeltaPerMinute(snapshotHistory) { it.cpuCelsius }
        val hotspotDelta = computeDeltaPerMinute(snapshotHistory) { it.hotspotCelsius }
        val batteryDelta = computeDeltaPerMinute(snapshotHistory) { it.batteryCelsius }

        _uiState.update { current ->
          current.copy(
            cpuStats =
              buildChannelStats(
                channel = PrimaryChannel.CPU,
                current = frame.cpuTempC,
                acc = cpuAcc,
                deltaPerMin = cpuDelta,
                source = frame.cpuSensorName,
              ),
            hotspotStats =
              buildChannelStats(
                channel = PrimaryChannel.HOTSPOT,
                current = frame.hotspotTempC,
                acc = hotspotAcc,
                deltaPerMin = hotspotDelta,
                source = frame.hotspotSensorName,
              ),
            batteryStats =
              buildChannelStats(
                channel = PrimaryChannel.BATTERY,
                current = frame.battery.tempCelsius,
                acc = batteryAcc,
                deltaPerMin = batteryDelta,
                source = "BatteryManager (${frame.battery.technology})",
              ),
            cores = frame.cores,
            thermalZones = frame.thermalZones,
            batteryDetails = frame.battery,
            history = snapshotHistory,
            inspectedHistoryIndex =
              current.inspectedHistoryIndex?.coerceIn(0, (snapshotHistory.size - 1).coerceAtLeast(0)),
            systemThermalStatusRu = frame.systemThermalStatusRu,
            deviceSocModel = frame.deviceHardwareLabel,
            totalSamplesCollected = sampleCounter,
          )
        }
      }
    }
  }

  private fun computeDeltaPerMinute(
    points: List<TelemetrySamplePoint>,
    selector: (TelemetrySamplePoint) -> Float,
  ): Float {
    if (points.size < 3) return 0f
    val anchorIdx = (points.size - 12).coerceAtLeast(0)
    val anchor = points[anchorIdx]
    val latest = points.last()
    val elapsedMinutes = ((latest.timestampMs - anchor.timestampMs) / 60_000f).coerceAtLeast(0.05f)
    return (selector(latest) - selector(anchor)) / elapsedMinutes
  }

  private fun buildChannelStats(
    channel: PrimaryChannel,
    current: Float,
    acc: RunningAccumulator,
    deltaPerMin: Float,
    source: String,
  ): ChannelStats {
    return ChannelStats(
      channel = channel,
      currentCelsius = current,
      minCelsius = if (acc.min.isInfinite()) current else acc.min,
      maxCelsius = if (acc.max.isInfinite()) current else acc.max,
      avgCelsius = acc.average(current),
      deltaCelsiusPerMin = deltaPerMin,
      sensorSource = source,
      severity = ThermalHardwareReader.evaluateSeverity(channel, current),
    )
  }

  fun selectPrimaryChannel(channel: PrimaryChannel) {
    _uiState.update { it.copy(selectedChannel = channel) }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(KEY_THEME, mode.name).apply()
    _uiState.update { it.copy(themeMode = mode) }
  }

  fun cycleTemperatureUnit() {
    val next =
      when (_uiState.value.temperatureUnit) {
        TemperatureUnit.CELSIUS -> TemperatureUnit.FAHRENHEIT
        TemperatureUnit.FAHRENHEIT -> TemperatureUnit.KELVIN
        TemperatureUnit.KELVIN -> TemperatureUnit.CELSIUS
      }
    prefs.edit().putString(KEY_UNIT, next.name).apply()
    _uiState.update { it.copy(temperatureUnit = next) }
  }

  fun setPollingInterval(interval: PollingInterval) {
    prefs.edit().putString(KEY_INTERVAL, interval.name).apply()
    _uiState.update { it.copy(pollingInterval = interval) }
  }

  fun togglePause() {
    _uiState.update { it.copy(isPaused = !it.isPaused) }
  }

  fun resetPeaks() {
    val current = _uiState.value
    cpuAcc.reset(current.cpuStats.currentCelsius)
    hotspotAcc.reset(current.hotspotStats.currentCelsius)
    batteryAcc.reset(current.batteryStats.currentCelsius)

    _uiState.update { state ->
      state.copy(
        cpuStats =
          state.cpuStats.copy(
            minCelsius = state.cpuStats.currentCelsius,
            maxCelsius = state.cpuStats.currentCelsius,
            avgCelsius = state.cpuStats.currentCelsius,
          ),
        hotspotStats =
          state.hotspotStats.copy(
            minCelsius = state.hotspotStats.currentCelsius,
            maxCelsius = state.hotspotStats.currentCelsius,
            avgCelsius = state.hotspotStats.currentCelsius,
          ),
        batteryStats =
          state.batteryStats.copy(
            minCelsius = state.batteryStats.currentCelsius,
            maxCelsius = state.batteryStats.currentCelsius,
            avgCelsius = state.batteryStats.currentCelsius,
          ),
      )
    }
  }

  fun setInspectedHistoryIndex(index: Int?) {
    _uiState.update { state ->
      val validIdx =
        if (index == null || state.history.isEmpty()) null
        else index.coerceIn(0, state.history.lastIndex)
      state.copy(inspectedHistoryIndex = validIdx)
    }
  }

  fun toggleStressTest() {
    if (_uiState.value.isStressTestRunning) {
      stopStressTest()
    } else {
      startStressTest()
    }
  }

  private fun startStressTest() {
    stressJob?.cancel()
    val workers = min(Runtime.getRuntime().availableProcessors().coerceAtLeast(2), 6)
    _uiState.update {
      it.copy(isStressTestRunning = true, stressWorkersCount = workers, isPaused = false)
    }
    stressJob =
      viewModelScope.launch(Dispatchers.Default) {
        val jobs =
          List(workers) { workerIdx ->
            launch {
              var acc = (workerIdx + 1) * 1.4142135
              var step = 0L
              while (isActive) {
                // Compute real floating-point math bursts with cooperative yield
                for (k in 0 until 18_000) {
                  acc = sin(acc) * cos(acc + k * 0.001) + sqrt(acc * acc + 1.0)
                }
                step++
                if (step % 4L == 0L) {
                  delay(8L)
                }
              }
            }
          }
        // Automatically stop after 60 seconds for thermal safety
        delay(60_000L)
        jobs.forEach { it.cancel() }
        _uiState.update { it.copy(isStressTestRunning = false, stressWorkersCount = 0) }
      }
  }

  private fun stopStressTest() {
    stressJob?.cancel()
    stressJob = null
    _uiState.update { it.copy(isStressTestRunning = false, stressWorkersCount = 0) }
  }

  override fun onCleared() {
    stressJob?.cancel()
    super.onCleared()
  }

  companion object {
    private const val MAX_HISTORY_POINTS = 60
    private const val KEY_THEME = "pref_theme_mode"
    private const val KEY_UNIT = "pref_temp_unit"
    private const val KEY_INTERVAL = "pref_polling_interval"
  }
}
