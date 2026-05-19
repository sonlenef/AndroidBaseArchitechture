package dev.sonle.pdfscanner.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
import dev.sonle.pdfscanner.data.mapper.ExportPageBitmapMapper.toBitmap
import dev.sonle.pdfscanner.domain.model.ExportPage
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import dev.sonle.pdfscanner.domain.model.PdfExportError
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.repository.PdfExportException
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ScannerRepositoryImpl(
    private val context: Context
) : ScannerRepository {

    override suspend fun generatePdf(
        pages: List<ExportPage>,
        fileName: String,
        quality: PdfOutputQuality
    ): Result<ExportedPdf> = withContext(Dispatchers.IO) {
        if (pages.isEmpty()) {
            return@withContext Result.failure(
                PdfExportException(PdfExportError.EmptyPages)
            )
        }
        try {
            val pdfDocument = PdfDocument()
            pages.forEachIndexed { index, page ->
                val sourceBitmap = page.toBitmap()
                val bitmap = sourceBitmap.scaledForQuality(quality)
                if (bitmap !== sourceBitmap) {
                    sourceBitmap.recycle()
                }
                try {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        bitmap.width,
                        bitmap.height,
                        index + 1
                    ).create()
                    val pdfPage = pdfDocument.startPage(pageInfo)
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)
                } finally {
                    bitmap.recycle()
                }
            }

            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val resolvedName = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                fileName
            } else {
                "$fileName.pdf"
            }
            val file = File(directory, resolvedName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Result.success(
                ExportedPdf(
                    fileName = file.name,
                    absolutePath = file.absolutePath,
                    fileSizeBytes = file.length()
                )
            )
        } catch (e: Exception) {
            Result.failure(
                PdfExportException(PdfExportError.IoFailure(e.message))
            )
        }
    }

    private fun Bitmap.scaledForQuality(quality: PdfOutputQuality): Bitmap {
        if (quality == PdfOutputQuality.HIGH) return this
        val maxDimension = STANDARD_MAX_PDF_DIMENSION
        val longestSide = maxOf(width, height)
        if (longestSide <= maxDimension) return this
        val scale = maxDimension.toFloat() / longestSide
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    companion object {
        private const val STANDARD_MAX_PDF_DIMENSION = 1600
    }
}
