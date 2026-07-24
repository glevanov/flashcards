package io.levanov.flashcards.ui.study

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Wraps Android TextToSpeech for Swedish card pronunciation.
 *
 * Lifecycle: created with an application/activity context, asynchronously
 * initialized (OnInitListener), must be closed with [shutdown] exactly once.
 * All public speak calls are safe no-ops until init succeeds and whenever the
 * Swedish voice data is missing — never throws, never shows UI.
 *
 * [available] is Compose state so the study screen recomposes and shows the
 * pronunciation button as soon as TTS initialization completes.
 */
class TtsManager(context: Context) {

    var available by mutableStateOf(false)
        private set

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = this.tts.setLanguage(Locale("sv", "SE"))
            available = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            available = false
        }
    }

    /** Speaks [text] as Swedish, replacing any queued utterance. No-op if unavailable. */
    fun speak(text: String) {
        if (!available || text.isBlank()) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "flashcards-${text.hashCode()}")
    }

    fun shutdown() {
        available = false
        tts.shutdown()
    }
}
