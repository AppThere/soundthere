package app.loki.soundthere

// This file is the app's own (egui2compose writes it once). SoundCraft's arrangement is drawn here from
// `session.inspect` (Arrangement.kt): tracks, clips, markers; the engine has no picture of it.
// Written ONCE by egui2compose for an app with the art workspace and never written again. It says what the generic workspace cannot know: which
// command describes the document, which panels the dock has and what they draw, how a tool is
// picked, and what the status bar says. The defaults below work for any engine; replace them.

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.loki.kit.ChoiceChip
import app.loki.kit.DockPanel
import app.loki.kit.EngineClient
import app.loki.kit.NumberChip
import app.loki.kit.RailTool
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.Square
import com.composables.icons.lucide.AudioLines
import com.composables.icons.lucide.SlidersVertical
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/** Read after every change: the session (tracks, clips, markers, selection, transport). */
val STATE_COMMANDS: List<String> = listOf("session.inspect")

internal fun JsonElement?.obj() = this as? JsonObject
internal fun JsonElement?.arr(): List<JsonElement> = (this as? JsonArray)?.toList() ?: emptyList()
internal fun JsonElement?.str(key: String): String? = ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.contentOrNull
internal fun JsonElement?.num(key: String): Double? = ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.doubleOrNull
internal fun JsonElement?.long(key: String): Long? = ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.longOrNull
internal fun JsonElement?.bool(key: String): Boolean? = ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.booleanOrNull

fun docOf(state: JsonElement?): JsonElement? = (state as? JsonObject)?.get("session.inspect")

val PANEL_COMMANDS: Set<String> = setOf(
    "edit.select", "edit.select_none", "mix.mute", "mix.solo", "mix.record_arm", "mix.volume", "track.new", "transport.toggle", "transport.stop", "transport.record",
    "transport.locate", "markers.add", "session.open", "session.save", "session.save_as", "file.import_audio",
)

val FIRST_PANEL: String? = "tracks"

val PANEL_GROUPS: List<List<String>> = listOf(listOf("tracks", "sources", "markers"))

fun documentTitle(state: JsonElement?): String = docOf(state).str("name") ?: APP_NAME

fun documentDirty(state: JsonElement?): Boolean = docOf(state).bool("dirty") == true

/** Samples as m:ss.mmm at the session's rate. */
internal fun clock(samples: Long, rate: Double): String {
    val s = if (rate > 0) samples / rate else 0.0
    val m = (s / 60).toInt(); val sec = s - m * 60
    return "$m:" + (if (sec < 10) "0" else "") + "%.3f".format(sec)
}

fun statusSegments(state: JsonElement?): List<String> {
    val d = docOf(state)
    val rate = d.num("sample_rate") ?: 48000.0
    val tempo = d.obj()?.get("tempo").arr().firstOrNull().num("bpm")
    val meter = d.obj()?.get("meter").arr().firstOrNull()
    return listOfNotNull(
        clock(d.obj()?.get("transport").long("position") ?: 0, rate),
        tempo?.let { "${it.toInt()} bpm" },
        meter?.let { "${it.long("numerator")}/${it.long("denominator")}" },
        d.num("sample_rate")?.let { "${(it / 1000)} kHz" },
        d.str("bit_depth"),
    )
}

fun loadTools(engine: EngineClient): List<RailTool> = emptyList()
fun toolCommand(id: String): Pair<String, JsonObject>? = null
fun toolInHand(state: JsonElement?): String? = null

private fun exec(run: (String, JsonObject) -> Unit, command: String, params: JsonObject = JsonObject(emptyMap())) =
    run(command, params)

internal fun ids(vararg v: Long) = buildJsonArray { v.forEach { add(JsonPrimitive(it)) } }

/** The transport and the session's quick actions. */
@Composable
fun OptionsBar(tool: String, tools: List<RailTool>, state: JsonElement?, run: (String, JsonObject) -> Unit) {
    val d = docOf(state)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton({ exec(run, "transport.locate", buildJsonObject { put("at", 0) }) }, Modifier.size(36.dp)) { Icon(Lucide.SkipBack, "Back to the start", Modifier.size(18.dp)) }
        IconButton({ exec(run, "transport.toggle") }, Modifier.size(36.dp)) { Icon(Lucide.Play, "Play", Modifier.size(18.dp)) }
        IconButton({ exec(run, "transport.stop") }, Modifier.size(36.dp)) { Icon(Lucide.Square, "Stop", Modifier.size(18.dp)) }
        IconButton({ exec(run, "transport.record") }, Modifier.size(36.dp)) { Icon(Lucide.Circle, "Record", Modifier.size(18.dp)) }
        Text(clock(d.obj()?.get("transport").long("position") ?: 0, d.num("sample_rate") ?: 48000.0), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton({ exec(run, "track.new", buildJsonObject { put("kind", "audio"); put("format", "stereo") }) }, Modifier.size(36.dp)) { Icon(Lucide.Plus, "New audio track", Modifier.size(18.dp)) }
        IconButton({ exec(run, "markers.add") }, Modifier.size(36.dp)) { Icon(Lucide.MapPin, "Add a marker", Modifier.size(18.dp)) }
    }
}

@Composable
fun ToolFooter(state: JsonElement?, run: (String, JsonObject) -> Unit) {}

