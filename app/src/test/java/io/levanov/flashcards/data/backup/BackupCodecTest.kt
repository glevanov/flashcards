package io.levanov.flashcards.data.backup

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    private val settings = BackupSettings(newCardsPerDay = 10, ttsEnabled = true)

    private val cards = listOf(
        BackupCard("core/adjectives::kall", box = 3, dueEpochDay = 20200, seen = 5, correct = 4, isNew = false),
        BackupCard("rivstart/kapitel-01::en fritid", box = 1, dueEpochDay = 20199, seen = 1, correct = 1, isNew = false),
    )

    @Test
    fun roundTripPreservesStatesAndSettings() {
        val json = BackupCodec.encode(cards, settings, appVersionName = "2")

        val file = BackupCodec.decode(json)

        assertEquals(BackupCodec.CURRENT_VERSION, file.version)
        assertEquals(BackupCodec.APP_ID, file.app)
        assertEquals("2", file.appVersionName)
        assertEquals(settings, file.settings)
        assertEquals(cards, file.cards)
        assertTrue(Instant.parse(file.exportedAt).epochSecond > 0)
    }

    @Test
    fun missingVersionRejected() {
        val e = assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(
                """
                {"app":"io.levanov.flashcards","exportedAt":"2026-06-29T12:34:56Z",
                 "settings":{"newCardsPerDay":10,"ttsEnabled":true},"cards":[]}
                """.trimIndent(),
            )
        }
        assertTrue(e.message.orEmpty().contains("Malformed JSON"))
    }

    @Test
    fun versionZeroRejected() {
        val json = validJson().replace("\"version\":1", "\"version\":0")
        assertThrows(BackupError.InvalidFormat::class.java) { BackupCodec.decode(json) }
    }

    @Test
    fun newerVersionRejected() {
        val json = validJson().replace("\"version\":1", "\"version\":99")
        val e = assertThrows(BackupError.UnsupportedVersion::class.java) { BackupCodec.decode(json) }
        assertEquals(99, e.version)
    }

    @Test
    fun unknownExtraFieldsTolerated() {
        val json = """
            {
              "version": 1,
              "app": "io.levanov.flashcards",
              "exportedAt": "2026-06-29T12:34:56Z",
              "appVersionName": "2",
              "futureTopLevelField": {"a": 1},
              "settings": {"newCardsPerDay": 10, "ttsEnabled": true, "futureSetting": false},
              "cards": [
                {"key": "core/adjectives::kall", "box": 3, "dueEpochDay": 20200,
                 "seen": 5, "correct": 4, "isNew": false, "futureCardField": "x"}
              ]
            }
            """.trimIndent()

        val file = BackupCodec.decode(json)

        assertEquals(1, file.cards.size)
        assertEquals("core/adjectives::kall", file.cards[0].key)
        assertEquals(settings, file.settings)
    }

    @Test
    fun malformedJsonRejected() {
        assertThrows(BackupError.InvalidFormat::class.java) { BackupCodec.decode("not json at all") }
        assertThrows(BackupError.InvalidFormat::class.java) { BackupCodec.decode("") }
    }

    @Test
    fun boxOutOfRangeRejected() {
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"box\":3", "\"box\":0"))
        }
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"box\":3", "\"box\":7"))
        }
    }

    @Test
    fun negativeCountersRejected() {
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"seen\":5", "\"seen\":-1"))
        }
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"correct\":4", "\"correct\":-4"))
        }
    }

    @Test
    fun seenBelowCorrectRejected() {
        // Card has seen=5, correct=4; seen=2 < correct=4 is impossible state.
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"seen\":5", "\"seen\":2"))
        }
    }

    @Test
    fun keyWithoutDeckSeparatorRejected() {
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"core/adjectives::kall\"", "\"kall\""))
        }
    }

    @Test
    fun settingsOutOfRangeRejected() {
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"newCardsPerDay\":10", "\"newCardsPerDay\":-1"))
        }
        assertThrows(BackupError.InvalidFormat::class.java) {
            BackupCodec.decode(validJson().replace("\"newCardsPerDay\":10", "\"newCardsPerDay\":101"))
        }
    }

    @Test
    fun emptyCardListRoundTrips() {
        val file = BackupCodec.decode(BackupCodec.encode(emptyList(), settings))
        assertTrue(file.cards.isEmpty())
        assertEquals(settings, file.settings)
    }

    @Test
    fun swedishCharactersSurviveRoundTrip() {
        val swedishCards = listOf(
            BackupCard("rivstart/kapitel-01::på gatan", box = 2, dueEpochDay = 20300, seen = 2, correct = 2, isNew = false),
            BackupCard("core/adjectives::hårig", box = 6, dueEpochDay = 20400, seen = 42, correct = 40, isNew = false),
            BackupCard("rivstart/kapitel-05::äpple", box = 1, dueEpochDay = 20250, seen = 0, correct = 0, isNew = true),
        )

        val file = BackupCodec.decode(BackupCodec.encode(swedishCards, settings))

        assertEquals(swedishCards, file.cards)
        assertEquals("på gatan", file.cards[0].key.substringAfter("::"))
    }

    // --- filterUnknownCards ---

    @Test
    fun unknownCardsAreFilteredAndCounted() {
        val knownKeys = setOf("core/adjectives::kall", "rivstart/kapitel-01::en fritid")
        val unknown = BackupCard("old/deck::försvunnen", box = 1, dueEpochDay = 1, seen = 0, correct = 0, isNew = true)

        val (known, skipped) = filterUnknownCards(cards + unknown, knownKeys)

        assertEquals(2, known.size)
        assertEquals(1, skipped)
    }

    @Test
    fun filterKeepsAllWhenAllKnown() {
        val knownKeys = cards.map { it.key }.toSet()
        val (known, skipped) = filterUnknownCards(cards, knownKeys)
        assertEquals(cards.size, known.size)
        assertEquals(0, skipped)
    }

    @Test
    fun filterSkipsAllWhenNoKnownKeys() {
        val (known, skipped) = filterUnknownCards(cards, emptySet())
        assertTrue(known.isEmpty())
        assertEquals(cards.size, skipped)
    }

    private fun validJson(): String = BackupCodec.encode(cards, settings)
}
