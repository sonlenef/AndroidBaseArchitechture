package dev.sonle.pdfscanner.domain.review

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPromptPolicyTest {

    @Test
    fun `shouldScheduleReviewOnHome is false before third scan`() {
        assertFalse(
            ReviewPromptPolicy.shouldScheduleReviewOnHome(0, reviewPromptHandled = false)
        )
        assertFalse(
            ReviewPromptPolicy.shouldScheduleReviewOnHome(2, reviewPromptHandled = false)
        )
    }

    @Test
    fun `shouldScheduleReviewOnHome is true from third scan`() {
        assertTrue(
            ReviewPromptPolicy.shouldScheduleReviewOnHome(3, reviewPromptHandled = false)
        )
        assertTrue(
            ReviewPromptPolicy.shouldScheduleReviewOnHome(5, reviewPromptHandled = false)
        )
    }

    @Test
    fun `shouldScheduleReviewOnHome is false after user handled prompt`() {
        assertFalse(
            ReviewPromptPolicy.shouldScheduleReviewOnHome(3, reviewPromptHandled = true)
        )
    }
}
