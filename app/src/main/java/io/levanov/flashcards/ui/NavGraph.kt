package io.levanov.flashcards.ui

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val STUDY_ALL = "study"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    fun study(deck: String) = "study?deck=${Uri.encode(deck)}"
}