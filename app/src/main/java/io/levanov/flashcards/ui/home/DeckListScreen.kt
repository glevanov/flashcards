package io.levanov.flashcards.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.levanov.flashcards.data.Card
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.ui.theme.FlashcardsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    decks: List<Deck>,
    modifier: Modifier = Modifier,
) {
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
        if (decks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No decks found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                val grouped = decks.groupBy { it.group }
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
                    items(items = groupDecks, key = { it.name }) { deck ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    deck.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${deck.cards.size} cards · — due · — new",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                            trailingContent = {
                                LinearProgressIndicator(
                                    progress = { 0f },
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
fun DeckListScreenPreview() {
    FlashcardsTheme {
        DeckListScreen(
            decks = listOf(
                Deck("core/adjectives", listOf(Card("en vacker", "beautiful", "ex"))),
                Deck("core/common-nouns", List(26) { Card("ord$it", "word$it", "") }),
                Deck("rivstart/kapitel-01", List(26) { Card("sv$it", "en$it", "") }),
                Deck("rivstart/kapitel-02", List(20) { Card("sv$it", "en$it", "") }),
                Deck("rivstart/kapitel-03", List(18) { Card("sv$it", "en$it", "") }),
            ),
        )
    }
}