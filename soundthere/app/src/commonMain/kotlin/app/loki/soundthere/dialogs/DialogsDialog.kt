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
 * Dialogs: translated from `dialogs.rs` (fn `show`, line 461) by egui2compose.
 *
 * The controls and their order are the desktop dialog's. NOT translated: the starting values (the
 * desktop reads them from its own state), anything it draws by hand, and enabling rules. The
 * parameter names are guessed from the Rust fields each control was bound to: check them against
 * the command's params doc before trusting OK.
 *   command: session.revert
 *   params:  {}
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogsDialog(onRun: (command: String, params: JsonObject) -> Unit, onDismiss: () -> Unit) {
    var f0 by remember { mutableStateOf("") }
    var f1 by remember { mutableStateOf("") }
    var f2 by remember { mutableStateOf(false) }
    var f3 by remember { mutableStateOf("") }
    var f4 by remember { mutableStateOf("") }
    var f5 by remember { mutableStateOf(false) }
    var f6 by remember { mutableStateOf(false) }
    var f7 by remember { mutableStateOf("") }
    var f8 by remember { mutableStateOf("") }
    var f9 by remember { mutableStateOf("") }
    var f10 by remember { mutableStateOf(false) }
    var f11 by remember { mutableStateOf(false) }
    var f12 by remember { mutableStateOf(false) }
    var f13 by remember { mutableStateOf("") }
    var f14 by remember { mutableStateOf("") }
    var f15 by remember { mutableStateOf("") }
    var f16 by remember { mutableStateOf("") }
    var f17 by remember { mutableStateOf("") }
    var f18 by remember { mutableStateOf("") }
    var f19 by remember { mutableStateOf("") }
    var f20 by remember { mutableStateOf("") }
    var f21 by remember { mutableStateOf(false) }
    SheetDialog(
        "Dialogs", "", onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            TextButton(onClick = {
                val params = buildJsonObject {
                if (f0.isNotBlank()) put("name", f0)
                if (f1.isNotBlank()) put("sampleRate", f1)
                put("ui", f2)
                if (f3.isNotBlank()) put("format", f3)
                if (f4.isNotBlank()) put("bitDepth", f4)
                put("ui", f5)
                put("ui", f6)
                f7.trim().replace(',', '.').toDoubleOrNull()?.let { put("value", it) }
                f8.trim().replace(',', '.').toDoubleOrNull()?.let { put("bpm", it) }
                if (f9.isNotBlank()) put("name", f9)
                put("ui", f10)
                put("ui", f11)
                put("ui", f12)
                if (f13.isNotBlank()) put("frameRate", f13)
                if (f14.isNotBlank()) put("bitDepth", f14)
                if (f15.isNotBlank()) put("by", f15)
                f16.trim().replace(',', '.').toDoubleOrNull()?.let { put("count", it) }
                if (f17.isNotBlank()) put("length", f17)
                if (f18.isNotBlank()) put("title", f18)
                if (f19.isNotBlank()) put("composer", f19)
                f20.trim().replace(',', '.').toDoubleOrNull()?.let { put("barsPerSystem", it) }
                put("ui", f21)
                }
                onRun("session.revert", params)
                onDismiss()
            }) { Text("OK") }
        },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(f0, { f0 = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Sample Rate", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(f1 == "r", { f1 = "r" }, label = { Text("{r} Hz") })
            }
            Row(Modifier.fillMaxWidth().clickable { f2 = !f2 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f2, { f2 = it })
                Text("Ui")
            }
            Text("Format", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            }
            Text("Bit Depth", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(f4 == "b.to_string()", { f4 = "b.to_string()" }, label = { Text("{b}-bit") })
            }
            Row(Modifier.fillMaxWidth().clickable { f5 = !f5 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f5, { f5 = it })
                Text("Ui")
            }
            Row(Modifier.fillMaxWidth().clickable { f6 = !f6 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f6, { f6 = it })
                Text("Ui")
            }
            OutlinedTextField(f7, { f7 = it }, label = { Text("Bounces the edit selection, or the whole session when nothing is selected.") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f8, { f8 = it }, label = { Text("Bpm") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f9, { f9 = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().clickable { f10 = !f10 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f10, { f10 = it })
                Text("Ui")
            }
            Row(Modifier.fillMaxWidth().clickable { f11 = !f11 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f11, { f11 = it })
                Text("Ui")
            }
            Row(Modifier.fillMaxWidth().clickable { f12 = !f12 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f12, { f12 = it })
                Text("Tracks")
            }
            Text("Timecode Rate", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            }
            Text("Bit Depth", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            }
            Text("By", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(f15 == "Count", { f15 = "Count" }, label = { Text("Number of Loops") })
                FilterChip(f15 == "Length", { f15 = "Length" }, label = { Text("Loop Length") })
                FilterChip(f15 == "Fill", { f15 = "Fill" }, label = { Text("Loop Until End of Session or Next Clip") })
            }
            OutlinedTextField(f16, { f16 = it }, label = { Text("Count") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f17, { f17 = it }, label = { Text("Length") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f18, { f18 = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f19, { f19 = it }, label = { Text("Composer") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(f20, { f20 = it }, label = { Text("Bars per system") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().clickable { f21 = !f21 }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(f21, { f21 = it })
                Text("Ui")
            }
            // egui button "Clear"
            // egui button "Don't Save"
        }
    }
}
