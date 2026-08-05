package io.levanov.flashcards.data

data class Deck(
    /** Path under assets/vocab minus .csv, e.g. "core/adjectives". */
    val name: String,
    val cards: List<Card>,
) {
    /** Folder part, e.g. "core". Empty string if deck sits at vocab root. */
    val group: String get() = name.substringBefore('/', "")
    /** File part, e.g. "adjectives". */
    val displayName: String get() = name.substringAfterLast('/')

    /**
     * Stable SRS key for a card: "<deck>::<swedish>". This is the Room primary
     * key — renaming a deck or a Swedish term orphans its SRS state.
     */
    fun cardKey(swedish: String): String = "$name$KEY_SEPARATOR$swedish"

    companion object {
        const val KEY_SEPARATOR = "::"
    }
}