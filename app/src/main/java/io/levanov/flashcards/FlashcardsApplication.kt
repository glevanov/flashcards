package io.levanov.flashcards

import android.app.Application
import io.levanov.flashcards.ui.study.TtsManager

class FlashcardsApplication : Application() {

    val ttsManager: TtsManager by lazy { TtsManager(this) }

    // Touch the lazy property eagerly: the bundled model loads in the
    // background at startup so the first playback on the study screen is
    // instant. Tradeoff: the ~62MB TTS assets are copied/loaded every launch.
    override fun onCreate() {
        super.onCreate()
        ttsManager
    }
}
