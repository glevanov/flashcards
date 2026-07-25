package io.levanov.flashcards

import android.app.Application
import io.levanov.flashcards.ui.study.TtsManager

class FlashcardsApplication : Application() {

    val ttsManager: TtsManager by lazy { TtsManager(this) }

    override fun onCreate() {
        super.onCreate()
        ttsManager
    }

    override fun onTerminate() {
        ttsManager.shutdown()
        super.onTerminate()
    }
}
