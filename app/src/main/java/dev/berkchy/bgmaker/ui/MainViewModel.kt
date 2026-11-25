package dev.berkchy.bgmaker.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.berkchy.bgmaker.core.ConversionConfig
import dev.berkchy.bgmaker.core.ConversionEngine
import dev.berkchy.bgmaker.core.ConversionRequest
import dev.berkchy.bgmaker.core.ConversionResult
import dev.berkchy.bgmaker.data.AppSettings
import dev.berkchy.bgmaker.data.SettingsRepository
import dev.berkchy.bgmaker.util.ImageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SelectedImage(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val bitmap: Bitmap,
)

data class ConversionProgress(
    val stage: String,
    val current: Int,
    val total: Int,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    var settings by mutableStateOf(repository.load())
        private set

    var selectedImage by mutableStateOf<SelectedImage?>(null)
        private set

    var progress by mutableStateOf<ConversionProgress?>(null)
        private set

    var result by mutableStateOf<ConversionResult?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var status by mutableStateOf<String?>(null)
        private set

    private var job: Job? = null

    fun outputRoot(): File = repository.outputRoot(settings)

    fun baseDir(): File = repository.baseDir()

    fun updateSettings(block: (AppSettings) -> AppSettings) {
        settings = block(settings)
        repository.save(settings)
    }

    fun selectImage(uri: Uri): Boolean {
        val context = getApplication<Application>()
        val bitmap = runCatching { ImageLoader.decode(context, uri) }.getOrNull()
        if (bitmap == null) {
            status = "That image could not be read. Try another one."
            return false
        }
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        selectedImage = SelectedImage(
            uri = uri,
            name = ImageLoader.displayName(context, uri),
            sizeBytes = ImageLoader.size(context, uri),
            bitmap = bitmap,
        )
        error = null
        result = null
        status = null
        return true
    }

    fun showStatus(message: String) {
        status = message
    }

    fun clearStatus() {
        status = null
    }

    fun startOver() {
        job?.cancel()
        progress = null
        result = null
        error = null
        selectedImage = null
    }

    fun convert() {
        val image = selectedImage ?: return
        error = null
        result = null
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val main = viewModelScope
            try {
                val config = ConversionConfig(
                    resolution = settings.resolution,
                    mode = settings.mode,
                    namePrefix = settings.prefix.trim().ifBlank { "bg" },
                    outputRoot = outputRoot(),
                    backupEnabled = settings.backup,
                )
                val outcome = ConversionEngine().convert(
                    ConversionRequest(image.name, image.bitmap, config),
                    onProgress = { stage, current, total ->
                        main.launch(Dispatchers.Main.immediate) {
                            progress = ConversionProgress(stage, current, total)
                        }
                    },
                    isCancelled = { job?.isCancelled == true },
                )
                withContext(Dispatchers.Main) {
                    result = outcome
                    progress = null
                }
            } catch (_: CancellationException) {
                withContext(Dispatchers.Main) { progress = null }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    progress = null
                    error = exception.message ?: "Conversion failed"
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        progress = null
    }
}
