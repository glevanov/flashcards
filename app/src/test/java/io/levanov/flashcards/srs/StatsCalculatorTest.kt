package io.levanov.flashcards.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    private val today = LocalDate.of(2026, 1, 1)

    @Test
    fun `empty input yields zeros`() {
        val stats = StatsCalculator.aggregate(emptyList())
        assertEquals(0, stats.total)
        assertEquals(0, stats.newCount)
        assertEquals(0, stats.learningCount)
        assertEquals(0, stats.masteredCount)
        assertEquals(0, stats.seen)
        assertEquals(0, stats.correct)
        assertEquals(0, stats.accuracyPercent)
        assertEquals(6, stats.boxCounts.size)
        for (b in 1..6) assertEquals(0, stats.boxCounts.getValue(b))
    }

    @Test
    fun `new cards counted separately and not in boxes`() {
        val states = List(3) { LeitnerEngine.newState(today) }
        val stats = StatsCalculator.aggregate(states)
        assertEquals(3, stats.total)
        assertEquals(3, stats.newCount)
        assertEquals(0, stats.learningCount)
        assertEquals(0, stats.masteredCount)
        for (b in 1..6) assertEquals(0, stats.boxCounts.getValue(b))
    }

    @Test
    fun `box distribution counts each box`() {
        val states = (1..6).map { box ->
            LeitnerEngine.grade(LeitnerEngine.newState(today), true, today).copy(box = box)
        }
        val stats = StatsCalculator.aggregate(states)
        assertEquals(6, stats.total)
        assertEquals(0, stats.newCount)
        for (b in 1..6) assertEquals(1, stats.boxCounts.getValue(b))
    }

    @Test
    fun `mastered is box 6 only`() {
        val states = listOf(4, 5, 6, 6).map { box ->
            LeitnerEngine.newState(today).copy(isNew = false, box = box)
        }
        val stats = StatsCalculator.aggregate(states)
        assertEquals(2, stats.masteredCount)
        assertEquals(2, stats.learningCount)
    }

    @Test
    fun `learning excludes new and mastered`() {
        val states = listOf(
            LeitnerEngine.newState(today),                                             // new
            LeitnerEngine.newState(today),                                             // new
            LeitnerEngine.newState(today).copy(isNew = false, box = 2),                 // learning
            LeitnerEngine.newState(today).copy(isNew = false, box = 5),                 // learning
            LeitnerEngine.newState(today).copy(isNew = false, box = 6),                 // mastered
        )
        val stats = StatsCalculator.aggregate(states)
        assertEquals(2, stats.newCount)
        assertEquals(2, stats.learningCount)
        assertEquals(1, stats.masteredCount)
    }

    @Test
    fun `accuracy percent floors and guards zero`() {
        val zeroSeen = LeitnerEngine.newState(today)
        assertEquals(0, StatsCalculator.aggregate(listOf(zeroSeen)).accuracyPercent)

        val oneOfThree = LeitnerEngine.newState(today).copy(isNew = false, seen = 3, correct = 1)
        assertEquals(33, StatsCalculator.aggregate(listOf(oneOfThree)).accuracyPercent)
    }

    @Test
    fun `seen and correct accumulate over states`() {
        val s1 = LeitnerEngine.newState(today).copy(isNew = false, seen = 5, correct = 4)
        val s2 = LeitnerEngine.newState(today).copy(isNew = false, seen = 3, correct = 1)
        val stats = StatsCalculator.aggregate(listOf(s1, s2))
        assertEquals(8, stats.seen)
        assertEquals(5, stats.correct)
        assertEquals(62, stats.accuracyPercent)
    }

    @Test
    fun `out-of-range box is clamped`() {
        val box9 = LeitnerEngine.newState(today).copy(isNew = false, box = 9)
        val box0 = LeitnerEngine.newState(today).copy(isNew = false, box = 0)
        val stats = StatsCalculator.aggregate(listOf(box9, box0))
        assertEquals(1, stats.boxCounts.getValue(6))   // 9 clamped to 6
        assertEquals(1, stats.boxCounts.getValue(1))   // 0 clamped to 1
    }

    @Test
    fun `boxCounts always has exactly keys 1 to 6`() {
        val states = (1..6).map { rawBox ->
            LeitnerEngine.newState(today).copy(isNew = false, box = rawBox % 7 + 1)
        }
        val stats = StatsCalculator.aggregate(states)
        assertEquals((1..6).toSet(), stats.boxCounts.keys)
        // Also verify with an empty input
        val emptyStats = StatsCalculator.aggregate(emptyList())
        assertEquals((1..6).toSet(), emptyStats.boxCounts.keys)
    }
}