package app.loki.soundthere

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import app.loki.kit.LocalWorkspace
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val LANE_DP = 72f
private const val RULER_DP = 28f

/**
 * The arrangement: a ruler with the markers, a lane for each track with its clips, the selection
 * and the playhead. Two fingers or Ctrl and the wheel zoom the time axis, a drag or the wheel
 * scrolls it, a tap selects a clip, or a track where there is none.
 */
@Composable
fun Arrangement(modifier: Modifier) {
    val ws = LocalWorkspace.current
    val d = docOf(ws?.state)
    val rate = d.num("sample_rate") ?: 48000.0
    val length = (d.long("length_samples") ?: 0L).coerceAtLeast((rate * 30).toLong())
    val tracks = d.obj()?.get("tracks").arr().filter { it.bool("hidden") != true }
    val markers = d.obj()?.get("markers").arr()
    val selection = d.obj()?.get("selection")
    val selClips = selection.obj()?.get("clips").arr().mapNotNull { (it as? JsonPrimitive)?.content?.toLongOrNull() }.toSet()
    val selTracks = selection.obj()?.get("tracks").arr().mapNotNull { (it as? JsonPrimitive)?.content?.toLongOrNull() }.toSet()
    val playhead = d.obj()?.get("transport").long("position") ?: 0L
    var secondsPerPx by remember { mutableFloatStateOf(0f) }
    var scrollSec by remember { mutableFloatStateOf(0f) }
    val measurer = rememberTextMeasurer()
    val colors = MaterialTheme.colorScheme
    val clipColor = Color(0xFF3E7BD4); val lane = colors.surface; val laneAlt = colors.surfaceVariant.copy(alpha = 0.5f)
    val runCmd = rememberUpdatedState(ws)
    val labelStyle = TextStyle(color = colors.onSurface, fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp))
    Box(modifier.background(colors.background)) {
        if (tracks.isEmpty()) {
            Text("An empty session: File › Import Audio…, or the + button for a track.", Modifier.align(Alignment.Center).padding(16.dp), color = colors.onSurfaceVariant)
        }
        Canvas(
            Modifier.fillMaxSize()
                .pointerInput(rate, length) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (secondsPerPx <= 0f) return@detectTransformGestures
                        val before = scrollSec + centroid.x * secondsPerPx
                        secondsPerPx = (secondsPerPx / zoom).coerceIn(0.0005f, (length / rate / 50).toFloat().coerceAtLeast(0.01f))
                        scrollSec = (before - centroid.x * secondsPerPx - pan.x * secondsPerPx).coerceAtLeast(0f)
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            if (e.type == PointerEventType.Scroll) {
                                val c = e.changes.firstOrNull() ?: continue
                                val dy = c.scrollDelta.y; val dx = c.scrollDelta.x
                                if (e.keyboardModifiers.isCtrlPressed && secondsPerPx > 0f) {
                                    val before = scrollSec + c.position.x * secondsPerPx
                                    secondsPerPx = (secondsPerPx * (if (dy > 0) 1.15f else 1 / 1.15f)).coerceIn(0.0005f, 600f)
                                    scrollSec = (before - c.position.x * secondsPerPx).coerceAtLeast(0f)
                                } else scrollSec = (scrollSec + (dx + dy) * 30 * secondsPerPx).coerceAtLeast(0f)
                                c.consume()
                            }
                        }
                    }
                }
                .pointerInput(tracks, secondsPerPx, scrollSec) {
                    detectTapGestures { p ->
                        val w = runCmd.value ?: return@detectTapGestures
                        val laneH = LANE_DP * density; val rulerH = RULER_DP * density
                        if (p.y < rulerH) {
                            val at = ((scrollSec + p.x * secondsPerPx) * rate).toLong().coerceAtLeast(0)
                            w.run("engine.execute", buildJsonObject { put("command", "transport.locate"); put("params", buildJsonObject { put("at", at) }) }) {}
                            return@detectTapGestures
                        }
                        val row = ((p.y - rulerH) / laneH).toInt()
                        val track = tracks.getOrNull(row) ?: return@detectTapGestures
                        val sample = (scrollSec + p.x * secondsPerPx) * rate
                        val clip = track.obj()?.get("clips").arr().firstOrNull { c -> sample >= (c.long("start") ?: 0) && sample < (c.long("end") ?: 0) }
                        val params = if (clip != null) buildJsonObject { put("clips", ids(clip.long("id") ?: return@detectTapGestures)) } else buildJsonObject { put("tracks", ids(track.long("id") ?: return@detectTapGestures)) }
                        w.run("engine.execute", buildJsonObject { put("command", "edit.select"); put("params", params) }) {}
                    }
                },
        ) {
            if (secondsPerPx <= 0f) secondsPerPx = (length / rate / (size.width.coerceAtLeast(1f))).toFloat()
            val laneH = LANE_DP.dp.toPx(); val rulerH = RULER_DP.dp.toPx()
            // Text is drawn only where it has room: a label past the canvas's edge would be given a negative width.
            fun label(text: String, at: Offset, style: TextStyle, width: Float = size.width - at.x) {
                if (at.x < 0f || at.y < 0f || width < 4f || size.height - at.y < 4f) return
                drawText(measurer, text, at, style, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Clip, size = Size(width, minOf(20.dp.toPx(), size.height - at.y)))
            }
            fun x(sample: Long) = ((sample / rate - scrollSec) / secondsPerPx).toFloat()
            // Lanes.
            tracks.forEachIndexed { i, t ->
                val top = rulerH + i * laneH
                drawRect(if (i % 2 == 0) lane else laneAlt, Offset(0f, top), Size(size.width, laneH))
                if ((t.long("id") ?: -1L) in selTracks) drawRect(clipColor.copy(alpha = 0.12f), Offset(0f, top), Size(size.width, laneH))
                drawLine(colors.outlineVariant, Offset(0f, top + laneH), Offset(size.width, top + laneH))
                for (c in t.obj()?.get("clips").arr()) {
                    val x0 = x(c.long("start") ?: 0); val x1 = x(c.long("end") ?: 0)
                    if (x1 < 0 || x0 > size.width) continue
                    val chosen = (c.long("id") ?: -1L) in selClips
                    val muted = c.bool("muted") == true || t.bool("mute") == true
                    val body = if (muted) clipColor.copy(alpha = 0.35f) else clipColor
                    drawRoundRect(body, Offset(x0, top + 4.dp.toPx()), Size((x1 - x0).coerceAtLeast(2f), laneH - 8.dp.toPx()), CornerRadius(4.dp.toPx()))
                    drawRoundRect(if (chosen) Color.White else Color.Black.copy(alpha = 0.4f), Offset(x0, top + 4.dp.toPx()), Size((x1 - x0).coerceAtLeast(2f), laneH - 8.dp.toPx()), CornerRadius(4.dp.toPx()), style = Stroke(if (chosen) 2.5f else 1f))
                    if (x1 - x0 > 30) label(c.str("name") ?: "", Offset(maxOf(x0, 0f) + 6.dp.toPx(), top + 8.dp.toPx()), labelStyle.copy(color = Color.White), minOf(x1 - maxOf(x0, 0f) - 8.dp.toPx(), size.width - maxOf(x0, 0f) - 6.dp.toPx()))
                }
                label(t.str("name") ?: "", Offset(6.dp.toPx(), top + laneH - 18.dp.toPx()), labelStyle.copy(color = colors.onSurfaceVariant))
            }
            // Ruler: seconds, at a spacing the zoom allows.
            drawRect(colors.surfaceVariant, Offset.Zero, Size(size.width, rulerH))
            val steps = listOf(0.1, 0.5, 1.0, 2.0, 5.0, 10.0, 15.0, 30.0, 60.0, 120.0, 300.0, 600.0)
            val step = steps.firstOrNull { it / secondsPerPx >= 70 } ?: steps.last()
            var s = Math.floor(scrollSec / step) * step
            while ((s - scrollSec) / secondsPerPx < size.width) {
                val px = ((s - scrollSec) / secondsPerPx).toFloat()
                drawLine(colors.outline, Offset(px, rulerH - 8.dp.toPx()), Offset(px, rulerH))
                val text = if (step < 1) "%.1f".format(s) else "${(s / 60).toInt()}:" + "%02d".format((s % 60).toInt())
                label(text, Offset(px + 3.dp.toPx(), 2.dp.toPx()), labelStyle.copy(color = colors.onSurfaceVariant))
                s += step
            }
            for (m in markers) {
                val px = x(m.long("start") ?: 0)
                drawLine(Color(0xFFE5A100), Offset(px, 0f), Offset(px, size.height), strokeWidth = 1f)
                label(m.str("name") ?: "", Offset(px + 3.dp.toPx(), rulerH - 18.dp.toPx()), labelStyle.copy(color = Color(0xFFE5A100)))
            }
            val ph = x(playhead)
            drawLine(Color(0xFFE5484D), Offset(ph, 0f), Offset(ph, size.height), strokeWidth = 2f)
        }
    }
}
