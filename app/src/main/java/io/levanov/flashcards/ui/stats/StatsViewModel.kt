package io.levanov.flashcards.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.SrsRepository
import io.levanov.flashcards.data.db.FlashcardsDatabase
import io.levanov.flashcards.data.db.toCardState
import io.levanov.flashcards.srs.CardState
import io.levanov.flashcards.srs.LeitnerEngine
import io.levanov.flashcards.srs.StatsCalculator
import io.levanov.flashcards.srs.DeckStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val deckRepo = DeckRepository(app.assets)
    private val srsRepo = SrsRepository(FlashcardsDatabase.get(app).cardStateDao())

    data class DeckStatsUi(
        val deckName: String,        // full name, e.g. "core/adjectives"
        val displayName: String,     // "adjectives"
        val group: String,           // "core"
        val stats: DeckStats,
    )

    data class StatsUiState(
        val global: DeckStats,
        val decks: List<DeckStatsUi>,
    )

    val uiState: StateFlow<StatsUiState?> = combine(
        flow { emit(deckRepo.loadDecks()) }.flowOn(Dispatchers.IO),
        srsRepo.observeStates(),
    ) { decks, entities ->
        val today = LocalDate.now()
        val stateByKey = entities.associateBy { it.key }

        // Compute states per deck once, reuse for both per-deck and global.
        val statesByDeck: Map<String, List<CardState>> = decks.associate { deck ->
            val cardStates = deck.cards.map { c ->
                stateByKey["${deck.name}::${c.swedish}"]?.toCardState()
                    ?: LeitnerEngine.newState(today)
            }
            deck.name to cardStates
        }

        val perDeck = decks.map { deck ->
            DeckStatsUi(
                deckName = deck.name,
                displayName = deck.displayName,
                group = deck.group,
                stats = StatsCalculator.aggregate(statesByDeck.getValue(deck.name)),
            )
        }
        val globalStats = StatsCalculator.aggregate(statesByDeck.values.flatten())
        StatsUiState(global = globalStats, decks = perDeck)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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