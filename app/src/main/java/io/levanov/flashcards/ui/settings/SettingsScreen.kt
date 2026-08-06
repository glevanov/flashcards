package io.levanov.flashcards.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.levanov.flashcards.data.AppSettings
import io.levanov.flashcards.data.backup.suggestedBackupFileName
import io.levanov.flashcards.ui.theme.FlashcardsTheme
import kotlin.math.roundToInt

private const val MIN_NEW_CARDS = 0
private const val MAX_NEW_CARDS = 50
private const val NEW_CARDS_STEP = 5

private val IMPORT_MIME_TYPES = arrayOf("application/json", "text/plain")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.messageShown()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> if (uri != null) vm.onExportUri(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) vm.onImportUri(uri) }

    ui.pendingImport?.let { preview ->
        AlertDialog(
            onDismissRequest = vm::dismissImport,
            title = { Text("Import backup") },
            text = {
                Text("Replace all current progress with this backup? ${preview.cards.size} cards will be imported.")
            },
            confirmButton = {
                TextButton(onClick = vm::confirmImport) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissImport) { Text("Cancel") }
            },
        )
    }

    when (val s = settings) {
        null -> Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        else -> SettingsContent(
            settings = s,
            appInfo = vm.appInfo,
            busy = ui.busy,
            onNewCardsChange = vm::setNewCardsPerDay,
            onTtsChange = vm::setTtsEnabled,
            onExport = { exportLauncher.launch(suggestedBackupFileName()) },
            onImport = { importLauncher.launch(IMPORT_MIME_TYPES) },
            onExit = onExit,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    settings: AppSettings,
    appInfo: AppInfo,
    busy: Boolean,
    onNewCardsChange: (Int) -> Unit,
    onTtsChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("New cards per day", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${settings.newCardsPerDay}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = settings.newCardsPerDay.toFloat(),
                    onValueChange = { onNewCardsChange(it.roundToInt()) },
                    valueRange = MIN_NEW_CARDS.toFloat()..MAX_NEW_CARDS.toFloat(),
                    steps = (MAX_NEW_CARDS / NEW_CARDS_STEP) - 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ListItem(
                headlineContent = { Text("Swedish pronunciation") },
                supportingContent = { Text("Speaker button on study cards") },
                trailingContent = {
                    Switch(
                        checked = settings.ttsEnabled,
                        onCheckedChange = onTtsChange,
                    )
                },
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Backup", style = MaterialTheme.typography.titleMedium)
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
                OutlinedButton(
                    onClick = onExport,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Export backup")
                }
                OutlinedButton(
                    onClick = onImport,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Import backup")
                }
                Text(
                    "Saves your progress and settings to a JSON file. " +
                        "Importing replaces all current progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "Version ${appInfo.versionName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (appInfo.isDebug) {
                    Text(
                        "Debug build",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FlashcardsTheme {
        SettingsContent(
            settings = AppSettings(newCardsPerDay = 10, ttsEnabled = true),
            appInfo = AppInfo(
                versionName = "2",
                isDebug = true,
            ),
            busy = false,
            onNewCardsChange = {},
            onTtsChange = {},
            onExport = {},
            onImport = {},
            onExit = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
