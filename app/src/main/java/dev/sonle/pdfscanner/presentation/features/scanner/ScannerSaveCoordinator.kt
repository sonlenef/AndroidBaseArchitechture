package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.domain.model.ExportedPdf
import dev.sonle.pdfscanner.domain.model.PdfExportError
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository
import dev.sonle.pdfscanner.domain.repository.pdfExportErrorOrNull
import dev.sonle.pdfscanner.domain.usecase.AddRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannedPage
import dev.sonle.pdfscanner.presentation.features.scanner.util.ScannedPageExportMapper.toExportPages
import timber.log.Timber

/**
 * Handles PDF export and recent-scan metadata persistence for the scanner flow.
 */
class ScannerSaveCoordinator(
    private val savePdfUseCase: SavePdfUseCase,
    private val addRecentScanUseCase: AddRecentScanUseCase,
    private val appSettingsRepository: AppSettingsRepository
) {
    sealed class SaveOutcome {
        data class Success(val exported: ExportedPdf) : SaveOutcome()
        data class Failure(val error: PdfExportError) : SaveOutcome()
        data class MetadataFailed(val exported: ExportedPdf, val cause: Throwable) : SaveOutcome()
    }

    suspend fun save(
        pages: List<ScannedPage>,
        fileName: String
    ): SaveOutcome {
        if (pages.isEmpty()) return SaveOutcome.Failure(PdfExportError.EmptyPages)

        val exportPages = pages.toExportPages()
        val pageCount = pages.size
        val quality = appSettingsRepository.currentSettings().pdfOutputQuality
        val result = savePdfUseCase(exportPages, fileName, quality)

        return result.fold(
            onSuccess = { exported ->
                runCatching {
                    addRecentScanUseCase(
                        RecentScan(
                            id = 0,
                            fileName = exported.fileName,
                            filePath = exported.absolutePath,
                            pageCount = pageCount,
                            fileSizeBytes = exported.fileSizeBytes,
                            savedAt = System.currentTimeMillis()
                        )
                    )
                }.fold(
                    onSuccess = { SaveOutcome.Success(exported) },
                    onFailure = { error ->
                        Timber.e(error, "Failed to record recent scan metadata")
                        SaveOutcome.MetadataFailed(exported, error)
                    }
                )
            },
            onFailure = { error ->
                val exportError = result.pdfExportErrorOrNull()
                    ?: PdfExportError.IoFailure(error.message)
                SaveOutcome.Failure(exportError)
            }
        )
    }
}
