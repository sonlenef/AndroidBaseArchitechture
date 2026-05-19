package dev.sonle.pdfscanner.domain.usecase

import dev.sonle.pdfscanner.domain.model.ExportPage
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.repository.ScannerRepository

class SavePdfUseCase(
    private val repository: ScannerRepository
) {
    suspend operator fun invoke(
        pages: List<ExportPage>,
        fileName: String,
        quality: PdfOutputQuality = PdfOutputQuality.STANDARD
    ): Result<ExportedPdf> {
        return repository.generatePdf(pages, fileName, quality)
    }
}
