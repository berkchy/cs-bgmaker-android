package dev.berkchy.bgmaker.core

import kotlin.coroutines.cancellation.CancellationException

class ConversionEngine {

    fun convert(
        request: ConversionRequest,
        onProgress: (stage: String, current: Int, total: Int) -> Unit,
        isCancelled: () -> Boolean = { false },
    ): ConversionResult {
        val config = request.config
        val grid = buildGrid(config.resolution.width, config.resolution.height)
        val total = grid.tileCount + 4
        var done = 0

        fun advance(stage: String) {
            done += 1
            onProgress(stage, done, total)
        }

        advance("Loading image")
        val canvas = Converter.prepare(request.source, config.mode, config.resolution)
        advance("Preparing ${config.resolution} canvas")

        val backupName = if (config.backupEnabled) {
            BackupManager.backup(config.outputRoot)?.name
        } else {
            null
        }
        advance(
            when {
                !config.backupEnabled -> "Backup skipped"
                backupName != null -> "Previous output backed up"
                else -> "No previous output to back up"
            }
        )

        val backgroundDir = File(config.outputRoot, config.backgroundDir)
        backgroundDir.mkdirs()
        val cells = grid.cells()
        cells.forEachIndexed { index, cell ->
            if (isCancelled()) {
                canvas.recycle()
                throw CancellationException("Conversion cancelled")
            }
            val tile = Converter.tile(canvas, cell)
            Converter.writeTga(File(backgroundDir, Converter.tileFileName(config.namePrefix, cell)), tile)
            tile.recycle()
            onProgress("Writing tile ${index + 1}/${cells.size}", done + index + 1, total)
        }
        done += cells.size
        canvas.recycle()

        LayoutWriter.write(config.outputRoot, grid, config.namePrefix, config.backgroundDir)
        advance("Writing layout files")
        onProgress("Done", total, total)

        return ConversionResult(
            imageName = request.imageName,
            resolution = config.resolution,
            grid = grid,
            mode = config.mode,
            outputRoot = config.outputRoot,
            backupName = backupName,
            tileCount = grid.tileCount,
            prefix = config.namePrefix,
        )
    }
}
