package io.levanov.flashcards.ui.study

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Wraps Android TextToSpeech for Swedish card pronunciation.
 *
 * Lifecycle: created with an application/activity context, asynchronously
 * initialized (OnInitListener), must be closed with [shutdown] exactly once.
 * All public speak calls are safe no-ops until init succeeds and whenever the
 * Swedish voice data is missing — never throws, never shows UI.
 */
class TtsManager(context: Context) {

    private var ready = false
    private var swedishAvailable = false

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = this.tts.setLanguage(Locale("sv", "SE"))
            swedishAvailable = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            ready = true
        }
    }

    /** True once init finished AND a Swedish voice is usable. Drives 🔊 visibility. */
    val available: Boolean get() = ready && swedishAvailable

    /** Speaks [text] as Swedish, replacing any queued utterance. No-op if unavailable. */
    fun speak(text: String) {
        if (!available || text.isBlank()) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "flashcards-${text.hashCode()}")
    }

    fun shutdown() = tts.shutdown()
}