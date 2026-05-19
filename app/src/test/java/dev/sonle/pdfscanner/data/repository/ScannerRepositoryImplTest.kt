package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.domain.model.PdfExportError
import dev.sonle.pdfscanner.domain.repository.pdfExportErrorOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ScannerRepositoryImplTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `generatePdf should fail when pages are empty`() = runTest {
        val repository = ScannerRepositoryImpl(context)
        val result = repository.generatePdf(emptyList(), "empty.pdf")

        assertTrue(result.isFailure)
        assertEquals(PdfExportError.EmptyPages, result.pdfExportErrorOrNull())
    }
}
