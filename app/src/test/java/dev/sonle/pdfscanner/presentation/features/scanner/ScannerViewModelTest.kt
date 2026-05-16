package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import dev.sonle.pdfscanner.core.util.OpenCVScanner
import dev.sonle.pdfscanner.domain.usecase.AddRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.presentation.features.scanner.model.CaptureAnimationPhase
import dev.sonle.pdfscanner.presentation.features.scanner.model.MultiCaptureOverlayState
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private lateinit var viewModel: ScannerViewModel
    private lateinit var savePdfUseCase: SavePdfUseCase
    private lateinit var addRecentScanUseCase: AddRecentScanUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savePdfUseCase = mockk(relaxed = true)
        addRecentScanUseCase = mockk(relaxed = true)
        viewModel = ScannerViewModel(savePdfUseCase, addRecentScanUseCase)
        mockkObject(OpenCVScanner)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `togglePageMode should expose MULTI on Camera state`() = runTest {
        viewModel.togglePageMode()
        testDispatcher.scheduler.advanceUntilIdle()

        val camera = viewModel.uiState.value as ScannerUiState.Camera
        assertEquals(PageMode.MULTI, camera.pageMode)
    }

    @Test
    fun `beginMultiCaptureOverlay should start detecting phase and lock pipeline`() = runTest {
        viewModel.togglePageMode()
        viewModel.beginMultiCaptureOverlay()

        val active = viewModel.captureOverlay.value as MultiCaptureOverlayState.Active
        assertEquals(CaptureAnimationPhase.Detecting, active.phase)
        val camera = viewModel.uiState.value as ScannerUiState.Camera
        assertTrue(camera.isPipelineRunning)
    }

    @Test
    fun `shouldAutoCapture should return true once when stable`() {
        val quad = DocumentQuad(
            tl = NormalizedPoint(0.1f, 0.1f),
            tr = NormalizedPoint(0.9f, 0.1f),
            br = NormalizedPoint(0.9f, 0.9f),
            bl = NormalizedPoint(0.1f, 0.9f),
            confidence = 0.9f
        )
        viewModel.onDetectionUpdated(quad, 1f, true)

        assertTrue(viewModel.shouldAutoCapture())
        assertFalse(viewModel.shouldAutoCapture())
    }
}
