package dev.sonle.pdfscanner.presentation.features.scanner.model

import android.graphics.Bitmap
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad

/**
 * Represents a single scanned page with all its processing data.
 */
data class ScannedPage(
    /** Original captured bitmap before any processing */
    val originalBitmap: Bitmap,
    /** Bitmap after crop and filter applied */
    val processedBitmap: Bitmap,
    /** The crop quadrilateral used for perspective transform */
    val cropQuad: DocumentQuad?,
    /** The filter applied to this page */
    val filter: ImageFilter = ImageFilter.ORIGINAL,
    /** Rotation in degrees (0, 90, 180, 270) */
    val rotation: Int = 0
)
