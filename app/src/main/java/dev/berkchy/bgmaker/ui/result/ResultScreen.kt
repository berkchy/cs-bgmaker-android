package dev.berkchy.bgmaker.ui.result

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.berkchy.bgmaker.ui.MainViewModel
import dev.berkchy.bgmaker.ui.components.InfoRow
import dev.berkchy.bgmaker.ui.components.SectionLabel
import dev.berkchy.bgmaker.ui.components.TopBar
import dev.berkchy.bgmaker.util.OutputHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onAgain: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val summary = viewModel.result
    var zip by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(summary) {
        if (summary != null) {
            zip = withContext(Dispatchers.IO) { OutputHelper.zipDirectory(summary.outputRoot) }
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        val file = zip
        if (uri != null && file != null) {
            scope.launch(Dispatchers.IO) {
                OutputHelper.writeToUri(context, uri, file)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "ZIP saved to your device", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(topBar = { TopBar(title = "Result") }) { padding ->
        val result = summary
        if (result == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Nothing to show yet.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAgain) { Text("Pick an image") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
            Text(
                "Background ready",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                "${result.tileCount} 32-bit TGA tiles and both layout files are written.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionLabel("Conversion summary")
                    InfoRow("Image", result.imageName)
                    InfoRow("Resolution", result.resolution.toString())
                    InfoRow("Grid", "${result.grid.cols} × ${result.grid.rows}")
                    InfoRow("Mode", result.mode.value)
                    InfoRow("Prefix", "${result.prefix}_r_c.tga")
                    InfoRow("Output", result.outputRoot.name)
                    if (result.backupName != null) {
                        InfoRow("Backup", result.backupName)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(
                    text = "Copy the tiles into your CS 1.6 resource/background folder and place both layout files in resource/.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        zip?.let { file ->
                            context.startActivity(
                                Intent.createChooser(
                                    OutputHelper.shareIntent(context, file),
                                    "Share background",
                                ),
                            )
                        }
                    },
                    enabled = zip != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("Share ZIP")
                }
                FilledTonalButton(
                    onClick = { zip?.let { saveLauncher.launch(it.name) } },
                    enabled = zip != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("Save ZIP to device")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onAgain,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("New image")
                    }
                    OutlinedButton(
                        onClick = onSettings,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text("Backups")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
