package io.levanov.flashcards.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LeitnerEngineTest {

    private val today = LocalDate.of(2026, 1, 15)

    @Test
    fun correctFromBox1MovesToBox2DueTomorrow() {
        val state = LeitnerEngine.newState(today).copy(box = 1, isNew = false)
        val graded = LeitnerEngine.grade(state, correct = true, today = today)
        assertEquals(2, graded.box)
        assertEquals(today.plusDays(1), graded.due)
        assertEquals(1, graded.seen)
        assertEquals(1, graded.correct)
        assertFalse(graded.isNew)
    }

    @Test
    fun correctAtBox6StaysBox6DueIn35Days() {
        val state = CardState(box = 6, due = today, seen = 5, correct = 5, isNew = false)
        val graded = LeitnerEngine.grade(state, correct = true, today = today)
        assertEquals(6, graded.box)
        assertEquals(today.plusDays(35), graded.due)
        assertEquals(6, graded.seen)
        assertEquals(6, graded.correct)
    }

    @Test
    fun wrongFromBox4ResetsToBox1DueToday() {
        val state = CardState(box = 4, due = today, seen = 3, correct = 3, isNew = false)
        val graded = LeitnerEngine.grade(state, correct = false, today = today)
        assertEquals(1, graded.box)
        assertEquals(today, graded.due) // interval[1] = 0
        assertEquals(4, graded.seen)
        assertEquals(3, graded.correct) // unchanged
    }

    @Test
    fun wrongOnNewCardResetsToBox1SeenOneCorrectZero() {
        val state = LeitnerEngine.newState(today)
        val graded = LeitnerEngine.grade(state, correct = false, today = today)
        assertEquals(1, graded.box)
        assertEquals(today, graded.due)
        assertEquals(1, graded.seen)
        assertEquals(0, graded.correct)
        assertFalse(graded.isNew)
    }

    @Test
    fun intervalMapCoversBoxes1To6Exactly() {
        assertEquals(6, LeitnerEngine.BOX_INTERVALS.size)
        assertEquals(0, LeitnerEngine.BOX_INTERVALS[1])
        assertEquals(1, LeitnerEngine.BOX_INTERVALS[2])
        assertEquals(3, LeitnerEngine.BOX_INTERVALS[3])
        assertEquals(7, LeitnerEngine.BOX_INTERVALS[4])
        assertEquals(16, LeitnerEngine.BOX_INTERVALS[5])
        assertEquals(35, LeitnerEngine.BOX_INTERVALS[6])
    }

    @Test
    fun isDueBehavior() {
        val newCard = LeitnerEngine.newState(today)
        assertFalse(LeitnerEngine.isDue(newCard, today))

        val dueToday = CardState(box = 1, due = today, isNew = false)
        assertTrue(LeitnerEngine.isDue(dueToday, today))

        val dueYesterday = CardState(box = 1, due = today.minusDays(1), isNew = false)
        assertTrue(LeitnerEngine.isDue(dueYesterday, today))

        val dueTomorrow = CardState(box = 2, due = today.plusDays(1), isNew = false)
        assertFalse(LeitnerEngine.isDue(dueTomorrow, today))
    }

    @Test
    fun newStateIsBox1NewWithZeroCounters() {
        val state = LeitnerEngine.newState(today)
        assertEquals(1, state.box)
        assertTrue(state.isNew)
        assertEquals(0, state.seen)
        assertEquals(0, state.correct)
        assertEquals(today, state.due)
    }
}