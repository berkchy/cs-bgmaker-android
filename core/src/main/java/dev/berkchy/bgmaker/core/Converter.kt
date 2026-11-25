package dev.berkchy.bgmaker.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

object Converter {

    private fun software(bitmap: Bitmap): Bitmap =
        if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }

    fun prepare(source: Bitmap, mode: ResizeMode, target: Resolution): Bitmap {
        val input = software(source)
        val tw = target.width
        val th = target.height
        val out = Bitmap.createBitmap(tw, th, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.BLACK)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val sw = input.width.toFloat()
        val sh = input.height.toFloat()

        when (mode) {
            ResizeMode.STRETCH -> {
                canvas.drawBitmap(input, null, RectF(0f, 0f, tw.toFloat(), th.toFloat()), paint)
            }
            ResizeMode.COVER, ResizeMode.FIT -> {
                val scale = if (mode == ResizeMode.COVER) {
                    maxOf(tw / sw, th / sh)
                } else {
                    minOf(tw / sw, th / sh)
                }
                val nw = sw * scale
                val nh = sh * scale
                val left = (tw - nw) / 2f
                val top = (th - nh) / 2f
                canvas.drawBitmap(input, null, RectF(left, top, left + nw, top + nh), paint)
            }
        }
        return out
    }

    fun tile(canvas: Bitmap, cell: TileCell): Bitmap {
        val source = software(canvas)
        return Bitmap.createBitmap(source, cell.x, cell.y, cell.width, cell.height)
    }

    fun tileFileName(prefix: String, cell: TileCell): String =
        "${prefix}_${cell.row}_${cell.col}.tga"

    fun writeTga(file: File, bitmap: Bitmap) {
        val source = software(bitmap)
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        val header = ByteArray(18)
        header[2] = 2
        putShort(header, 12, w)
        putShort(header, 14, h)
        header[16] = 32
        header[17] = 0

        BufferedOutputStream(FileOutputStream(file)).use { out ->
            out.write(header)
            for (y in h - 1 downTo 0) {
                val row = y * w
                for (x in 0 until w) {
                    val argb = pixels[row + x]
                    out.write(argb and 0xFF)
                    out.write((argb shr 8) and 0xFF)
                    out.write((argb shr 16) and 0xFF)
                    out.write((argb shr 24) and 0xFF)
                }
            }
        }
    }

    private fun putShort(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xFF).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
