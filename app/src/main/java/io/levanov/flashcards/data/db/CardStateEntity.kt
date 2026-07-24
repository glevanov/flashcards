package io.levanov.flashcards.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.levanov.flashcards.srs.CardState
import java.time.LocalDate

@Entity(tableName = "card_states")
data class CardStateEntity(
    /** "<deck>::<swedish>" — stable card key. */
    @PrimaryKey val key: String,
    val deck: String, // denormalized for per-deck count queries
    val box: Int,
    val dueEpochDay: Long,
    val seen: Int,
    val correct: Int,
    val isNew: Boolean,
)

fun CardStateEntity.toCardState(): CardState =
    CardState(box, LocalDate.ofEpochDay(dueEpochDay), seen, correct, isNew)

fun toEntity(key: String, deck: String, state: CardState): CardStateEntity =
    CardStateEntity(key, deck, state.box, state.due.toEpochDay(), state.seen, state.correct, state.isNew)