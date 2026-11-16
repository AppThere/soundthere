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
 * Ops windows midi ops: translated from `dialogs/ops_windows.rs` (fn `midi_ops`, line 246) by egui2compose.
 *
 * The controls and their order are the desktop dialog's. NOT translated: the starting values (the
 * desktop reads them from its own state), anything it draws by hand, and enabling rules. The
 * parameter names are guessed from the Rust fields each control was bound to: check them against
 * the command's params doc before trusting OK.
 *   command: event.quantize
 *   params:  {grid?: 1/16 | ticks, strength?: 100, swing?: 0, clips?}
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpsWindowsMidiOpsDialog(onRun: (command: String, params: JsonObject) -> Unit, onDismiss: () -> Unit) {
    var f0 by remember { mutableStateOf("") }
    var f1 by remember { mutableStateOf("") }
    var f2 by remember { mutableStateOf(0.0f) }
    var f3 by remember { mutableStateOf(0.0f) }
    var f4 by remember { mutableStateOf(-48.0f) }
    var f5 by remember { mutableStateOf(-127.0f) }
    SheetDialog(
        "Ops windows midi ops", "", onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(onClick = {
                val params = buildJsonObject {
                if (f0.isNotBlank()) put("midiOp", f0)
                if (f1.isNotBlank()) put("quantGrid", f1)
                put("quantStrength", f2)
                put("quantSwing", f3)
                put("transpose", f4)
                put("velocityAdd", f5)
                }
                onRun("event.quantize", params)
                onDismiss()
            }) { Text("OK") }
        },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Operation", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            }
            Text("Grid", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            }
            Text("Strength %" + ": " + (kotlin.math.round(f2 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f2, { f2 = it }, valueRange = minOf(0.0f, 100.0f)..maxOf(0.0f, 100.0f))
            Text("Swing %" + ": " + (kotlin.math.round(f3 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f3, { f3 = it }, valueRange = minOf(0.0f, 100.0f)..maxOf(0.0f, 100.0f))
            Text("Semitones" + ": " + (kotlin.math.round(f4 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f4, { f4 = it }, valueRange = minOf(-48.0f, 48.0f)..maxOf(-48.0f, 48.0f))
            Text("Add" + ": " + (kotlin.math.round(f5 * 100f) / 100f), style = MaterialTheme.typography.labelLarge)
            Slider(f5, { f5 = it }, valueRange = minOf(-127.0f, 127.0f)..maxOf(-127.0f, 127.0f))
            // egui button "Apply" runs event.quantize
        }
    }
}
