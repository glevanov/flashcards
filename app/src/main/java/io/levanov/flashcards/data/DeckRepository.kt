package io.levanov.flashcards.data

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DeckRepository(private val assets: AssetManager) {

    fun loadDecks(): List<Deck> =
        listCsvFiles(VOCAB_ROOT)
            .sorted()
            .map { path ->
                val name = path.removePrefix("$VOCAB_ROOT/").removeSuffix(".csv")
                assets.open(path).use { Deck(name, CsvParser.parse(it)) }
            }

    /** Decks as a one-shot flow on the IO dispatcher, for combining with SRS state. */
    fun observeDecks(): Flow<List<Deck>> =
        flow { emit(loadDecks()) }.flowOn(Dispatchers.IO)

    private fun listCsvFiles(dir: String): List<String> {
        val entries = assets.list(dir).orEmpty()
        return entries.flatMap { entry ->
            val path = "$dir/$entry"
            if (entry.endsWith(".csv")) listOf(path) else listCsvFiles(path)
        }
    }

    private companion object {
        const val VOCAB_ROOT = "vocab"
    }
}