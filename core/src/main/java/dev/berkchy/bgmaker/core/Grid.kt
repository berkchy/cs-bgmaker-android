package dev.berkchy.bgmaker.core

const val GRID_STEP = 256

data class TileCell(
    val row: Int,
    val col: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class Grid(
    val width: Int,
    val height: Int,
    val colStarts: IntArray,
    val colWidths: IntArray,
    val rowStarts: IntArray,
    val rowHeights: IntArray,
) {
    val cols: Int get() = colWidths.size
    val rows: Int get() = rowHeights.size
    val tileCount: Int get() = cols * rows

    fun cells(): List<TileCell> = buildList {
        rowStarts.forEachIndexed { r, y ->
            colStarts.forEachIndexed { c, x ->
                add(TileCell(r, c, x, y, colWidths[c], rowHeights[r]))
            }
        }
    }
}

fun buildGrid(width: Int, height: Int, step: Int = GRID_STEP): Grid {
    require(width > 0 && height > 0) { "Resolution must be positive" }
    val nCols = (width + step - 1) / step
    val nRows = (height + step - 1) / step
    val colWidths = IntArray(nCols) { i ->
        if (i == nCols - 1) width - step * (nCols - 1) else step
    }
    val rowHeights = IntArray(nRows) { i ->
        if (i == nRows - 1) height - step * (nRows - 1) else step
    }
    return Grid(
        width = width,
        height = height,
        colStarts = IntArray(nCols) { it * step },
        colWidths = colWidths,
        rowStarts = IntArray(nRows) { it * step },
        rowHeights = rowHeights,
    )
}
