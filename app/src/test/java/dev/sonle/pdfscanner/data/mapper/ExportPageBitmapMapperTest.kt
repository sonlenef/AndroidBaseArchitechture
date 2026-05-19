package dev.sonle.pdfscanner.data.mapper

import android.graphics.Bitmap
import android.graphics.Color
import dev.sonle.pdfscanner.data.mapper.ExportPageBitmapMapper.toBitmap
import dev.sonle.pdfscanner.data.mapper.ExportPageBitmapMapper.toExportPage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExportPageBitmapMapperTest {

    @Test
    fun `toExportPage and toBitmap preserve dimensions`() {
        val bitmap = Bitmap.createBitmap(12, 8, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        val page = bitmap.toExportPage()
        val restored = page.toBitmap()

        assertEquals(12, page.width)
        assertEquals(8, page.height)
        assertEquals(12, restored.width)
        assertEquals(8, restored.height)

        restored.recycle()
        bitmap.recycle()
    }
}
