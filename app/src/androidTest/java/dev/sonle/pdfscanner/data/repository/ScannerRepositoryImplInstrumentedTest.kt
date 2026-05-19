package dev.sonle.pdfscanner.data.repository

import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.sonle.pdfscanner.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ScannerRepositoryImplInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun generatePdf_createsPdfFile_forValidPages() = runTest {
        val repository = ScannerRepositoryImpl(context)
        val result = repository.generatePdf(
            pages = listOf(TestData.solidExportPage(width = 64, height = 64)),
            fileName = "instrumented_scan.pdf"
        )

        assertTrue(result.isSuccess)
        val exported = result.getOrThrow()
        val file = File(exported.absolutePath)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        assertEquals("instrumented_scan.pdf", exported.fileName)
        file.delete()
    }

    @Test
    fun generatePdf_writesToDocumentsDirectory() = runTest {
        val repository = ScannerRepositoryImpl(context)
        val result = repository.generatePdf(
            pages = listOf(TestData.solidExportPage(width = 64, height = 64)),
            fileName = "instrumented_docs.pdf"
        )

        val exported = result.getOrThrow()
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        assertTrue(exported.absolutePath.startsWith(storageDir.absolutePath))
        File(exported.absolutePath).delete()
    }
}
