package io.levanov.flashcards.ui

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val STUDY_ALL = "study"
    fun study(deck: String) = "study?deck=${Uri.encode(deck)}"
}