package io.levanov.flashcards.ui

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val DIRECTION_SV_EN = "sv_en"
    const val DIRECTION_EN_SV = "en_sv"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun study(deck: String?, reversed: Boolean): String {
        val direction = if (reversed) DIRECTION_EN_SV else DIRECTION_SV_EN
        return if (deck != null) {
            "study?deck=${Uri.encode(deck)}&direction=$direction"
        } else {
            "study?direction=$direction"
        }
    }
}
