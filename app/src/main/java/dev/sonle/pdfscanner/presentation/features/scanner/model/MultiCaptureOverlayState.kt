package dev.sonle.pdfscanner.presentation.features.scanner.model

import android.graphics.Bitmap
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad

sealed interface MultiCaptureOverlayState {
    data object Idle : MultiCaptureOverlayState

    data class Active(
        val sessionId: Long,
        val phase: CaptureAnimationPhase,
        val quad: DocumentQuad,
        val sourceBitmap: Bitmap? = null,
        val processedBitmap: Bitmap? = null
    ) : MultiCaptureOverlayState
}
