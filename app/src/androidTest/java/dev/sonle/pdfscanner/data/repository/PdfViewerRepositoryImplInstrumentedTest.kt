package dev.sonle.pdfscanner.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sonle.pdfscanner.TestPdfFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.Dispatchers

@RunWith(AndroidJUnit4::class)
class PdfViewerRepositoryImplInstrumentedTest {

    @Test
    fun openAndRender_shouldSucceed_onDevice() = runTest {
        val repository = PdfViewerRepositoryImpl(Dispatchers.IO)
        val pdf = File.createTempFile("viewer_integration", ".pdf")
        try {
            TestPdfFactory.writeMinimalPdf(pdf)
            val opened = repository.openDocument(pdf.absolutePath)
            assertTrue(opened.isSuccess)
            assertEquals(1, opened.getOrThrow().pageCount)

            val bitmapResult = repository.renderPage(0, targetWidthPx = 480)
            assertTrue(bitmapResult.isSuccess)
            assertTrue(bitmapResult.getOrThrow().width > 0)
        } finally {
            repository.close()
            pdf.delete()
        }
    }
}
