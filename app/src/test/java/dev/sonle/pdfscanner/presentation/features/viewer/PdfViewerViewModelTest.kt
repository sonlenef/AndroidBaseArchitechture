package dev.sonle.pdfscanner.presentation.features.viewer

import android.graphics.Bitmap
import android.graphics.Color
import app.cash.turbine.test
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.domain.model.PdfViewerDocument
import dev.sonle.pdfscanner.domain.model.PdfViewerError
import dev.sonle.pdfscanner.domain.repository.PdfViewerException
import dev.sonle.pdfscanner.domain.repository.PdfViewerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.P])
class PdfViewerViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: PdfViewerRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `uiState should expose document after successful open`() = runTest(dispatcher) {
        val document = PdfViewerDocument(
            fileName = "scan.pdf",
            absolutePath = "/tmp/scan.pdf",
            pageCount = 2,
            fileSizeBytes = 1024
        )
        coEvery { repository.openDocument(any()) } returns Result.success(document)
        coEvery { repository.renderPage(any(), any()) } returns Result.success(createBitmap())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("scan.pdf", state.fileName)
            assertEquals(2, state.pageCount)
            assertTrue(state.isReady)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should show error when open fails`() = runTest(dispatcher) {
        coEvery { repository.openDocument(any()) } returns Result.failure(
            PdfViewerException(PdfViewerError.FileNotFound)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(R.string.viewer_file_not_found, viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun `onPageSelected should trim cache and request adjacent pages`() = runTest(dispatcher) {
        val document = PdfViewerDocument("a.pdf", "/tmp/a.pdf", pageCount = 3, fileSizeBytes = 10)
        coEvery { repository.openDocument(any()) } returns Result.success(document)
        coEvery { repository.renderPage(any(), any()) } returns Result.success(createBitmap())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onGoToPage(1)
        advanceUntilIdle()

        coVerify { repository.releasePagesExcept(setOf(0, 1, 2)) }
        coVerify(atLeast = 1) { repository.renderPage(1, any()) }
        assertEquals(1, viewModel.uiState.value.currentPageIndex)
    }

    @Test
    fun `onShareClick should emit share effect`() = runTest(dispatcher) {
        val document = PdfViewerDocument("share.pdf", "/tmp/share.pdf", pageCount = 1, fileSizeBytes = 10)
        coEvery { repository.openDocument(any()) } returns Result.success(document)
        coEvery { repository.renderPage(any(), any()) } returns Result.success(createBitmap())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onShareClick()
            val effect = awaitItem()
            assertTrue(effect is PdfViewerUiEffect.Share)
            assertEquals("share.pdf", (effect as PdfViewerUiEffect.Share).exported.fileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(): PdfViewerViewModel =
        PdfViewerViewModel(
            filePath = "/tmp/share.pdf",
            pdfViewerRepository = repository,
            ioDispatcher = dispatcher
        )

    private fun createBitmap(): Bitmap =
        Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }
}
