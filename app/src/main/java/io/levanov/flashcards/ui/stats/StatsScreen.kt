package io.levanov.flashcards.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.levanov.flashcards.srs.DeckStats
import io.levanov.flashcards.ui.theme.FlashcardsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    when (val s = uiState) {
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
        else -> StatsContent(uiState = s, onExit = onExit, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    uiState: StatsViewModel.StatsUiState,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Stats") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            item(key = "global") {
                val g = uiState.global
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("All decks", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${g.total} cards · ${g.newCount} new · ${g.learningCount} learning · ${g.masteredCount} mastered",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Lifetime accuracy: ${g.accuracyPercent}%  (${g.correct}/${g.seen})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "1 = hard … 6 = mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BoxDistribution(stats = g)
                    }
                }
            }

            val grouped = uiState.decks.groupBy { it.group }
            grouped.forEach { (group, groupDecks) ->
                item(key = "header:$group") {
                    Text(
                        text = group.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 4.dp,
                        ),
                    )
                }
                items(items = groupDecks, key = { it.deckName }) { deckUi ->
                    val s = deckUi.stats
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(deckUi.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${s.total} cards · ${s.newCount} new · ${s.learningCount} learning · ${s.masteredCount} mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BoxDistribution(stats = s)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxDistribution(stats: DeckStats, modifier: Modifier = Modifier) {
    val max = stats.boxCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (box in 1..6) {
            val count = stats.boxCounts.getValue(box)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$box",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                LinearProgressIndicator(
                    progress = { count.toFloat() / max },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val fakeStats = DeckStats(
        total = 26,
        newCount = 20,
        learningCount = 4,
        masteredCount = 2,
        boxCounts = mapOf(1 to 2, 2 to 1, 3 to 1, 4 to 0, 5 to 0, 6 to 2),
        seen = 6,
        correct = 4,
    )
    val fakeStats2 = DeckStats(
        total = 26,
        newCount = 5,
        learningCount = 20,
        masteredCount = 1,
        boxCounts = mapOf(1 to 5, 2 to 8, 3 to 7, 4 to 0, 5 to 0, 6 to 1),
        seen = 21,
        correct = 14,
    )
    FlashcardsTheme {
        StatsContent(
            uiState = StatsViewModel.StatsUiState(
                global = fakeStats,
                decks = listOf(
                    StatsViewModel.DeckStatsUi("core/adjectives", "adjectives", "core", fakeStats),
                    StatsViewModel.DeckStatsUi("rivstart/kapitel-01", "kapitel-01", "rivstart", fakeStats2),
                ),
            ),
            onExit = {},
        )
    }
}