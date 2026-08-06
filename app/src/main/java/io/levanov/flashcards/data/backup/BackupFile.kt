package io.levanov.flashcards.data.backup

import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.srs.LeitnerEngine
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BackupCard(
    val key: String,
    val box: Int,
    val dueEpochDay: Long,
    val seen: Int,
    val correct: Int,
    val isNew: Boolean,
)

@Serializable
data class BackupSettings(
    val newCardsPerDay: Int,
    val ttsEnabled: Boolean,
)

@Serializable
data class BackupFile(
    val version: Int,
    val app: String,
    val exportedAt: String,
    val appVersionName: String? = null,
    val settings: BackupSettings,
    val cards: List<BackupCard>,
)

sealed class BackupError(message: String) : Exception(message) {
    class UnsupportedVersion(val version: Int) :
        BackupError("Backup version $version is newer than supported version ${BackupCodec.CURRENT_VERSION}")

    class InvalidFormat(reason: String) : BackupError(reason)
}

object BackupCodec {
    const val CURRENT_VERSION = 1
    const val APP_ID = "io.levanov.flashcards"

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(
        states: List<BackupCard>,
        settings: BackupSettings,
        appVersionName: String? = null,
        exportedAt: Instant = Instant.now(),
    ): String = json.encodeToString(
        BackupFile(
            version = CURRENT_VERSION,
            app = APP_ID,
            exportedAt = exportedAt.toString(),
            appVersionName = appVersionName,
            settings = settings,
            cards = states,
        ),
    )

    fun decode(text: String): BackupFile {
        val file = try {
            json.decodeFromString<BackupFile>(text)
        } catch (e: Exception) {
            throw BackupError.InvalidFormat("Malformed JSON: ${e.message}")
        }
        if (file.version < 1) throw BackupError.InvalidFormat("Missing or invalid version")
        if (file.version > CURRENT_VERSION) throw BackupError.UnsupportedVersion(file.version)
        file.cards.forEach(::validateCard)
        validateSettings(file.settings)
        return file
    }

    private fun validateCard(card: BackupCard) {
        if (card.box !in 1..LeitnerEngine.MAX_BOX) {
            throw BackupError.InvalidFormat(
                "Card ${card.key}: box ${card.box} outside 1..${LeitnerEngine.MAX_BOX}",
            )
        }
        if (card.seen < 0 || card.correct < 0 || card.seen < card.correct) {
            throw BackupError.InvalidFormat("Card ${card.key}: invalid seen/correct counters")
        }
        if (Deck.KEY_SEPARATOR !in card.key) {
            throw BackupError.InvalidFormat("Card key without deck separator: ${card.key}")
        }
    }

    private fun validateSettings(settings: BackupSettings) {
        if (settings.newCardsPerDay !in 0..100) {
            throw BackupError.InvalidFormat("newCardsPerDay out of range: ${settings.newCardsPerDay}")
        }
    }
}

fun filterUnknownCards(cards: List<BackupCard>, knownKeys: Set<String>): Pair<List<BackupCard>, Int> {
    val known = cards.filter { it.key in knownKeys }
    return known to (cards.size - known.size)
}

fun suggestedBackupFileName(date: LocalDate = LocalDate.now()): String =
    "svenska-flashcards-backup-${DateTimeFormatter.BASIC_ISO_DATE.format(date)}.json"
