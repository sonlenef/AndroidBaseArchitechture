package dev.sonle.pdfscanner.domain.repository

import dev.sonle.pdfscanner.domain.model.ExportPage
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import dev.sonle.pdfscanner.domain.model.PdfExportError
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality

interface ScannerRepository {
    suspend fun generatePdf(
        pages: List<ExportPage>,
        fileName: String,
        quality: PdfOutputQuality = PdfOutputQuality.STANDARD
    ): Result<ExportedPdf>
}

class PdfExportException(val error: PdfExportError) : Exception()

fun Result<ExportedPdf>.pdfExportErrorOrNull(): PdfExportError? =
    (exceptionOrNull() as? PdfExportException)?.error
