package dev.sonle.pdfscanner.presentation.features.scanner.util

import android.graphics.Bitmap

object CaptureBitmapScaler {
    private const val MAX_EDGE_PX = 1080
    private const val FREEZE_PREVIEW_MAX_EDGE_PX = 640

    fun downscaleForFreezePreview(bitmap: Bitmap): Bitmap {
        return downscale(bitmap, FREEZE_PREVIEW_MAX_EDGE_PX)
    }

    fun downscaleForOverlay(bitmap: Bitmap): Bitmap {
        return downscale(bitmap, MAX_EDGE_PX)
    }

    private fun downscale(bitmap: Bitmap, maxEdgePx: Int): Bitmap {
        val maxEdge = maxOf(bitmap.width, bitmap.height)
        if (maxEdge <= maxEdgePx) return bitmap
        val scale = maxEdgePx.toFloat() / maxEdge
        val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
