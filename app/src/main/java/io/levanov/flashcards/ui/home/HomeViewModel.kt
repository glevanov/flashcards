package io.levanov.flashcards.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.DeckStateProvider
import io.levanov.flashcards.data.SrsRepository
import io.levanov.flashcards.data.db.FlashcardsDatabase
import io.levanov.flashcards.srs.LeitnerEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val stateProvider = DeckStateProvider(
        DeckRepository(app.assets),
        SrsRepository(FlashcardsDatabase.get(app).cardStateDao()),
    )

    data class DeckUi(
        val deck: Deck,
        val dueCount: Int,
        val newCount: Int,
        val progress: Float,
    )

    val decks: StateFlow<List<DeckUi>?> = stateProvider.observe()
        .map { deckStates ->
            val today = LocalDate.now()
            deckStates.map { ds ->
                val total = ds.deck.cards.size
                var dueCount = 0
                var newCount = 0
                for (st in ds.states) {
                    if (st.isNew) newCount++
                    else if (LeitnerEngine.isDue(st, today)) dueCount++
                }
                val progress = if (total == 0) 0f else (total - newCount).toFloat() / total
                DeckUi(ds.deck, dueCount, newCount, progress)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application) }
        }
    }
}
