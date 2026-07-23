package io.levanov.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.ui.home.DeckListScreen
import io.levanov.flashcards.ui.theme.FlashcardsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardsTheme {
                val repository = remember { DeckRepository(assets) }
                val decks by produceState<List<Deck>?>(initialValue = null) {
                    value = withContext(Dispatchers.IO) { repository.loadDecks() }
                }
                when (val d = decks) {
                    null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    else -> DeckListScreen(decks = d)
                }
            }
        }
    }
}