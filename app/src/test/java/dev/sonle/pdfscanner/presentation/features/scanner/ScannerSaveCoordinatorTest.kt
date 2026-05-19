package dev.sonle.pdfscanner.presentation.features.scanner

import android.graphics.Bitmap
import dev.sonle.pdfscanner.domain.model.AppSettings
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import dev.sonle.pdfscanner.domain.model.PdfExportError
import dev.sonle.pdfscanner.domain.model.PdfOutputQuality
import dev.sonle.pdfscanner.domain.repository.AppSettingsRepository
import dev.sonle.pdfscanner.domain.usecase.AddRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannedPage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScannerSaveCoordinatorTest {

    private val savePdfUseCase: SavePdfUseCase = mockk()
    private val addRecentScanUseCase: AddRecentScanUseCase = mockk(relaxed = true)
    private val appSettingsRepository: AppSettingsRepository = mockk {
        every { currentSettings() } returns AppSettings.Default.copy(
            pdfOutputQuality = PdfOutputQuality.HIGH
        )
    }
    private val coordinator = ScannerSaveCoordinator(
        savePdfUseCase,
        addRecentScanUseCase,
        appSettingsRepository
    )

    @Test
    fun `save returns failure when pages are empty`() = runTest {
        val outcome = coordinator.save(emptyList(), "test.pdf")
        assertEquals(ScannerSaveCoordinator.SaveOutcome.Failure(PdfExportError.EmptyPages), outcome)
    }

    @Test
    fun `save returns success and records recent scan`() = runTest {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val page = ScannedPage(
            originalBitmap = bitmap,
            processedBitmap = bitmap,
            cropQuad = null,
            filter = ImageFilter.ORIGINAL,
            rotation = 0
        )
        val exported = ExportedPdf(
            fileName = "test.pdf",
            absolutePath = "/tmp/test.pdf",
            fileSizeBytes = 42
        )
        coEvery { savePdfUseCase(any(), any(), PdfOutputQuality.HIGH) } returns Result.success(exported)

        val outcome = coordinator.save(listOf(page), "test.pdf")

        assertTrue(outcome is ScannerSaveCoordinator.SaveOutcome.Success)
        coVerify(exactly = 1) { addRecentScanUseCase(any()) }
        bitmap.recycle()
    }
}