// Opening and saving a session, and importing audio, are the desktop's file dialogs; the engine takes paths.
val APP_ACTIONS: List<app.loki.kit.RibbonItem> = listOf(
    app.loki.kit.RibbonItem("app.open", "Open Session…", ""),
    app.loki.kit.RibbonItem("app.importAudio", "Import Audio…", ""),
    app.loki.kit.RibbonItem("app.save", "Save", ""),
    app.loki.kit.RibbonItem("app.saveAs", "Save As…", ""),
)
val APP_ACTION_IDS: Set<String> = APP_ACTIONS.map { it.id }.toSet()

fun withAppActions(tabs: List<app.loki.kit.RibbonTab>): List<app.loki.kit.RibbonTab> = app.loki.kit.addToFileMenu(tabs, APP_ACTIONS)

suspend fun appAction(id: String, state: JsonElement?, io: app.loki.kit.FileIo) {
    val d = docOf(state)
    fun cmd(command: String, params: JsonObject) = buildJsonObject { put("command", command); put("params", params) }
    suspend fun saveTo(p: String, command: String) {
        if (io.call("engine.execute", cmd(command, buildJsonObject { put("path", p); put("replace", true) })) != null) { io.publish(p); io.refresh(); io.status("Saved ${p.substringAfterLast('/')}") }
    }
    when (id) {
        "app.open" -> {
            val p = io.pick(false, "") ?: return
            if (io.call("engine.execute", cmd("session.open", buildJsonObject { put("path", p) })) != null) io.refresh()
        }
        "app.importAudio" -> {
            val p = io.pick(false, "") ?: return
            if (io.call("engine.execute", cmd("file.import_audio", buildJsonObject { put("path", p) })) != null) io.refresh()
        }
        "app.save" -> d.str("path")?.let { saveTo(it, "session.save") } ?: appAction("app.saveAs", state, io)
        "app.saveAs" -> {
            val p = io.pick(true, (d.str("name") ?: "Session").replace('/', '_') + ".scs") ?: return
            saveTo(p, "session.save_as")
        }
    }
}

fun dockPanels(state: JsonElement?, revision: Int, run: (String, JsonObject) -> Unit): List<DockPanel> = listOf(
    DockPanel("tracks", "Tracks", Lucide.SlidersVertical) { m -> TracksPanel(state, run, m) },
    DockPanel("sources", "Clip List", Lucide.AudioLines) { m -> SourcesPanel(state, run, m) },
    DockPanel("markers", "Markers", Lucide.MapPin) { m -> MarkersPanel(state, run, m) },
)

@Composable
private fun TracksPanel(state: JsonElement?, run: (String, JsonObject) -> Unit, modifier: Modifier) {
    val tracks = docOf(state).obj()?.get("tracks").arr()
    val selected = docOf(state).obj()?.get("selection").obj()?.get("tracks").arr().mapNotNull { (it as? JsonPrimitive)?.longOrNull }.toSet()
    if (tracks.isEmpty()) { Text("No tracks: Track › New…, or File › Import Audio…", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyColumn(modifier) {
        items(tracks) { t ->
            val id = t.long("id") ?: return@items
            Column(Modifier.fillMaxWidth().clickable { exec(run, "edit.select", buildJsonObject { put("tracks", ids(id)) }) }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.str("name") ?: "Track", Modifier.weight(1f), maxLines = 1, fontWeight = if (id in selected) FontWeight.Bold else FontWeight.Normal)
                    app.loki.kit.ToggleChip("M", t.bool("mute") == true) { exec(run, "mix.mute", buildJsonObject { put("tracks", ids(id)); put("value", it) }) }
                    Row(Modifier.padding(start = 4.dp)) { app.loki.kit.ToggleChip("S", t.bool("solo") == true) { exec(run, "mix.solo", buildJsonObject { put("tracks", ids(id)); put("value", it) }) } }
                    Row(Modifier.padding(start = 4.dp)) { app.loki.kit.ToggleChip("R", t.bool("record_arm") == true) { exec(run, "mix.record_arm", buildJsonObject { put("tracks", ids(id)); put("value", it) }) } }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberChip("Volume", t.num("volume_db"), -144.0, 12.0, { exec(run, "mix.volume", buildJsonObject { put("tracks", ids(id)); put("db", it) }) }, unit = " dB", step = 0.5, decimals = 1)
                    Text((t.str("kind") ?: "") + " · " + (t.str("format") ?: ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}

@Composable
private fun SourcesPanel(state: JsonElement?, run: (String, JsonObject) -> Unit, modifier: Modifier) {
    val rate = docOf(state).num("sample_rate") ?: 48000.0
    val sources = docOf(state).obj()?.get("sources").arr()
    if (sources.isEmpty()) { Text("No audio files in the session.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyColumn(modifier) {
        items(sources) { s ->
            Column(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(s.str("name") ?: "Audio", maxLines = 1)
                Text("${clock(s.long("frames") ?: 0, rate)} · ${s.long("channels") ?: 0} ch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MarkersPanel(state: JsonElement?, run: (String, JsonObject) -> Unit, modifier: Modifier) {
    val rate = docOf(state).num("sample_rate") ?: 48000.0
    val markers = docOf(state).obj()?.get("markers").arr()
    if (markers.isEmpty()) { Text("No markers: the pin button adds one at the playhead.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant); return }
    LazyColumn(modifier) {
        items(markers) { mk ->
            Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).clickable { exec(run, "transport.locate", buildJsonObject { put("at", mk.long("start") ?: 0) }) }.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${mk.long("number") ?: ""}  ${mk.str("name").orEmpty()}", Modifier.weight(1f), maxLines = 1)
                Text(clock(mk.long("start") ?: 0, rate), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
