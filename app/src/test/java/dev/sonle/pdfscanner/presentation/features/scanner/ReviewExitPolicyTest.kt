package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression: exiting Page Review in SINGLE must drop thumbnails / pending insert (fresh camera).
 */
class ReviewExitPolicyTest {

    @Test
    fun `clearsScanSessionOnReviewBack is true only for single`() {
        assertTrue(clearsScanSessionOnReviewBack(PageMode.SINGLE))
        assertFalse(clearsScanSessionOnReviewBack(PageMode.MULTI))
    }
}
