package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.core.analytics.AnalyticsManager
import dev.sonle.pdfscanner.core.analytics.FirebaseAnalyticsEvents
import dev.sonle.pdfscanner.core.crashlytics.CrashlyticsManager
import dev.sonle.pdfscanner.core.performance.PerformanceMonitor
import dev.sonle.pdfscanner.core.performance.PerformanceTraceHandle
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
    private val appSettingsRepository: AppSettingsRepository,
    private val analyticsManager: AnalyticsManager,
    private val crashlyticsManager: CrashlyticsManager,
    private val performanceMonitor: PerformanceMonitor
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
        val trace: PerformanceTraceHandle? =
            performanceMonitor.startUserActionTrace("save_pdf")
        val result = savePdfUseCase(exportPages, fileName, quality)

        return result.fold(
            onSuccess = { exported ->
                performanceMonitor.stopTrace(trace)
                analyticsManager.logEvent(
                    FirebaseAnalyticsEvents.PDF_SAVED,
                    mapOf(
                        FirebaseAnalyticsEvents.PARAM_PAGE_COUNT to pageCount,
                        FirebaseAnalyticsEvents.PARAM_FILE_SIZE_BYTES to exported.fileSizeBytes
                    )
                )
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
                        crashlyticsManager.recordException(error)
                        SaveOutcome.MetadataFailed(exported, error)
                    }
                )
            },
            onFailure = { error ->
                performanceMonitor.stopTrace(trace)
                val exportError = result.pdfExportErrorOrNull()
                    ?: PdfExportError.IoFailure(error.message)
                analyticsManager.logEvent(
                    FirebaseAnalyticsEvents.PDF_SAVE_FAILED,
                    mapOf(FirebaseAnalyticsEvents.PARAM_ERROR to (exportError.toString()))
                )
                crashlyticsManager.recordException(error)
                SaveOutcome.Failure(exportError)
            }
        )
    }
}
