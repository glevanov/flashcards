package io.levanov.flashcards.data

import io.levanov.flashcards.data.db.CardStateDao
import io.levanov.flashcards.data.db.CardStateEntity
import io.levanov.flashcards.data.db.toCardState
import io.levanov.flashcards.data.db.toEntity
import io.levanov.flashcards.srs.CardState
import io.levanov.flashcards.srs.LeitnerEngine
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class SrsRepository(private val dao: CardStateDao) {

    fun observeStates(): Flow<List<CardStateEntity>> = dao.observeAll()

    suspend fun statesFor(keys: List<Pair<String, String>>): Map<String, CardState> {
        // keys: (cardKey, deckName); rows missing from DB get newState(today)
        val today = LocalDate.now()
        val stored = dao.getByKeys(keys.map { it.first }).associateBy { it.key }
        return keys.associate { (key, _) ->
            key to (stored[key]?.toCardState() ?: LeitnerEngine.newState(today))
        }
    }

    suspend fun save(key: String, deck: String, state: CardState) =
        dao.upsertAll(listOf(toEntity(key, deck, state)))
}