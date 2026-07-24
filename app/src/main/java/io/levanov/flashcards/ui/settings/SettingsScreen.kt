package io.levanov.flashcards.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.levanov.flashcards.data.AppSettings
import io.levanov.flashcards.ui.theme.FlashcardsTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settings by vm.settings.collectAsStateWithLifecycle()
    when (val s = settings) {
        null -> Scaffold(modifier = modifier) { padding ->
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
            onNewCardsChange = vm::setNewCardsPerDay,
            onTtsChange = vm::setTtsEnabled,
            onExit = onExit,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    settings: AppSettings,
    onNewCardsChange: (Int) -> Unit,
    onTtsChange: (Boolean) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
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
            // New cards per day
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
                    valueRange = 0f..50f,
                    // steps=9 → 11 positions at 5-card intervals on a 0..50 range
                    steps = 9,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Pronunciation (TTS)
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FlashcardsTheme {
        SettingsContent(
            settings = AppSettings(newCardsPerDay = 10, ttsEnabled = true),
            onNewCardsChange = {},
            onTtsChange = {},
            onExit = {},
        )
    }
}