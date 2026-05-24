package dev.sonle.pdfscanner.core.util

import android.os.Build
import android.os.Environment
import dev.sonle.pdfscanner.domain.model.ExportedPdf
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class PdfExportActionsTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `resolveFileUri should fail when pdf does not exist`() {
        val result = PdfExportActions.resolveFileUri(context, "/tmp/missing.pdf")
        assertTrue(result.isFailure)
    }

    @Test
    fun `openWithPdf should fail when pdf does not exist`() {
        val exported = ExportedPdf(
            fileName = "missing.pdf",
            absolutePath = "/tmp/missing_open_with.pdf",
            fileSizeBytes = 0
        )
        val result = PdfExportActions.openWithPdf(context, exported, "Open")
        assertTrue(result.isFailure)
    }

    @Test
    fun `saveToDownloads should copy pdf into public downloads`() {
        val sourceDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!
        val source = File(sourceDir, "save_downloads_test.pdf")
        source.writeBytes(byteArrayOf(0x25, 0x50, 0x44, 0x46))

        val exported = ExportedPdf(
            fileName = source.name,
            absolutePath = source.absolutePath,
            fileSizeBytes = source.length()
        )

        val result = PdfExportActions.saveToDownloads(context, exported)

        assertTrue(result.isSuccess)
        source.delete()
    }
}
