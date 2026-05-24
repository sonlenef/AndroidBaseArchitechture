package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.domain.model.PdfViewerError
import dev.sonle.pdfscanner.domain.repository.PdfViewerException
import dev.sonle.pdfscanner.domain.repository.pdfViewerErrorOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Fast unit tests that do not open [android.graphics.pdf.PdfRenderer] (can hang on some JVM hosts).
 * Rendering integration is covered on device via instrumented tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [android.os.Build.VERSION_CODES.P])
class PdfViewerRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: PdfViewerRepositoryImpl
    private lateinit var tempDir: File

    @Before
    fun setup() {
        repository = PdfViewerRepositoryImpl(dispatcher)
        tempDir = File(System.getProperty("java.io.tmpdir"), "pdf_viewer_repo_${System.nanoTime()}")
            .apply { mkdirs() }
    }

    @After
    fun tearDown() {
        repository.close()
        tempDir.deleteRecursively()
    }

    @Test
    fun `openDocument should fail when file is missing`() = runTest(dispatcher) {
        val result = repository.openDocument(File(tempDir, "missing.pdf").absolutePath)
        assertTrue(result.isFailure)
        assertEquals(PdfViewerError.FileNotFound, result.pdfViewerErrorOrNull())
    }

    @Test
    fun `openDocument should fail when path is a directory`() = runTest(dispatcher) {
        val result = repository.openDocument(tempDir.absolutePath)
        assertTrue(result.isFailure)
        assertEquals(PdfViewerError.FileNotFound, result.pdfViewerErrorOrNull())
    }

    @Test
    fun `renderPage should fail when document is not open`() = runTest(dispatcher) {
        val result = repository.renderPage(pageIndex = 0, targetWidthPx = 400)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PdfViewerException)
    }

    @Test
    fun `releasePagesExcept should not throw when cache is empty`() {
        repository.releasePagesExcept(setOf(0, 1))
    }
}
