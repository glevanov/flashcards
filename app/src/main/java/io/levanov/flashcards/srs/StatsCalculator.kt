package io.levanov.flashcards.srs

/** Aggregated SRS statistics for one scope (one deck, or all decks). */
data class DeckStats(
    val total: Int,
    val newCount: Int,
    val learningCount: Int,          // reviewed, not mastered (boxes 1..5)
    val masteredCount: Int,          // box 6
    val boxCounts: Map<Int, Int>,    // boxes 1..6, every key present (0-filled)
    val seen: Int,
    val correct: Int,
) {
    /** Lifetime accuracy in percent 0..100; 0 when never seen. */
    val accuracyPercent: Int
        get() = if (seen == 0) 0 else correct * 100 / seen
}

object StatsCalculator {

    const val MASTERED_BOX = 6

    /**
     * Aggregates per-card states into [DeckStats]. Missing states (never
     * reviewed) must be passed in by the caller as [LeitnerEngine.newState]
     * values — the calculator never invents cards.
     *
     * Decisions:
     * - mastered = box == 6 exactly (box 5 is "learning"; the 35-day box is the
     *   terminal one). Bars show the full distribution, so no info is lost.
     * - box values outside 1..6 are clamped into 1..6 defensively (cannot
     *   happen via the engine; protects against hand-edited DB rows).
     */
    fun aggregate(states: Collection<CardState>): DeckStats {
        val boxes = (1..LeitnerEngine.MAX_BOX).associateWith { 0 }.toMutableMap()
        var new = 0
        var seen = 0
        var correct = 0
        for (st in states) {
            if (st.isNew) {
                new++
            } else {
                val box = st.box.coerceIn(1, LeitnerEngine.MAX_BOX)
                boxes[box] = boxes.getValue(box) + 1
            }
            seen += st.seen
            correct += st.correct
        }
        val mastered = boxes.getValue(MASTERED_BOX)
        return DeckStats(
            total = states.size,
            newCount = new,
            learningCount = states.size - new - mastered,
            masteredCount = mastered,
            boxCounts = boxes,
            seen = seen,
            correct = correct,
        )
    }
}