package io.levanov.flashcards.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    onStudy: (deck: String?, reversed: Boolean) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val decks by vm.decks.collectAsStateWithLifecycle()
    when (val d = decks) {
        null -> LoadingScaffold(modifier)
        else -> DeckListContent(
            deckUis = d,
            onStudy = onStudy,
            onOpenStats = onOpenStats,
            onOpenSettings = onOpenSettings,
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

private data class StudyTarget(val deck: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckListContent(
    deckUis: List<HomeViewModel.DeckUi>,
    onStudy: (deck: String?, reversed: Boolean) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalDue = deckUis.sumOf { it.dueCount }
    val totalNew = deckUis.sumOf { it.newCount }
    val allAvailable = totalDue + totalNew > 0
    var studyTarget by remember { mutableStateOf<StudyTarget?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Svenska Flashcards") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                scrollBehavior = scrollBehavior,
                actions = {
                    var menuOpen by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Stats") },
                            onClick = { menuOpen = false; onOpenStats() },
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { menuOpen = false; onOpenSettings() },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (allAvailable) studyTarget = StudyTarget(null) },
                modifier = Modifier.then(if (!allAvailable) Modifier.alpha(0.5f) else Modifier),
                icon = {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                },
                text = {
                    Text(
                        "Study all · $totalDue due · $totalNew new",
                        maxLines = 1,
                    )
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 88.dp,
            ),
        ) {
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
                            modifier = Modifier.clickable { studyTarget = StudyTarget(deck.name) },
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

    studyTarget?.let { target ->
        DirectionSheet(
            onPick = { reversed ->
                onStudy(target.deck, reversed)
                studyTarget = null
            },
            onDismiss = { studyTarget = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectionSheet(
    onPick: (reversed: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Study direction",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(8.dp))
        ListItem(
            modifier = Modifier.clickable { onPick(false) },
            headlineContent = { Text("Swedish to English") },
        )
        ListItem(
            modifier = Modifier.clickable { onPick(true) },
            headlineContent = { Text("English to Swedish") },
        )
        Spacer(Modifier.height(24.dp))
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
            onStudy = { _, _ -> },
            onOpenStats = {},
            onOpenSettings = {},
        )
    }
}