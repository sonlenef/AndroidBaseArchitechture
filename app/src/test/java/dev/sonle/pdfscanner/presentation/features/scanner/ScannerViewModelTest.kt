package dev.sonle.pdfscanner.presentation.features.scanner

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.sonle.pdfscanner.core.util.OpenCVScanner
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import io.mockk.coEvery

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private lateinit var viewModel: ScannerViewModel
    private lateinit var savePdfUseCase: SavePdfUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savePdfUseCase = mockk()
        viewModel = ScannerViewModel(savePdfUseCase)
        mockkObject(OpenCVScanner)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `processCapturedImage with valid document should emit Success`() = runTest {
        // GIVEN
        val mockFile = mockk<File>(relaxed = true)
        val mockBitmap = mockk<Bitmap>()
        val processedBitmap = mockk<Bitmap>()
        
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockBitmap
        every { OpenCVScanner.findDocumentCorners(any()) } returns listOf(mockk())
        every { OpenCVScanner.perspectiveTransform(any(), any()) } returns processedBitmap

        // WHEN
        viewModel.processCapturedImage(mockFile)

        // THEN
        viewModel.uiState.test {
            assertEquals(ScannerUiState.Idle, awaitItem())
            assertEquals(ScannerUiState.Processing, awaitItem())
            val successState = awaitItem() as ScannerUiState.Success
            assertEquals(processedBitmap, successState.bitmap)
        }
    }

    @Test
    fun `processCapturedImage with no document found should emit Error`() = runTest {
        // GIVEN
        val mockFile = mockk<File>(relaxed = true)
        val mockBitmap = mockk<Bitmap>()
        
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockBitmap
        every { OpenCVScanner.findDocumentCorners(any()) } returns null

        // WHEN
        viewModel.processCapturedImage(mockFile)

        // THEN
        viewModel.uiState.test {
            assertEquals(ScannerUiState.Idle, awaitItem())
            assertEquals(ScannerUiState.Processing, awaitItem())
            val errorState = awaitItem() as ScannerUiState.Error
            assertTrue(errorState.message.contains("No document found"))
        }
    }

    @Test
    fun `saveAsPdf should emit SaveSuccess when use case returns success`() = runTest {
        // GIVEN
        val mockFile = mockk<File>(relaxed = true)
        val mockBitmap = mockk<Bitmap>()
        val processedBitmap = mockk<Bitmap>()
        val outputPdfFile = mockk<File>()
        
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(any()) } returns mockBitmap
        every { OpenCVScanner.findDocumentCorners(any()) } returns listOf(mockk())
        every { OpenCVScanner.perspectiveTransform(any(), any()) } returns processedBitmap
        coEvery { savePdfUseCase(any(), any()) } returns Result.success(outputPdfFile)

        // WHEN
        viewModel.processCapturedImage(mockFile)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.saveAsPdf()

        // THEN
        viewModel.uiState.test {
            // Skip Idle, Processing, and Success states from initial capture
            assertEquals(ScannerUiState.Success(processedBitmap), awaitItem())
            assertEquals(ScannerUiState.Processing, awaitItem())
            val saveSuccessState = awaitItem() as ScannerUiState.SaveSuccess
            assertEquals(outputPdfFile, saveSuccessState.file)
        }
    }
}
