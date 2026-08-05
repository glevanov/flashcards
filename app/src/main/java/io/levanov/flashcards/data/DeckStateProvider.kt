package io.levanov.flashcards.data

import io.levanov.flashcards.data.db.toCardState
import io.levanov.flashcards.srs.CardState
import io.levanov.flashcards.srs.LeitnerEngine
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** A deck paired with the current SRS state of each of its cards, in card order. */
data class DeckWithStates(
    val deck: Deck,
    val states: List<CardState>,
)

/**
 * Joins the bundled decks with persisted SRS state, filling in fresh
 * [LeitnerEngine.newState] values for never-reviewed cards. Shared by the
 * home and stats screens so deck/state aggregation lives in one place.
 */
class DeckStateProvider(
    private val deckRepo: DeckRepository,
    private val srsRepo: SrsRepository,
) {

    fun observe(): Flow<List<DeckWithStates>> = combine(
        deckRepo.observeDecks(),
        srsRepo.observeStates(),
    ) { decks, entities ->
        val today = LocalDate.now()
        val stateByKey = entities.associateBy { it.key }
        decks.map { deck ->
            DeckWithStates(
                deck = deck,
                states = deck.cards.map { card ->
                    stateByKey[deck.cardKey(card.swedish)]?.toCardState()
                        ?: LeitnerEngine.newState(today)
                },
            )
        }
    }
}
