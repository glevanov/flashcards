package io.levanov.flashcards

import android.app.Application
import io.levanov.flashcards.ui.study.TtsManager

class FlashcardsApplication : Application() {

    // Lazy: the engine (and its bundled model) only loads when something
    // actually accesses the manager — i.e. the study screen.
    val ttsManager: TtsManager by lazy { TtsManager(this) }
}
