package io.levanov.flashcards.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.DeckStateProvider
import io.levanov.flashcards.data.SrsRepository
import io.levanov.flashcards.data.db.FlashcardsDatabase
import io.levanov.flashcards.srs.DeckStats
import io.levanov.flashcards.srs.StatsCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val stateProvider = DeckStateProvider(
        DeckRepository(app.assets),
        SrsRepository(FlashcardsDatabase.get(app).cardStateDao()),
    )

    data class DeckStatsUi(
        val deckName: String,
        val displayName: String,
        val group: String,
        val stats: DeckStats,
    )

    data class StatsUiState(
        val global: DeckStats,
        val decks: List<DeckStatsUi>,
    )

    val uiState: StateFlow<StatsUiState?> = stateProvider.observe()
        .map { deckStates ->
            val perDeck = deckStates.map { ds ->
                DeckStatsUi(
                    deckName = ds.deck.name,
                    displayName = ds.deck.displayName,
                    group = ds.deck.group,
                    stats = StatsCalculator.aggregate(ds.states),
                )
            }
            val globalStats = StatsCalculator.aggregate(deckStates.flatMap { it.states })
            StatsUiState(global = globalStats, decks = perDeck)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                StatsViewModel(
                    this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application,
                )
            }
        }
    }
}
