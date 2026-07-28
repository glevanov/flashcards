package io.levanov.flashcards.srs

import java.time.LocalDate
import kotlin.random.Random

object SessionBuilder {

    /**
     * Builds a session queue: all due cards (shuffled) + up to [newLimit] new
     * cards (shuffled), due first then new — matching flashcards.py cmd_review.
     *
     * @param candidates pairs of (stable key, state) for every card in scope
     * @param today injected for testability
     * @param random inject seeded Random in tests for deterministic shuffles
     * @return ordered list of keys forming the session queue
     */
    fun <K> buildSession(
        candidates: List<Pair<K, CardState>>,
        today: LocalDate,
        newLimit: Int = DEFAULT_NEW_CARDS,
        random: Random = Random,
    ): List<K> {
        val due = candidates.filter { (_, st) -> LeitnerEngine.isDue(st, today) }
        val new = candidates.filter { (_, st) -> st.isNew }
        return (due.shuffled(random) + new.shuffled(random).take(newLimit)).map { it.first }
    }

    const val DEFAULT_NEW_CARDS = 10
}