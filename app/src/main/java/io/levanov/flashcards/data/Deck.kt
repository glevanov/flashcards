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
}