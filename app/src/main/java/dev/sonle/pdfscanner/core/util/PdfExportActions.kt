package dev.sonle.pdfscanner.core.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import java.io.File
import java.io.IOException

/**
 * Android-specific actions for an exported PDF (share, email, save to public storage).
 */
object PdfExportActions {

    const val MIME_PDF = "application/pdf"
    const val MIME_EMAIL = "message/rfc822"

    fun resolveFileUri(context: Context, absolutePath: String): Result<Uri> {
        val file = File(absolutePath)
        if (!file.exists() || !file.isFile) {
            return Result.failure(IOException("PDF file not found"))
        }
        return runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }
    }

    fun sharePdf(context: Context, exported: ExportedPdf, chooserTitle: String): Result<Unit> {
        val uri = resolveFileUri(context, exported.absolutePath).getOrElse { return Result.failure(it) }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_PDF
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, exported.fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return launchChooser(context, sendIntent, chooserTitle)
    }

    fun sharePdfFiles(
        context: Context,
        absolutePaths: List<String>,
        chooserTitle: String
    ): Result<Unit> {
        if (absolutePaths.isEmpty()) {
            return Result.failure(IllegalArgumentException("No files to share"))
        }
        if (absolutePaths.size == 1) {
            val path = absolutePaths.first()
            val fileName = File(path).name
            return sharePdf(
                context,
                ExportedPdf(fileName = fileName, absolutePath = path, fileSizeBytes = File(path).length()),
                chooserTitle
            )
        }
        val uris = ArrayList<Uri>()
        absolutePaths.forEach { path ->
            val uri = resolveFileUri(context, path).getOrElse { return Result.failure(it) }
            uris.add(uri)
        }
        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = MIME_PDF
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return launchChooser(context, sendIntent, chooserTitle)
    }

    fun openWithPdf(context: Context, exported: ExportedPdf, chooserTitle: String): Result<Unit> {
        val uri = resolveFileUri(context, exported.absolutePath).getOrElse { return Result.failure(it) }
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME_PDF)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchChooser(context, viewIntent, chooserTitle)
    }

    fun sharePdfViaEmail(context: Context, exported: ExportedPdf, chooserTitle: String): Result<Unit> {
        val uri = resolveFileUri(context, exported.absolutePath).getOrElse { return Result.failure(it) }
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_EMAIL
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, exported.fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return launchChooser(context, emailIntent, chooserTitle)
    }

    fun saveToDownloads(context: Context, exported: ExportedPdf): Result<Unit> {
        val source = File(exported.absolutePath)
        if (!source.exists()) {
            return Result.failure(IOException("PDF file not found"))
        }
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, exported.fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, MIME_PDF)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val targetUri = resolver.insert(collection, values)
                ?: throw IOException("Unable to create download entry")
            resolver.openOutputStream(targetUri)?.use { output ->
                source.inputStream().use { input -> input.copyTo(output) }
            } ?: throw IOException("Unable to write PDF to Downloads")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val published = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                resolver.update(targetUri, published, null, null)
            }
        }
    }

    private fun launchChooser(
        context: Context,
        sendIntent: Intent,
        chooserTitle: String
    ): Result<Unit> = runCatching {
        val chooser = Intent.createChooser(sendIntent, chooserTitle)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
