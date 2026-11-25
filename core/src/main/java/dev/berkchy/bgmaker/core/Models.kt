package dev.berkchy.bgmaker.core

import java.io.File

enum class ResizeMode(val value: String, val description: String) {
    COVER("cover", "Fill the frame, crop the overflow"),
    FIT("fit", "Keep the whole image, black bars"),
    STRETCH("stretch", "Force the exact aspect ratio"),
    ;

    companion object {
        fun from(value: String): ResizeMode = entries.firstOrNull { it.value == value } ?: COVER
    }
}

data class Resolution(val width: Int, val height: Int) {
    override fun toString(): String = "${width}x${height}"

    companion object {
        fun parse(raw: String): Resolution? {
            val parts = raw.trim().lowercase()
                .replace(" ", "x")
                .split("x")
                .filter { it.isNotBlank() }
            if (parts.size != 2) return null
            val w = parts[0].toIntOrNull() ?: return null
            val h = parts[1].toIntOrNull() ?: return null
            return if (w > 0 && h > 0) Resolution(w, h) else null
        }
    }
}

object Presets {
    val resolutions: List<Pair<Resolution, String>> = listOf(
        Resolution(1600, 720) to "CS 1.6",
        Resolution(1920, 1080) to "Full HD",
        Resolution(1366, 768) to "HD",
        Resolution(1024, 768) to "4:3",
        Resolution(800, 600) to "Legacy",
    )
    const val DEFAULT_PREFIX = "bg"
    const val DEFAULT_BACKGROUND_DIR = "background"
    val LAYOUT_FILES = listOf("BackgroundLayout.txt", "BackgroundLoadingLayout.txt")
}

data class ConversionConfig(
    val resolution: Resolution = Resolution(1600, 720),
    val mode: ResizeMode = ResizeMode.COVER,
    val namePrefix: String = Presets.DEFAULT_PREFIX,
    val outputRoot: File,
    val backgroundDir: String = Presets.DEFAULT_BACKGROUND_DIR,
    val backupEnabled: Boolean = true,
)

data class ConversionRequest(
    val imageName: String,
    val source: android.graphics.Bitmap,
    val config: ConversionConfig,
)

data class ConversionResult(
    val imageName: String,
    val resolution: Resolution,
    val grid: Grid,
    val mode: ResizeMode,
    val outputRoot: File,
    val backupName: String?,
    val tileCount: Int,
    val prefix: String,
)
