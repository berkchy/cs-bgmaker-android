package dev.berkchy.bgmaker.core

import java.io.File

object LayoutWriter {

    fun build(grid: Grid, prefix: String, backgroundDir: String = Presets.DEFAULT_BACKGROUND_DIR): String {
        val sb = StringBuilder()
        sb.append("resolution   ").append(grid.width).append("   ").append(grid.height).append('\n')
        sb.append('\n')
        for (cell in grid.cells()) {
            sb.append("resource/").append(backgroundDir).append('/')
                .append(prefix).append('_').append(cell.row).append('_').append(cell.col)
                .append(".tga")
                .append("      scaled      ")
                .append(cell.x).append("   ").append(cell.y).append('\n')
            sb.append('\n')
        }
        return sb.toString()
    }

    fun write(
        outputRoot: File,
        grid: Grid,
        prefix: String,
        backgroundDir: String = Presets.DEFAULT_BACKGROUND_DIR,
    ): List<File> {
        outputRoot.mkdirs()
        val text = build(grid, prefix, backgroundDir)
        return Presets.LAYOUT_FILES.map { name ->
            File(outputRoot, name).apply { writeText(text, Charsets.UTF_8) }
        }
    }
}
