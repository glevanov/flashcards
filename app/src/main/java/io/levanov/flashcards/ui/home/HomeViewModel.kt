package io.levanov.flashcards.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.SrsRepository
import io.levanov.flashcards.data.db.FlashcardsDatabase
import io.levanov.flashcards.data.db.toCardState
import io.levanov.flashcards.srs.LeitnerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val deckRepo = DeckRepository(app.assets)
    private val srsRepo = SrsRepository(FlashcardsDatabase.get(app).cardStateDao())

    data class DeckUi(
        val deck: Deck,
        val dueCount: Int,
        val newCount: Int,
        val progress: Float,
    )

    val decks: StateFlow<List<DeckUi>?> = combine(
        flow { emit(deckRepo.loadDecks()) }.flowOn(Dispatchers.IO),
        srsRepo.observeStates(),
    ) { loadedDecks, states ->
        val today = LocalDate.now()
        val stateByKey = states.associateBy { it.key }
        loadedDecks.map { deck ->
            val cards = deck.cards
            val total = cards.size
            var dueCount = 0
            var newCount = 0
            for (card in cards) {
                val key = "${deck.name}::${card.swedish}"
                val st = stateByKey[key]?.toCardState() ?: LeitnerEngine.newState(today)
                if (st.isNew) newCount++ else if (LeitnerEngine.isDue(st, today)) dueCount++
            }
            val progress = if (total == 0) 0f else (total - newCount).toFloat() / total
            DeckUi(deck, dueCount, newCount, progress)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application) }
        }
    }
}