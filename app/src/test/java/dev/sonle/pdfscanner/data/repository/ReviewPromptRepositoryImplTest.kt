package dev.sonle.pdfscanner.data.repository

import android.content.Context
import dev.sonle.pdfscanner.core.util.Constants
import dev.sonle.pdfscanner.data.local.ReviewPromptPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ReviewPromptRepositoryImplTest {

    private lateinit var repository: ReviewPromptRepositoryImpl

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        repository = ReviewPromptRepositoryImpl(ReviewPromptPreferences(context))
    }

    @Test
    fun `recordSuccessfulScan schedules review on home after third save`() {
        repository.recordSuccessfulScan()
        assertFalse(repository.consumePendingReviewPromptOnHome())
        repository.recordSuccessfulScan()
        assertFalse(repository.consumePendingReviewPromptOnHome())
        repository.recordSuccessfulScan()
        assertTrue(repository.consumePendingReviewPromptOnHome())
        assertFalse(repository.consumePendingReviewPromptOnHome())
    }

    @Test
    fun `onUserSharedExport does not schedule review without enough scans`() {
        repository.onUserSharedExport()
        repository.onUserSharedExport()
        repository.onUserSharedExport()
        assertFalse(repository.consumePendingReviewPromptOnHome())
    }

    @Test
    fun `refreshReviewPromptPendingFromCounts schedules once for users past threshold`() {
        repeat(3) { repository.recordSuccessfulScan() }
        repository.consumePendingReviewPromptOnHome()
        repository.refreshReviewPromptPendingFromCounts()
        assertTrue(repository.consumePendingReviewPromptOnHome())
    }

    @Test
    fun `markReviewPromptHandled clears pending and prevents rescheduling`() {
        repeat(3) { repository.recordSuccessfulScan() }
        assertTrue(repository.consumePendingReviewPromptOnHome())
        repository.markReviewPromptHandled()
        repository.recordSuccessfulScan()
        assertFalse(repository.consumePendingReviewPromptOnHome())
    }
}
