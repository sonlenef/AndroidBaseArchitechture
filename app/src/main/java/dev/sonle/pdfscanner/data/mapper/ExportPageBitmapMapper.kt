package dev.sonle.pdfscanner.data.mapper

import android.graphics.Bitmap
import dev.sonle.pdfscanner.domain.model.ExportPage

object ExportPageBitmapMapper {

    fun Bitmap.toExportPage(): ExportPage {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        return ExportPage(width = width, height = height, pixels = pixels)
    }

    fun ExportPage.toBitmap(): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
}
