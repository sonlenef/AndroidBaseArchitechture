package dev.sonle.pdfscanner.data.repository

import dev.sonle.pdfscanner.data.local.ReviewPromptPreferences
import dev.sonle.pdfscanner.domain.repository.ReviewPromptRepository
import dev.sonle.pdfscanner.domain.review.ReviewPromptPolicy

class ReviewPromptRepositoryImpl(
    private val preferences: ReviewPromptPreferences
) : ReviewPromptRepository {

    override fun getSuccessfulScanCount(): Int = preferences.getSuccessfulScanCount()

    override fun recordSuccessfulScan() {
        if (hasReviewPromptBeenHandled()) return
        preferences.incrementSuccessfulScanCount()
        if (ReviewPromptPolicy.shouldScheduleReviewOnHome(
                successfulScanCount = getSuccessfulScanCount(),
                reviewPromptHandled = false
            )
        ) {
            preferences.setReviewPromptPendingOnHome(true)
        }
    }

    override fun hasReviewPromptBeenHandled(): Boolean =
        preferences.hasReviewPromptBeenHandled()

    override fun markReviewPromptHandled() {
        preferences.markReviewPromptHandled()
        preferences.setReviewPromptPendingOnHome(false)
    }

    override fun onUserSharedExport() {
        if (hasReviewPromptBeenHandled()) return
        preferences.incrementSuccessfulShareCount()
    }

    override fun refreshReviewPromptPendingFromCounts() {
        if (hasReviewPromptBeenHandled()) return
        if (preferences.isReviewPromptPendingOnHome()) return
        if (ReviewPromptPolicy.shouldScheduleReviewOnHome(
                successfulScanCount = getSuccessfulScanCount(),
                reviewPromptHandled = false
            )
        ) {
            preferences.setReviewPromptPendingOnHome(true)
        }
    }

    override fun prepareDebugReviewPromptOnHome() {
        preferences.prepareDebugReviewPromptOnHome()
    }

    override fun consumePendingReviewPromptOnHome(): Boolean {
        if (hasReviewPromptBeenHandled()) {
            preferences.setReviewPromptPendingOnHome(false)
            return false
        }
        if (!preferences.isReviewPromptPendingOnHome()) return false
        preferences.setReviewPromptPendingOnHome(false)
        return true
    }
}
