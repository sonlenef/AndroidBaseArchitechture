package dev.sonle.pdfscanner.core.review

/**
 * Result of attempting Google Play In-App Review with optional store fallback.
 */
sealed class InAppReviewOutcome {
    /** [com.google.android.play.core.review.ReviewManager.launchReviewFlow] completed (UI may or may not appear per quota). */
    data object FlowFinished : InAppReviewOutcome()

    /** Store listing opened because the in-app flow could not be started. */
    data object StoreFallback : InAppReviewOutcome()

    /** In-app review and Play Store fallback both failed. */
    data object Unavailable : InAppReviewOutcome()
}
