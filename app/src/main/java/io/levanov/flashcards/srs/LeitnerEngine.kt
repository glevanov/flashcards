package io.levanov.flashcards.srs

import java.time.LocalDate

/** Review state of one card. Mirrors the Room entity but Android-free. */
data class CardState(
    val box: Int = 1,
    val due: LocalDate,
    val seen: Int = 0,
    val correct: Int = 0,
    val isNew: Boolean = true,
)

object LeitnerEngine {
    val BOX_INTERVALS: Map<Int, Int> = mapOf(1 to 0, 2 to 1, 3 to 3, 4 to 7, 5 to 16, 6 to 35)
    const val MAX_BOX = 6

    /** Grade a card: correct -> box+1 (cap 6); wrong -> box 1. due = today + interval[newBox]. */
    fun grade(state: CardState, correct: Boolean, today: LocalDate): CardState {
        val newBox = if (correct) minOf(MAX_BOX, state.box + 1) else 1
        return CardState(
            box = newBox,
            due = today.plusDays(BOX_INTERVALS.getValue(newBox).toLong()),
            seen = state.seen + 1,
            correct = state.correct + if (correct) 1 else 0,
            isNew = false,
        )
    }

    /** A card is due when not new and due <= today. New cards are never "due". */
    fun isDue(state: CardState, today: LocalDate): Boolean =
        !state.isNew && !state.due.isAfter(today)

    /** Fresh state for a never-reviewed card. */
    fun newState(today: LocalDate): CardState = CardState(due = today)
}