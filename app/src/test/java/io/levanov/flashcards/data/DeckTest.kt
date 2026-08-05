package io.levanov.flashcards.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DeckTest {

    @Test
    fun `cardKey joins deck name and swedish term with separator`() {
        val deck = Deck("rivstart/kapitel-01", emptyList())
        assertEquals("rivstart/kapitel-01::en fritid", deck.cardKey("en fritid"))
    }

    @Test
    fun `cardKey preserves swedish characters`() {
        val deck = Deck("core/adjectives", emptyList())
        assertEquals("core/adjectives::lång", deck.cardKey("lång"))
    }
}
