package dev.sonle.pdfscanner.domain.repository

import android.graphics.Bitmap
import dev.sonle.pdfscanner.domain.model.PdfViewerDocument
import dev.sonle.pdfscanner.domain.model.PdfViewerError

/**
 * Opens PDF files and renders pages on demand with an internal bitmap cache.
 */
interface PdfViewerRepository {

    suspend fun openDocument(absolutePath: String): Result<PdfViewerDocument>

    suspend fun renderPage(
        pageIndex: Int,
        targetWidthPx: Int
    ): Result<Bitmap>

    fun releasePage(pageIndex: Int)

    fun releasePagesExcept(keepPageIndices: Set<Int>)

    fun close()
}

class PdfViewerException(val error: PdfViewerError) : Exception()

fun Result<*>.pdfViewerErrorOrNull(): PdfViewerError? =
    (exceptionOrNull() as? PdfViewerException)?.error
