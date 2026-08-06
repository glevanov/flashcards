package io.levanov.flashcards.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BundledDecksTest {

    private val vocabDir = File("src/main/assets/vocab")

    @Test
    fun `every bundled deck parses with unique card keys`() {
        assertTrue("deck directory not found: ${vocabDir.absolutePath}", vocabDir.isDirectory)

        val csvFiles = vocabDir.walkTopDown().filter { it.extension == "csv" }.toList()
        assertTrue("no decks found under $vocabDir", csvFiles.isNotEmpty())

        val deckNames = csvFiles.map { file ->
            file.relativeTo(vocabDir).path.removeSuffix(".csv").replace(File.separatorChar, '/')
        }
        assertEquals("deck names must be unique", deckNames.size, deckNames.toSet().size)

        for ((file, name) in csvFiles.zip(deckNames)) {
            val cards = CsvParser.parse(file.inputStream())
            assertTrue("$name is empty", cards.isNotEmpty())
            for (card in cards) {
                assertFalse("$name: blank swedish term", card.swedish.isBlank())
                assertFalse("$name: blank english for '${card.swedish}'", card.english.isBlank())
            }
            val keys = cards.map { Deck(name, emptyList()).cardKey(it.swedish) }
            assertEquals("$name: duplicate swedish terms", keys.size, keys.toSet().size)
        }
    }
}
