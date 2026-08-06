package io.levanov.flashcards.data.backup

import android.content.Context
import android.net.Uri
import io.levanov.flashcards.data.Deck
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.SettingsRepository
import io.levanov.flashcards.data.db.CardStateDao
import io.levanov.flashcards.data.db.CardStateEntity
import java.io.IOException
import kotlinx.coroutines.flow.first

data class ImportPreview(
    val cards: List<BackupCard>,
    val skipped: Int,
    val settings: BackupSettings,
)

class BackupRepository(
    private val appContext: Context,
    private val dao: CardStateDao,
    private val deckRepository: DeckRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun exportTo(uri: Uri): Int {
        val entities = dao.getAll()
        val settings = settingsRepository.settings.first()
        val json = BackupCodec.encode(
            states = entities.map(CardStateEntity::toBackupCard),
            settings = BackupSettings(settings.newCardsPerDay, settings.ttsEnabled),
            appVersionName = appVersionName(),
        )
        val out = appContext.contentResolver.openOutputStream(uri)
            ?: throw IOException("Cannot open $uri for writing")
        out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        return entities.size
    }

    suspend fun previewImport(uri: Uri): ImportPreview {
        val text = readAll(appContext.contentResolver.openInputStream(uri))
        val backup = BackupCodec.decode(text)
        val knownKeys = deckRepository.loadDecks()
            .flatMap { deck -> deck.cards.map { deck.cardKey(it.swedish) } }
            .toSet()
        val (cards, skipped) = filterUnknownCards(backup.cards, knownKeys)
        return ImportPreview(cards, skipped, backup.settings)
    }

    suspend fun applyImport(preview: ImportPreview) {
        dao.replaceAll(preview.cards.map(BackupCard::toEntity))
        settingsRepository.setNewCardsPerDay(preview.settings.newCardsPerDay)
        settingsRepository.setTtsEnabled(preview.settings.ttsEnabled)
    }

    suspend fun clearAllData() {
        dao.deleteAll()
        settingsRepository.reset()
    }

    private fun readAll(input: java.io.InputStream?): String {
        val stream = input ?: throw IOException("Cannot open file for reading")
        return stream.use { it.readBytes().toString(Charsets.UTF_8) }
    }

    private fun appVersionName(): String? = try {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
    } catch (e: Exception) {
        null
    }
}

private fun CardStateEntity.toBackupCard() = BackupCard(key, box, dueEpochDay, seen, correct, isNew)

private fun BackupCard.toEntity() = CardStateEntity(
    key = key,
    deck = key.substringBefore(Deck.KEY_SEPARATOR),
    box = box,
    dueEpochDay = dueEpochDay,
    seen = seen,
    correct = correct,
    isNew = isNew,
)
