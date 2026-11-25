package dev.berkchy.bgmaker.core

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun backup(outputRoot: File): File? {
        if (!outputRoot.exists()) return null
        val background = File(outputRoot, Presets.DEFAULT_BACKGROUND_DIR)
        val layouts = Presets.LAYOUT_FILES.map { File(outputRoot, it) }
        if (!background.exists() && layouts.none { it.exists() }) return null

        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val target = File(outputRoot, "_backup_$stamp")
        target.mkdirs()
        if (background.exists()) {
            background.renameTo(File(target, Presets.DEFAULT_BACKGROUND_DIR))
        }
        layouts.forEach { file ->
            if (file.exists()) file.renameTo(File(target, file.name))
        }
        return target
    }

    fun list(outputRoot: File): List<File> =
        outputRoot.listFiles { file -> file.isDirectory && file.name.startsWith("_backup_") }
            ?.sortedByDescending { it.name }
            ?: emptyList()

    fun restore(outputRoot: File, backup: File): Boolean {
        if (!backup.exists() || backup == outputRoot) return false
        backup(outputRoot)
        val sourceBackground = File(backup, Presets.DEFAULT_BACKGROUND_DIR)
        if (sourceBackground.exists()) {
            File(outputRoot, Presets.DEFAULT_BACKGROUND_DIR).deleteRecursively()
            sourceBackground.renameTo(File(outputRoot, Presets.DEFAULT_BACKGROUND_DIR))
        }
        Presets.LAYOUT_FILES.forEach { name ->
            val file = File(backup, name)
            if (file.exists()) {
                File(outputRoot, name).delete()
                file.renameTo(File(outputRoot, name))
            }
        }
        return true
    }

    fun delete(backup: File): Boolean = backup.deleteRecursively()
}
