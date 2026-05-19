package dev.sonle.pdfscanner.presentation.features.scanner.util

import android.graphics.Bitmap
import dev.sonle.pdfscanner.domain.model.ExportPage
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannedPage

object ScannedPageExportMapper {
    fun List<ScannedPage>.toExportPages(): List<ExportPage> =
        map { it.processedBitmap.toExportPage() }

    private fun Bitmap.toExportPage(): ExportPage {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return ExportPage(width = width, height = height, pixels = pixels)
    }
}
