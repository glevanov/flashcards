package io.levanov.flashcards.ui.study

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.pm.PackageInfoCompat
import com.k2fsa.sherpa.onnx.GeneratedAudio
import com.k2fsa.sherpa.onnx.GenerationConfig
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Wraps bundled Piper TTS for Swedish card pronunciation.
 *
 * Lifecycle: created with an application/activity context, asynchronously loads
 * the bundled model off the main thread, and exposes [available] as Compose
 * state. Public speak calls remain safe no-ops until loading succeeds.
 */
class TtsManager(context: Context) {

    var available by mutableStateOf(false)
        private set

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()

    private var tts: OfflineTts? = null
    private var audioTrack: AudioTrack? = null
    private var speakJob: Job? = null
    private var utteranceId: Long = 0

    init {
        scope.launch {
            loadBundledTts()
        }
    }

    /** Speaks [text] as Swedish, replacing any in-flight utterance. No-op if unavailable. */
    fun speak(text: String) {
        if (!available || text.isBlank()) return

        val currentTts = synchronized(lock) { tts } ?: return
        val requestId = synchronized(lock) {
            utteranceId += 1
            speakJob?.cancel()
            releaseAudioTrackLocked()
            utteranceId
        }

        val job = scope.launch {
            try {
                val callback = object : (FloatArray) -> Int {
                    override fun invoke(samples: FloatArray): Int = if (isCurrentRequest(requestId)) 1 else 0
                }
                val audio = currentTts.generateWithConfigAndCallback(
                    text = text,
                    config = GenerationConfig(sid = 0, speed = 0.75f, silenceScale = 0.2f),
                    callback = callback,
                )
                if (!isCurrentRequest(requestId) || !currentCoroutineContext().isActive) return@launch
                playAudio(audio, requestId)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to synthesize speech", t)
            }
        }

        synchronized(lock) {
            if (utteranceId == requestId) {
                speakJob = job
            } else {
                job.cancel()
            }
        }
    }

    fun shutdown() {
        available = false
        scope.cancel()

        val currentTts = synchronized(lock) {
            utteranceId += 1
            speakJob?.cancel()
            releaseAudioTrackLocked()
            val instance = tts
            tts = null
            instance
        }
        currentTts?.release()
    }

    private suspend fun loadBundledTts() {
        try {
            val runtimeDir = prepareRuntimeAssets()
            val instance = OfflineTts(config = offlineTtsConfig(runtimeDir))
            synchronized(lock) {
                tts = instance
            }
            withContext(Dispatchers.Main) {
                available = true
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize bundled Piper TTS", t)
            synchronized(lock) {
                tts?.release()
                tts = null
            }
            withContext(Dispatchers.Main) {
                available = false
            }
        }
    }

    private fun offlineTtsConfig(runtimeDir: File): OfflineTtsConfig {
        val modelPath = runtimeDir.resolve(MODEL_FILE_NAME)
        val tokensPath = runtimeDir.resolve(TOKENS_FILE_NAME)
        val dataDirPath = runtimeDir.resolve(ESPEAK_DIR_NAME)

        return OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(
                    model = modelPath.absolutePath,
                    tokens = tokensPath.absolutePath,
                    dataDir = dataDirPath.absolutePath,
                ),
                numThreads = 2,
                debug = false,
            ),
            maxNumSentences = 1,
        )
    }

    private fun prepareRuntimeAssets(): File {
        val runtimeDir = File(appContext.filesDir, RUNTIME_DIR_NAME)
        val versionMarker = File(runtimeDir, VERSION_MARKER_FILE_NAME)
        val currentVersion = installedAssetVersion()

        if (versionMarker.readTextOrNull() == currentVersion && requiredAssetsPresent(runtimeDir)) {
            return runtimeDir
        }

        runtimeDir.deleteRecursively()
        runtimeDir.mkdirs()
        copyAssetTree(appContext.assets, ASSET_DIR_NAME, runtimeDir)
        versionMarker.writeText(currentVersion)
        return runtimeDir
    }

    private fun requiredAssetsPresent(runtimeDir: File): Boolean =
        runtimeDir.resolve(MODEL_FILE_NAME).isFile &&
            runtimeDir.resolve(MODEL_CONFIG_FILE_NAME).isFile &&
            runtimeDir.resolve(TOKENS_FILE_NAME).isFile &&
            runtimeDir.resolve(ESPEAK_DIR_NAME).isDirectory

    private fun installedAssetVersion(): String {
        val packageInfo = packageInfo(appContext)
        return "${PackageInfoCompat.getLongVersionCode(packageInfo)}:${packageInfo.lastUpdateTime}"
    }

    private fun copyAssetTree(assetManager: AssetManager, assetPath: String, destination: File) {
        val children = assetManager.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            destination.parentFile?.mkdirs()
            assetManager.open(assetPath).use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return
        }

        destination.mkdirs()
        for (child in children) {
            copyAssetTree(assetManager, "$assetPath/$child", File(destination, child))
        }
    }

    private suspend fun playAudio(audio: GeneratedAudio, requestId: Long) {
        val track = createAudioTrack(audio)
        if (track == null) return

        synchronized(lock) {
            if (!isCurrentRequestLocked(requestId)) {
                track.release()
                return
            }
            audioTrack = track
        }

        try {
            val written = track.write(audio.samples, 0, audio.samples.size, AudioTrack.WRITE_BLOCKING)
            if (written <= 0) {
                Log.w(TAG, "AudioTrack.write() failed: $written")
                return
            }

            track.play()
            while (currentCoroutineContext().isActive && isCurrentRequest(requestId)) {
                val position = try {
                    track.playbackHeadPosition
                } catch (_: Throwable) {
                    break
                }
                if (position >= audio.samples.size) break
                delay(25)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to play synthesized speech", t)
        } finally {
            synchronized(lock) {
                if (audioTrack === track) {
                    audioTrack = null
                }
            }
            runCatching { track.stop() }
            track.release()
        }
    }

    private fun createAudioTrack(audio: GeneratedAudio): AudioTrack? {
        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(audio.sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        return runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(audio.samples.size * Float.SIZE_BYTES)
                .build()
        }.onFailure {
            Log.e(TAG, "Failed to create AudioTrack", it)
        }.getOrNull()
    }

    private fun isCurrentRequest(requestId: Long): Boolean = synchronized(lock) {
        isCurrentRequestLocked(requestId)
    }

    private fun isCurrentRequestLocked(requestId: Long): Boolean = utteranceId == requestId

    private fun releaseAudioTrackLocked() {
        audioTrack?.let { track ->
            runCatching { track.pause() }
            runCatching { track.stop() }
            runCatching { track.flush() }
            track.release()
        }
        audioTrack = null
    }

    private fun packageInfo(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

    private fun File.readTextOrNull(): String? =
        if (isFile) {
            try {
                readText()
            } catch (_: IOException) {
                null
            }
        } else {
            null
        }

    private companion object {
        private const val TAG = "TtsManager"
        private const val ASSET_DIR_NAME = "tts"
        private const val RUNTIME_DIR_NAME = "tts-runtime"
        private const val VERSION_MARKER_FILE_NAME = ".asset-version"
        private const val MODEL_FILE_NAME = "sv_SE-alma-medium.onnx"
        private const val MODEL_CONFIG_FILE_NAME = "sv_SE-alma-medium.onnx.json"
        private const val TOKENS_FILE_NAME = "tokens.txt"
        private const val ESPEAK_DIR_NAME = "espeak-ng-data"
    }
}
