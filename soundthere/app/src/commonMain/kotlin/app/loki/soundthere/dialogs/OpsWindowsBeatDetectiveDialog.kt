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
 * Ops windows beat detective: translated from `dialogs/ops_windows.rs` (fn `beat_detective`, line 92) by egui2compose.
 *
 * The controls and their order are the desktop dialog's. NOT translated: the starting values (the
 * desktop reads them from its own state), anything it draws by hand, and enabling rules. The
 * parameter names are guessed from the Rust fields each control was bound to: check them against
 * the command's params doc before trusting OK.
 *   command: event.beat_detective
 *   params:  {tracks?, start?, end?, sensitivity?: 0.5, separate?: false, set_tempo?: false}
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpsWindowsBeatDetectiveDialog(onRun: (command: String, params: JsonObject) -> Unit, onDismiss: () -> Unit) {
    var f0 by remember { mutableStateOf(0.0f) }
    var f1 by remember { mutableStateOf(false) }
    var f2 by remember { mutableStateOf(false) }
    var f3 by remember { mutableStateOf("") }
    SheetDialog(
        "Ops windows beat detective", "", onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(onClick = {
                val params = buildJsonObject {
                put("sensitivity", f0)
                put("tracks", f1)
                put("ui", f2)
                f3.trim().replace(',', '.').toDoubleOrNull()?.let { put("identifyBars", it) }
                }
                onRun("event.beat_detective", params)
                onDismiss()
            }) { Text("OK") }
        },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Sensitivity" + ": " + (kotlin.math.round(f0 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f0, { f0 = it }, valueRange = minOf(0.0f, 1.0f)..maxOf(0.0f, 1.0f))
            Row(Modifier.fillMaxWidth().clickable { f1 = !f1 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f1, { f1 = it })
                Text("Analyses the edit selection on the selected tracks.")
            }
            Row(Modifier.fillMaxWidth().clickable { f2 = !f2 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f2, { f2 = it })
                Text("Ui")
            }
            OutlinedTextField(f3, { f3 = it }, label = { Text("Identify Beat: selection is") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            // egui button "Analyze" runs event.beat_detective
            // egui button "Apply" runs event.beat_detective
            // egui button "Set Tempo" runs event.identify_beat
        }
    }
}
