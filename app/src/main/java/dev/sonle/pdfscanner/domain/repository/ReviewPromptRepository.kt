package dev.sonle.pdfscanner.domain.repository

interface ReviewPromptRepository {
    fun getSuccessfulScanCount(): Int
    fun recordSuccessfulScan()
    fun hasReviewPromptBeenHandled(): Boolean
    fun markReviewPromptHandled()
    /** Tracks Export/Share taps for analytics; scheduling uses [recordSuccessfulScan] only. */
    fun onUserSharedExport()
    /**
     * Sets the home pending flag when the user already reached the scan threshold
     * (e.g. before an app update). Safe to call on each Home resume.
     */
    fun refreshReviewPromptPendingFromCounts()
    /** Returns true once when Home should show the review dialog, then clears the pending flag. */
    fun consumePendingReviewPromptOnHome(): Boolean

    /** Debug: force the review dialog on next Home visit (does not mark as handled). */
    fun prepareDebugReviewPromptOnHome()
}
