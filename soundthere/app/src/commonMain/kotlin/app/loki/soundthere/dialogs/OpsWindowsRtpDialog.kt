package app.loki.soundthere.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.loki.kit.SheetDialog
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Ops windows rtp: translated from `dialogs/ops_windows.rs` (fn `rtp`, line 297) by egui2compose.
 *
 * The controls and their order are the desktop dialog's. NOT translated: the starting values (the
 * desktop reads them from its own state), anything it draws by hand, and enabling rules. The
 * parameter names are guessed from the Rust fields each control was bound to: check them against
 * the command's params doc before trusting OK.
 *   command: track.write_midi_rtp
 *   params:  {tracks?} — writes the track's real-time properties (velocity, transpose, duration, delay) into its notes and resets them
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpsWindowsRtpDialog(onRun: (command: String, params: JsonObject) -> Unit, onDismiss: () -> Unit) {
    var f0 by remember { mutableStateOf(-127.0f) }
    var f1 by remember { mutableStateOf(-48.0f) }
    var f2 by remember { mutableStateOf(1.0f) }
    var f3 by remember { mutableStateOf(-960.0f) }
    SheetDialog(
        "Ops windows rtp", "", onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(onClick = {
                val params = buildJsonObject {
                put("rtpVelocity", f0)
                put("rtpTranspose", f1)
                put("rtpDuration", f2)
                put("rtpDelay", f3)
                }
                onRun("track.write_midi_rtp", params)
                onDismiss()
            }) { Text("OK") }
        },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Velocity +" + ": " + (kotlin.math.round(f0 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f0, { f0 = it }, valueRange = minOf(-127.0f, 127.0f)..maxOf(-127.0f, 127.0f))
            Text("Transpose" + ": " + (kotlin.math.round(f1 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f1, { f1 = it }, valueRange = minOf(-48.0f, 48.0f)..maxOf(-48.0f, 48.0f))
            Text("Duration %" + ": " + (kotlin.math.round(f2 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f2, { f2 = it }, valueRange = minOf(1.0f, 400.0f)..maxOf(1.0f, 400.0f))
            Text("Delay (ticks)" + ": " + (kotlin.math.round(f3 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f3, { f3 = it }, valueRange = minOf(-960.0f, 960.0f)..maxOf(-960.0f, 960.0f))
            // egui button "Write to Notes" runs track.write_midi_rtp
            // egui button "Clear" runs event.midi_rtp
        }
    }
}
