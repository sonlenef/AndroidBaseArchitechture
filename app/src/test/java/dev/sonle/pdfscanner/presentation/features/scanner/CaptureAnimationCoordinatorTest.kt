package dev.sonle.pdfscanner.presentation.features.scanner

import android.graphics.Bitmap
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import dev.sonle.pdfscanner.core.scanner.processing.CropProcessor
import dev.sonle.pdfscanner.presentation.features.scanner.model.CaptureAnimationPhase
import dev.sonle.pdfscanner.presentation.features.scanner.model.MultiCaptureOverlayState
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureAnimationCoordinatorTest {

    private val quad = DocumentQuad(
        tl = NormalizedPoint(0.1f, 0.1f),
        tr = NormalizedPoint(0.9f, 0.1f),
        br = NormalizedPoint(0.9f, 0.9f),
        bl = NormalizedPoint(0.1f, 0.9f),
        confidence = 1f
    )

    @Test
    fun `isCaptureLocked is true only when overlay active`() {
        assertFalse(CaptureAnimationCoordinator.isCaptureLocked(MultiCaptureOverlayState.Idle))
        assertTrue(
            CaptureAnimationCoordinator.isCaptureLocked(
                MultiCaptureOverlayState.Active(
                    sessionId = 1L,
                    phase = CaptureAnimationPhase.Detecting,
                    quad = quad
                )
            )
        )
    }

    @Test
    fun `nextPhaseAfterUiStep advances detecting to lifting to flattening to flying`() {
        val active = MultiCaptureOverlayState.Active(
            sessionId = 1L,
            phase = CaptureAnimationPhase.Detecting,
            quad = quad
        )
        assertEquals(
            CaptureAnimationPhase.Lifting,
            CaptureAnimationCoordinator.nextPhaseAfterUiStep(CaptureAnimationPhase.Detecting, active)
        )

        val lifting = active.copy(phase = CaptureAnimationPhase.Lifting)
        assertEquals(
            CaptureAnimationPhase.Flattening,
            CaptureAnimationCoordinator.nextPhaseAfterUiStep(CaptureAnimationPhase.Lifting, lifting)
        )

        val flattening = active.copy(phase = CaptureAnimationPhase.Flattening)
        assertEquals(
            CaptureAnimationPhase.FlyingToStack,
            CaptureAnimationCoordinator.nextPhaseAfterUiStep(CaptureAnimationPhase.Flattening, flattening)
        )

        val flying = active.copy(phase = CaptureAnimationPhase.FlyingToStack)
        assertNull(
            CaptureAnimationCoordinator.nextPhaseAfterUiStep(CaptureAnimationPhase.FlyingToStack, flying)
        )
    }

    @Test
    fun `canStartFlattenAnimation requires processed bitmap`() {
        val sourceOnly = mockk<Bitmap>()
        val withoutProcessed = MultiCaptureOverlayState.Active(
            sessionId = 1L,
            phase = CaptureAnimationPhase.Lifting,
            quad = quad,
            sourceBitmap = sourceOnly
        )
        assertFalse(CaptureAnimationCoordinator.canStartFlattenAnimation(withoutProcessed))

        val processed = mockk<Bitmap>()
        val withProcessed = withoutProcessed.copy(processedBitmap = processed)
        assertTrue(CaptureAnimationCoordinator.canStartFlattenAnimation(withProcessed))
    }

    @Test
    fun `nextPhaseAfterUiStep returns null when phase mismatch`() {
        val active = MultiCaptureOverlayState.Active(
            sessionId = 1L,
            phase = CaptureAnimationPhase.Lifting,
            quad = CropProcessor.defaultQuad()
        )
        assertNull(
            CaptureAnimationCoordinator.nextPhaseAfterUiStep(CaptureAnimationPhase.Detecting, active)
        )
    }
}
