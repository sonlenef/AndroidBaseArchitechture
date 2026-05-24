package dev.sonle.pdfscanner.domain.review

/**
 * When to ask for a Play Store rating: after [MIN_SUCCESSFUL_SCANS] completed PDF saves.
 * The dialog is shown on Home when the user returns (never on app open, never blocking share).
 */
object ReviewPromptPolicy {
    const val MIN_SUCCESSFUL_SCANS = 3

    fun shouldScheduleReviewOnHome(
        successfulScanCount: Int,
        reviewPromptHandled: Boolean
    ): Boolean =
        !reviewPromptHandled && successfulScanCount >= MIN_SUCCESSFUL_SCANS
}
