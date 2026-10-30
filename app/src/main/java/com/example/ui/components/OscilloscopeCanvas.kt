package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PrimaryChannel
import com.example.data.TelemetrySamplePoint
import com.example.data.TemperatureUnit
import com.example.ui.theme.LocalTelemetryPalette
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun OscilloscopeSection(
  history: List<TelemetrySamplePoint>,
  selectedChannel: PrimaryChannel,
  temperatureUnit: TemperatureUnit,
  inspectedIndex: Int?,
  onInspectIndexChange: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val activeSample =
    if (inspectedIndex != null && inspectedIndex in history.indices) {
      history[inspectedIndex]
    } else {
      history.lastOrNull()
    }

  val minRawC =
    history.minOfOrNull { minOf(it.batteryCelsius, it.cpuCelsius, it.hotspotCelsius) } ?: 25f
  val maxRawC =
    history.maxOfOrNull { maxOf(it.batteryCelsius, it.cpuCelsius, it.hotspotCelsius) } ?: 65f

  val axisMinC = (floor((minRawC - 4f) / 5f) * 5f).coerceAtLeast(15f)
  val axisMaxC = (ceil((maxRawC + 6f) / 5f) * 5f).coerceAtLeast(axisMinC + 20f)
  val axisMidC = (axisMinC + axisMaxC) / 2f

  Column(modifier = modifier.fillMaxWidth().testTag("oscilloscope_section")) {
    // Section Header with live or inspected readout
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column {
        Text(
          text = stringResource(R.string.section_oscilloscope),
          style = MaterialTheme.typography.titleLarge,
          color = palette.textPrimary,
        )
        Text(
          text =
            if (inspectedIndex != null) {
              "Замер кадра #${activeSample?.sampleIndex ?: 0} • Нажмите «Сброс курсора» для возврата"
            } else {
              stringResource(R.string.section_oscilloscope_hint)
            },
          style = MaterialTheme.typography.bodyMedium,
          color = palette.textMuted,
        )
      }

      if (inspectedIndex != null) {
        Box(
          modifier =
            Modifier.border(1.dp, palette.accent)
              .clickable { onInspectIndexChange(null) }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("reset_cursor_button")
        ) {
          Text(
            text = "Сброс курсора",
            style = MaterialTheme.typography.labelMedium,
            color = palette.accent,
          )
        }
      }
    }

    // Legend & Instantaneous values at cursor
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(palette.surface)
          .border(1.dp, palette.hairline)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LegendTraceItem(
        label = "Hotspot ЦП",
        valueText =
          activeSample?.let { temperatureUnit.formatWithUnit(it.hotspotCelsius, 1) } ?: "—",
        traceColor = palette.traceHotspot,
        isSolid = true,
        isFocused = selectedChannel == PrimaryChannel.HOTSPOT,
      )
      LegendTraceItem(
        label = "ЦП средн.",
        valueText = activeSample?.let { temperatureUnit.formatWithUnit(it.cpuCelsius, 1) } ?: "—",
        traceColor = palette.traceCpu,
        isSolid = true,
        isFocused = selectedChannel == PrimaryChannel.CPU,
      )
      LegendTraceItem(
        label = "Батарея",
        valueText =
          activeSample?.let { temperatureUnit.formatWithUnit(it.batteryCelsius, 1) } ?: "—",
        traceColor = palette.traceBattery,
        isSolid = false,
        isFocused = selectedChannel == PrimaryChannel.BATTERY,
      )
    }

    // Precision Canvas Plot
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(196.dp)
          .background(palette.background)
          .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Canvas(
        modifier =
          Modifier.fillMaxSize()
            .testTag("oscilloscope_canvas")
            .pointerInput(history.size) {
              detectTapGestures { offset ->
                if (history.isNotEmpty() && size.width > 0) {
                  val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                  val idx =
                    (fraction * (history.size - 1).coerceAtLeast(0))
                      .roundToInt()
                      .coerceIn(0, history.lastIndex)
                  onInspectIndexChange(idx)
                }
              }
            }
            .pointerInput(history.size) {
              detectHorizontalDragGestures { change, _ ->
                change.consume()
                if (history.isNotEmpty() && size.width > 0) {
                  val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                  val idx =
                    (fraction * (history.size - 1).coerceAtLeast(0))
                      .roundToInt()
                      .coerceIn(0, history.lastIndex)
                  onInspectIndexChange(idx)
                }
              }
            }
      ) {
        val w = size.width
        val h = size.height
        val tempSpan = (axisMaxC - axisMinC).coerceAtLeast(1f)

        fun tempToY(tempC: Float): Float {
          val norm = ((tempC - axisMinC) / tempSpan).coerceIn(0f, 1f)
          return h - norm * h
        }

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        // Horizontal reference grid lines (0%, 25%, 50%, 75%, 100%)
        for (i in 0..4) {
          val y = h * (i / 4f)
          drawLine(
            color = palette.hairline,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = if (i == 0 || i == 4) null else dashEffect,
          )
        }

        // Vertical time division lines (6 segments)
        for (i in 0..6) {
          val x = w * (i / 6f)
          drawLine(
            color = palette.hairline,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1.dp.toPx(),
            pathEffect = if (i == 0 || i == 6) null else dashEffect,
          )
        }

        if (history.isNotEmpty()) {
          val count = history.size
          fun indexToX(idx: Int): Float {
            return if (count <= 1) w else (idx.toFloat() / (count - 1).toFloat()) * w
          }

          fun buildTracePath(selector: (TelemetrySamplePoint) -> Float): Path {
            val path = Path()
            history.forEachIndexed { idx, pt ->
              val x = indexToX(idx)
              val y = tempToY(selector(pt))
              if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
          }

          // 1. Battery trace (dashed precision line)
          val batteryPath = buildTracePath { it.batteryCelsius }
          drawPath(
            path = batteryPath,
            color = palette.traceBattery,
            style =
              Stroke(
                width =
                  if (selectedChannel == PrimaryChannel.BATTERY) 2.6.dp.toPx() else 1.6.dp.toPx(),
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f),
              ),
          )

          // 2. CPU average trace (solid technical line)
          val cpuPath = buildTracePath { it.cpuCelsius }
          drawPath(
            path = cpuPath,
            color = palette.traceCpu,
            style =
              Stroke(
                width = if (selectedChannel == PrimaryChannel.CPU) 2.8.dp.toPx() else 1.8.dp.toPx(),
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter,
              ),
          )

          // 3. Hotspot trace (primary signal orange line)
          val hotspotPath = buildTracePath { it.hotspotCelsius }
          drawPath(
            path = hotspotPath,
            color = palette.traceHotspot,
            style =
              Stroke(
                width =
                  if (selectedChannel == PrimaryChannel.HOTSPOT) 3.0.dp.toPx() else 2.0.dp.toPx(),
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter,
              ),
          )

          // Active cursor or latest reading vertical hairline & node markers
          val markerIdx =
            if (inspectedIndex != null && inspectedIndex in history.indices) {
              inspectedIndex
            } else {
              history.lastIndex
            }
          val markerX = indexToX(markerIdx)
          val pt = history[markerIdx]

          drawLine(
            color = if (inspectedIndex != null) palette.accent else palette.textMuted,
            start = Offset(markerX, 0f),
            end = Offset(markerX, h),
            strokeWidth = 1.2.dp.toPx(),
          )

          val rOuter = 4.dp.toPx()
          val rInner = 2.dp.toPx()

          // Battery marker node
          val yBat = tempToY(pt.batteryCelsius)
          drawCircle(color = palette.traceBattery, radius = rOuter, center = Offset(markerX, yBat))
          drawCircle(color = palette.background, radius = rInner, center = Offset(markerX, yBat))

          // CPU marker node
          val yCpu = tempToY(pt.cpuCelsius)
          drawCircle(color = palette.traceCpu, radius = rOuter, center = Offset(markerX, yCpu))
          drawCircle(color = palette.background, radius = rInner, center = Offset(markerX, yCpu))

          // Hotspot marker node
          val yHot = tempToY(pt.hotspotCelsius)
          drawCircle(color = palette.traceHotspot, radius = rOuter + 1.dp.toPx(), center = Offset(markerX, yHot))
          drawCircle(color = palette.background, radius = rInner, center = Offset(markerX, yHot))
        }
      }

      // Axis scale labels overlaid with clean tabular background chips
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        AxisScaleTag(text = temperatureUnit.formatWithUnit(axisMaxC, 0))
        AxisScaleTag(text = temperatureUnit.formatWithUnit(axisMidC, 0))
        AxisScaleTag(text = temperatureUnit.formatWithUnit(axisMinC, 0))
      }
    }

    HorizontalDivider(thickness = 1.dp, color = palette.hairline)
  }
}

@Composable
private fun AxisScaleTag(text: String) {
  val palette = LocalTelemetryPalette.current
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    color = palette.textMuted,
    modifier =
      Modifier.background(palette.background.copy(alpha = 0.86f))
        .padding(horizontal = 4.dp, vertical = 1.dp),
  )
}

@Composable
private fun LegendTraceItem(
  label: String,
  valueText: String,
  traceColor: androidx.compose.ui.graphics.Color,
  isSolid: Boolean,
  isFocused: Boolean,
) {
  val palette = LocalTelemetryPalette.current
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier =
        Modifier.width(14.dp)
          .height(if (isFocused) 4.dp else 2.dp)
          .background(traceColor)
    )
    if (!isSolid) {
      Spacer(modifier = Modifier.width(2.dp))
      Box(modifier = Modifier.size(3.dp, 2.dp).background(traceColor))
    }
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
        color = if (isFocused) palette.textPrimary else palette.textMuted,
      )
      Text(
        text = valueText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = palette.textPrimary,
      )
    }
  }
}
