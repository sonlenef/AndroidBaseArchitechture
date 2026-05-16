package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.presentation.features.scanner.model.CaptureAnimationPhase
import dev.sonle.pdfscanner.presentation.features.scanner.model.MultiCaptureOverlayState

internal object CaptureAnimationCoordinator {

    fun isCaptureLocked(overlay: MultiCaptureOverlayState): Boolean =
        overlay is MultiCaptureOverlayState.Active

    fun nextPhaseAfterUiStep(
        completedPhase: CaptureAnimationPhase,
        active: MultiCaptureOverlayState.Active
    ): CaptureAnimationPhase? {
        if (active.phase != completedPhase) return null
        return when (completedPhase) {
            CaptureAnimationPhase.Detecting -> CaptureAnimationPhase.Lifting
            CaptureAnimationPhase.Lifting -> CaptureAnimationPhase.Flattening
            CaptureAnimationPhase.Flattening -> CaptureAnimationPhase.FlyingToStack
            CaptureAnimationPhase.FlyingToStack -> null
        }
    }

    fun canStartFlattenAnimation(active: MultiCaptureOverlayState.Active): Boolean =
        active.processedBitmap != null

    fun shouldHoldLiftingUntilProcessed(active: MultiCaptureOverlayState.Active): Boolean =
        active.phase == CaptureAnimationPhase.Lifting && active.processedBitmap == null
}
