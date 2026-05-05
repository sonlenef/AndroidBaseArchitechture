package dev.sonle.pdfscanner.domain.usecase

import android.graphics.Bitmap
import dev.sonle.pdfscanner.domain.repository.ScannerRepository
import java.io.File

class SavePdfUseCase(
    private val repository: ScannerRepository
) {
    suspend operator fun invoke(bitmaps: List<Bitmap>, fileName: String): Result<File> {
        return repository.generatePdf(bitmaps, fileName)
    }
}
