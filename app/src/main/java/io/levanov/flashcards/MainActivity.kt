package io.levanov.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import io.levanov.flashcards.ui.FlashcardsNavGraph
import io.levanov.flashcards.ui.study.LocalTtsManager
import io.levanov.flashcards.ui.theme.FlashcardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ttsManager = (application as FlashcardsApplication).ttsManager
            FlashcardsTheme {
                CompositionLocalProvider(LocalTtsManager provides ttsManager) {
                    FlashcardsNavGraph()
                }
            }
        }
    }
}
