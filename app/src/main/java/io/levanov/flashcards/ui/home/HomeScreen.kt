package io.levanov.flashcards.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.levanov.flashcards.data.Card
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.ui.theme.FlashcardsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStudyDeck: (String) -> Unit,
    onStudyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val decks by vm.decks.collectAsStateWithLifecycle()
    when (val d = decks) {
        null -> LoadingScaffold(modifier)
        else -> DeckListContent(
            deckUis = d,
            onStudyDeck = onStudyDeck,
            onStudyAll = onStudyAll,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingScaffold(modifier: Modifier) = Scaffold(modifier = modifier) { padding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckListContent(
    deckUis: List<HomeViewModel.DeckUi>,
    onStudyDeck: (String) -> Unit,
    onStudyAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalDue = deckUis.sumOf { it.dueCount }
    val totalNew = deckUis.sumOf { it.newCount }
    val allAvailable = totalDue + totalNew > 0

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Svenska Flashcards") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
        ) {
            item(key = "study-all") {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .then(if (allAvailable) Modifier.clickable { onStudyAll() } else Modifier),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!allAvailable) Modifier.alpha(0.5f) else Modifier)
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Study all due",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$totalDue due · $totalNew new",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (deckUis.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No decks found")
                    }
                }
            } else {
                val grouped = deckUis.groupBy { it.deck.group }
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
                    items(items = groupDecks, key = { it.deck.name }) { deckUi ->
                        val deck = deckUi.deck
                        ListItem(
                            modifier = Modifier.clickable { onStudyDeck(deck.name) },
                            headlineContent = {
                                Text(
                                    deck.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${deck.cards.size} cards · ${deckUi.dueCount} due · ${deckUi.newCount} new",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                            trailingContent = {
                                LinearProgressIndicator(
                                    progress = { deckUi.progress },
                                    modifier = Modifier.width(56.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FlashcardsTheme {
        DeckListContent(
            deckUis = listOf(
                HomeViewModel.DeckUi(
                    Deck("core/adjectives", listOf(Card("en vacker", "beautiful", "ex"))),
                    dueCount = 4,
                    newCount = 12,
                    progress = 0.46f,
                ),
                HomeViewModel.DeckUi(
                    Deck("core/common-nouns", List(26) { Card("ord$it", "word$it", "") }),
                    dueCount = 0,
                    newCount = 26,
                    progress = 0f,
                ),
                HomeViewModel.DeckUi(
                    Deck("rivstart/kapitel-01", List(26) { Card("sv$it", "en$it", "") }),
                    dueCount = 2,
                    newCount = 0,
                    progress = 1f,
                ),
            ),
            onStudyDeck = {},
            onStudyAll = {},
        )
    }
}