package dev.berkchy.bgmaker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.berkchy.bgmaker.core.BackupManager
import dev.berkchy.bgmaker.theme.ThemeMode
import dev.berkchy.bgmaker.ui.MainViewModel
import dev.berkchy.bgmaker.ui.components.SectionLabel
import dev.berkchy.bgmaker.ui.components.TopBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val settings = viewModel.settings
    var refresh by remember { mutableStateOf(0) }
    val backups = remember(refresh, settings.outputFolderName) {
        BackupManager.list(viewModel.outputRoot())
    }
    var pending by remember { mutableStateOf<Pair<String, File>?>(null) }

    Scaffold(topBar = { TopBar(title = "Settings", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionLabel("Appearance")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.updateSettings { it.copy(themeMode = mode) } },
                                shape = SegmentedButtonDefaults.itemShape(index, 3),
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dynamic colors", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Use colors from your wallpaper (Android 12+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = { checked -> viewModel.updateSettings { it.copy(dynamicColor = checked) } },
                        )
                    }
                }
            }

            SectionLabel("Defaults")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = settings.outputFolderName,
                        onValueChange = { value -> viewModel.updateSettings { it.copy(outputFolderName = value) } },
                        label = { Text("Output folder") },
                        supportingText = { Text("Under Documents/BGMaker") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    OutlinedTextField(
                        value = settings.prefix,
                        onValueChange = { value -> viewModel.updateSettings { it.copy(prefix = value) } },
                        label = { Text("Tile prefix") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Back up before converting", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Keep the previous tiles and layouts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.backup,
                            onCheckedChange = { checked -> viewModel.updateSettings { it.copy(backup = checked) } },
                        )
                    }
                }
            }

            SectionLabel("Backups")
            if (backups.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        "No backups yet. They appear here after a conversion replaces an existing output.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    backups.forEach { backup ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        backup.name.removePrefix("_backup_").replace('-', ' '),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        "${File(backup, "background").listFiles()?.size ?: 0} tiles",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { pending = "restore" to backup }) {
                                    Icon(Icons.Filled.Restore, contentDescription = "Restore backup")
                                }
                                IconButton(onClick = { pending = "delete" to backup }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete backup")
                                }
                            }
                        }
                    }
                }
            }

            SectionLabel("About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BG Maker 1.0", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "An open-source Material 3 companion for the cs-bgmaker workflow. Builds 32-bit TGA tiles and CS 1.6 layout files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    val action = pending
    if (action != null) {
        val (kind, backup) = action
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(if (kind == "restore") "Restore this backup?" else "Delete this backup?") },
            text = {
                Text(
                    if (kind == "restore") {
                        "Current output is backed up first, then the selected tiles and layouts are restored."
                    } else {
                        "This permanently removes the backup folder."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (kind == "restore") BackupManager.restore(viewModel.outputRoot(), backup)
                    else BackupManager.delete(backup)
                    pending = null
                    refresh += 1
                }) { Text(if (kind == "restore") "Restore" else "Delete") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } },
        )
    }
}
