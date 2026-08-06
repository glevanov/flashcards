package io.levanov.flashcards.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.levanov.flashcards.BuildConfig
import io.levanov.flashcards.data.AppSettings
import io.levanov.flashcards.data.DeckRepository
import io.levanov.flashcards.data.SettingsRepository
import io.levanov.flashcards.data.backup.BackupError
import io.levanov.flashcards.data.backup.BackupRepository
import io.levanov.flashcards.data.backup.ImportPreview
import io.levanov.flashcards.data.db.FlashcardsDatabase
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val busy: Boolean = false,
    val pendingImport: ImportPreview? = null,
    val message: String? = null,
)

data class AppInfo(
    val versionName: String,
    val isDebug: Boolean,
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)
    private val backupRepo = BackupRepository(
        app,
        FlashcardsDatabase.get(app).cardStateDao(),
        DeckRepository(app.assets),
        repo,
    )

    val appInfo: AppInfo = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
        isDebug = BuildConfig.DEBUG,
    )

    val settings: StateFlow<AppSettings?> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setNewCardsPerDay(value: Int) {
        viewModelScope.launch { repo.setNewCardsPerDay(value) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch { repo.setTtsEnabled(enabled) }
    }

    fun onExportUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            try {
                val n = backupRepo.exportTo(uri)
                _uiState.update { it.copy(busy = false, message = "Exported $n cards") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(busy = false, message = backupErrorMessage(e)) }
            }
        }
    }

    fun onImportUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            try {
                val preview = backupRepo.previewImport(uri)
                _uiState.update { it.copy(busy = false, pendingImport = preview) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(busy = false, message = backupErrorMessage(e)) }
            }
        }
    }

    fun confirmImport() {
        val preview = _uiState.value.pendingImport ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, pendingImport = null) }
            try {
                backupRepo.applyImport(preview)
                val n = preview.cards.size
                val skipped = preview.skipped
                val message = if (skipped > 0) {
                    "Imported $n cards ($skipped skipped)"
                } else {
                    "Imported $n cards"
                }
                _uiState.update { it.copy(busy = false, message = message) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(busy = false, message = backupErrorMessage(e)) }
            }
        }
    }

    fun dismissImport() {
        _uiState.update { it.copy(pendingImport = null) }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun backupErrorMessage(e: Throwable): String = when (e) {
        is BackupError.UnsupportedVersion -> "This backup was created by a newer app version"
        is BackupError.InvalidFormat -> "Not a valid backup file"
        is IOException -> "Could not read or write the file"
        else -> "Backup failed"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application,
                )
            }
        }
    }
}
