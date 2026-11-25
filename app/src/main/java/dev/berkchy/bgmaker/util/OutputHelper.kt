package dev.berkchy.bgmaker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object OutputHelper {

    fun zipDirectory(source: File): File? {
        if (!source.exists() || !source.isDirectory) return null
        val zip = File(source.parentFile, "bgmaker-output.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zip))).use { output ->
            source.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = source.toPath().relativize(file.toPath())
                    .toString()
                    .replace(File.separatorChar, '/')
                output.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(output) }
                output.closeEntry()
            }
        }
        return zip
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun shareIntent(context: Context, file: File): Intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uriFor(context, file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun createDocumentIntent(suggestedName: String): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, suggestedName)
        }

    fun writeToUri(context: Context, uri: Uri, source: File) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            source.inputStream().use { it.copyTo(output) }
        }
    }
}
