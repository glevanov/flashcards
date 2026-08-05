package io.levanov.flashcards.ui.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.SettingsRepository
import io.levanov.flashcards.data.SrsRepository
import io.levanov.flashcards.data.db.FlashcardsDatabase
import io.levanov.flashcards.srs.CardState
import io.levanov.flashcards.srs.LeitnerEngine
import io.levanov.flashcards.srs.SessionBuilder
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SessionCard(
    val key: String,
    val deck: String,
    val swedish: String,
    val english: String,
    val example: String,
)

data class StudyUiState(
    val queue: List<SessionCard>? = null,
    val index: Int = 0,
    val flipped: Boolean = false,
    val answerSeen: Boolean = false,
    val gradedCount: Int = 0,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val reversed: Boolean = false,
    val ttsEnabled: Boolean = true,
)

class StudyViewModel(
    app: Application,
    val deckName: String?,
    reversed: Boolean,
) : AndroidViewModel(app) {

    private val deckRepo = DeckRepository(app.assets)
    private val srsRepo = SrsRepository(FlashcardsDatabase.get(app).cardStateDao())
    private val settingsRepo = SettingsRepository(app)

    private val _uiState = MutableStateFlow(StudyUiState(reversed = reversed))
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private val stateByKey = mutableMapOf<String, CardState>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val appSettings = settingsRepo.settings.first()
            val decks = deckRepo.loadDecks()
            val scoped = if (deckName != null) decks.filter { it.name == deckName } else decks
            val keys = scoped.flatMap { d -> d.cards.map { c -> d.cardKey(c.swedish) to d.name } }
            val states = srsRepo.statesFor(keys)
            stateByKey.putAll(states)
            val today = LocalDate.now()
            val candidates = scoped.flatMap { d ->
                d.cards.map { c ->
                    val key = d.cardKey(c.swedish)
                    key to states.getValue(key)
                }
            }
            val queueKeys = SessionBuilder.buildSession(
                candidates = candidates,
                today = today,
                newLimit = appSettings.newCardsPerDay,
            )
            val keyToCard = scoped.flatMap { d ->
                d.cards.map { c ->
                val key = d.cardKey(c.swedish)
                key to SessionCard(
                    key = key,
                        deck = d.name,
                        swedish = c.swedish,
                        english = c.english,
                        example = c.example,
                    )
                }
            }.toMap()
            val queue = queueKeys.mapNotNull { keyToCard[it] }
            _uiState.update { it.copy(queue = queue, ttsEnabled = appSettings.ttsEnabled) }
        }
    }

    fun toggleFlip() {
        _uiState.update {
            it.copy(flipped = !it.flipped, answerSeen = it.answerSeen || !it.flipped)
        }
    }

    fun grade(correct: Boolean) {
        val current = _uiState.value
        val queue = current.queue ?: return
        if (current.finished || current.index >= queue.size) return
        if (!current.answerSeen) return
        val card = queue[current.index]
        val today = LocalDate.now()
        val state = stateByKey.getValue(card.key)
        val graded = LeitnerEngine.grade(state, correct, today)
        stateByKey[card.key] = graded
        viewModelScope.launch(Dispatchers.IO) {
            srsRepo.save(card.key, card.deck, graded)
        }
        val newIndex = current.index + 1
        val finished = newIndex >= queue.size
        _uiState.update {
            it.copy(
                index = newIndex,
                flipped = false,
                answerSeen = false,
                gradedCount = it.gradedCount + 1,
                correctCount = it.correctCount + if (correct) 1 else 0,
                finished = finished,
            )
        }
    }

    fun skip() {
        val current = _uiState.value
        val queue = current.queue ?: return
        if (current.finished || current.index >= queue.size) return
        val card = queue[current.index]
        val newQueue = queue.toMutableList().apply {
            removeAt(current.index)
            add(card)
        }
        _uiState.update { it.copy(queue = newQueue, flipped = false, answerSeen = false) }
    }

    companion object {
        fun factory(deckName: String?, reversed: Boolean) = viewModelFactory {
            initializer {
                StudyViewModel(
                    this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application,
                    deckName,
                    reversed,
                )
            }
        }
    }
}
