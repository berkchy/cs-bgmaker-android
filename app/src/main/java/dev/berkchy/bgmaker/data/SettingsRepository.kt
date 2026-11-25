package dev.berkchy.bgmaker.data

import android.content.Context
import android.os.Environment
import dev.berkchy.bgmaker.core.Presets
import dev.berkchy.bgmaker.core.Resolution
import dev.berkchy.bgmaker.core.ResizeMode
import dev.berkchy.bgmaker.theme.ThemeMode
import java.io.File

data class AppSettings(
    val outputFolderName: String = "1600x720",
    val resolution: Resolution = Resolution(1600, 720),
    val mode: ResizeMode = ResizeMode.COVER,
    val prefix: String = Presets.DEFAULT_PREFIX,
    val backup: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
)

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("bgmaker_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        outputFolderName = prefs.getString(KEY_FOLDER, "1600x720") ?: "1600x720",
        resolution = Resolution.parse(prefs.getString(KEY_RESOLUTION, "1600x720") ?: "1600x720")
            ?: Resolution(1600, 720),
        mode = ResizeMode.from(prefs.getString(KEY_MODE, "cover") ?: "cover"),
        prefix = prefs.getString(KEY_PREFIX, Presets.DEFAULT_PREFIX) ?: Presets.DEFAULT_PREFIX,
        backup = prefs.getBoolean(KEY_BACKUP, true),
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")
        }.getOrDefault(ThemeMode.SYSTEM),
        dynamicColor = prefs.getBoolean(KEY_DYNAMIC, false),
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_FOLDER, settings.outputFolderName)
            .putString(KEY_RESOLUTION, settings.resolution.toString())
            .putString(KEY_MODE, settings.mode.value)
            .putString(KEY_PREFIX, settings.prefix)
            .putBoolean(KEY_BACKUP, settings.backup)
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC, settings.dynamicColor)
            .apply()
    }

    fun baseDir(): File =
        File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: appContext.filesDir, "BGMaker")

    fun outputRoot(settings: AppSettings = load()): File {
        val name = settings.outputFolderName.trim().ifBlank { "1600x720" }
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(baseDir(), name)
    }

    private companion object {
        const val KEY_FOLDER = "output_folder"
        const val KEY_RESOLUTION = "resolution"
        const val KEY_MODE = "mode"
        const val KEY_PREFIX = "prefix"
        const val KEY_BACKUP = "backup"
        const val KEY_THEME = "theme"
        const val KEY_DYNAMIC = "dynamic"
    }
}
