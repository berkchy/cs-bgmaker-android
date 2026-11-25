package dev.berkchy.bgmaker.ui.configure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import dev.berkchy.bgmaker.core.Grid
import dev.berkchy.bgmaker.core.Presets
import dev.berkchy.bgmaker.core.Resolution
import dev.berkchy.bgmaker.core.ResizeMode
import dev.berkchy.bgmaker.core.buildGrid
import dev.berkchy.bgmaker.ui.MainViewModel
import dev.berkchy.bgmaker.ui.components.InfoRow
import dev.berkchy.bgmaker.ui.components.SectionLabel
import dev.berkchy.bgmaker.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onConvert: () -> Unit,
) {
    val image = viewModel.selectedImage
    val settings = viewModel.settings
    val prefixValid = settings.prefix.matches(Regex("[A-Za-z0-9_-]+"))

    var expanded by remember { mutableStateOf(false) }
    var customWidth by remember { mutableStateOf(settings.resolution.width.toString()) }
    var customHeight by remember { mutableStateOf(settings.resolution.height.toString()) }

    fun applyCustom() {
        val w = customWidth.toIntOrNull()
        val h = customHeight.toIntOrNull()
        if (w != null && h != null && w > 0 && h > 0 && Resolution(w, h) != settings.resolution) {
            viewModel.updateSettings { it.copy(resolution = Resolution(w, h)) }
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Configure", onBack = onBack) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onConvert,
                        enabled = image != null && prefixValid,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Convert image", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
    ) { padding ->
        if (image == null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No image selected. Go back and choose a photo first.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Source", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(
                            image.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                        Text(
                            "${image.bitmap.width} × ${image.bitmap.height} px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SectionLabel("Resolution")
            val presetLabel = Presets.resolutions.firstOrNull { it.first == settings.resolution }?.second ?: "Custom"
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = "${settings.resolution}  ·  $presetLabel",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Output resolution") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    Presets.resolutions.forEach { (resolution, label) ->
                        DropdownMenuItem(
                            text = { Text("$resolution   ·   $label") },
                            onClick = {
                                viewModel.updateSettings { it.copy(resolution = resolution) }
                                expanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Custom size…") },
                        onClick = { expanded = false },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customWidth,
                    onValueChange = { customWidth = it; applyCustom() },
                    label = { Text("Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    value = customHeight,
                    onValueChange = { customHeight = it; applyCustom() },
                    label = { Text("Height") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                )
            }

            SectionLabel("Resize mode")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ResizeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.mode == mode,
                        onClick = { viewModel.updateSettings { it.copy(mode = mode) } },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                        label = { Text(mode.value.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Text(
                settings.mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            SectionLabel("Output & naming")
            OutlinedTextField(
                value = settings.outputFolderName,
                onValueChange = { value -> viewModel.updateSettings { it.copy(outputFolderName = value) } },
                label = { Text("Output folder") },
                supportingText = { Text("Saved under Documents/BGMaker") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            OutlinedTextField(
                value = settings.prefix,
                onValueChange = { value -> viewModel.updateSettings { it.copy(prefix = value) } },
                label = { Text("Tile prefix") },
                supportingText = {
                    Text(
                        if (prefixValid) "Files look like ${settings.prefix}_0_0.tga"
                        else "Use letters, numbers, _ or -",
                    )
                },
                isError = !prefixValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Back up existing output", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Move old tiles aside before overwriting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.backup,
                    onCheckedChange = { checked -> viewModel.updateSettings { it.copy(backup = checked) } },
                )
            }

            SectionLabel("Layout preview")
            val grid = buildGrid(settings.resolution.width, settings.resolution.height)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GridPreview(grid = grid)
                    Spacer(Modifier.height(14.dp))
                    InfoRow("Grid", "${grid.cols} × ${grid.rows} cells")
                    InfoRow("Tiles", "${grid.tileCount} TGA files")
                    InfoRow("Last tile", "${settings.prefix.trim().ifBlank { "bg" }}_${grid.rows - 1}_${grid.cols - 1}.tga")
                    InfoRow("Layouts", Presets.LAYOUT_FILES.joinToString(", "))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GridPreview(grid: Grid) {
    val colors = MaterialTheme.colorScheme
    val previewHeight = 132.dp
    val ratio = grid.width.toFloat() / grid.height
    Canvas(
        modifier = Modifier
            .width(previewHeight * ratio)
            .height(previewHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainerHighest),
    ) {
        val sx = size.width / grid.width
        val sy = size.height / grid.height
        grid.cells().forEachIndexed { index, cell ->
            drawRect(
                color = if (index % 2 == 0) colors.primary else colors.tertiary,
                alpha = if (cell.col == grid.cols - 1 || cell.row == grid.rows - 1) 0.95f else 0.65f,
                topLeft = Offset(cell.x * sx, cell.y * sy),
                size = Size(cell.width * sx, cell.height * sy),
            )
        }
    }
}
