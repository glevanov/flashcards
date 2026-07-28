package io.levanov.flashcards.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class SessionBuilderTest {

    private val today = LocalDate.of(2026, 1, 15)

    private fun newState() = LeitnerEngine.newState(today)
    private fun dueCard(box: Int = 1) = CardState(box = box, due = today, isNew = false)
    private fun dueTomorrow(box: Int = 2) = CardState(box = box, due = today.plusDays(1), isNew = false)

    @Test
    fun dueCardsComeBeforeNewCards() {
        val candidates = listOf(
            "k1" to newState(),
            "k2" to dueCard(),
            "k3" to newState(),
            "k4" to dueCard(),
        )
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 10, random = Random(42))
        val dueKeys = queue.take(2)
        val newKeys = queue.takeLast(2)
        assertTrue(dueKeys.containsAll(listOf("k2", "k4")))
        assertTrue(newKeys.containsAll(listOf("k1", "k3")))
    }

    @Test
    fun newCardsCappedAtNewLimit() {
        val candidates = (1..15).map { "k$it" to newState() }
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 10, random = Random(42))
        assertEquals(10, queue.size)
    }

    @Test
    fun newLimitZeroReturnsOnlyDue() {
        val candidates = listOf(
            "k1" to newState(),
            "k2" to newState(),
            "k3" to dueCard(),
        )
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 0, random = Random(42))
        assertEquals(listOf("k3"), queue)
    }

    @Test
    fun allNewScopeReturnsExactlyNewLimit() {
        val candidates = (1..20).map { "k$it" to newState() }
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 5, random = Random(42))
        assertEquals(5, queue.size)
    }

    @Test
    fun seededRandomProducesDeterministicQueue() {
        val candidates = (1..10).map { "k$it" to newState() }
        val q1 = SessionBuilder.buildSession(candidates, today, newLimit = 5, random = Random(42))
        val q2 = SessionBuilder.buildSession(candidates, today, newLimit = 5, random = Random(42))
        assertEquals(q1, q2)
    }

    @Test
    fun dueShufflePermutesOrder() {
        val candidates = (1..5).map { "k$it" to dueCard() }
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 0, random = Random(42))
        assertEquals(5, queue.size)
        assertNotEquals((1..5).map { "k$it" }, queue)
    }

    @Test
    fun emptyScopeReturnsEmptyQueue() {
        val queue = SessionBuilder.buildSession(emptyList<Pair<String, CardState>>(), today, newLimit = 10, random = Random(42))
        assertTrue(queue.isEmpty())
    }

    @Test
    fun notDueYetReviewedCardsAreExcluded() {
        val candidates = listOf(
            "k1" to dueTomorrow(),
            "k2" to dueTomorrow(box = 4),
        )
        val queue = SessionBuilder.buildSession(candidates, today, newLimit = 10, random = Random(42))
        assertTrue(queue.isEmpty())
    }
}