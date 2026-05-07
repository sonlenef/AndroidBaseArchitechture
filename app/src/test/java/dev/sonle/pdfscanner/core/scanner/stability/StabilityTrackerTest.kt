package dev.sonle.pdfscanner.core.scanner.stability

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StabilityTrackerTest {

    @Test
    fun `update should become stable for static quads`() {
        val tracker = StabilityTracker(windowSize = 6, varianceThreshold = 0.0001f)
        var stable = false
        repeat(6) {
            stable = tracker.update(staticQuad()).isStable
        }
        assertTrue(stable)
    }

    @Test
    fun `update should stay unstable for jittering quads`() {
        val tracker = StabilityTracker(windowSize = 6, varianceThreshold = 0.000001f)
        var stable = false
        repeat(6) { index ->
            stable = tracker.update(staticQuad(offset = if (index % 2 == 0) 0.03f else -0.03f)).isStable
        }
        assertFalse(stable)
    }

    private fun staticQuad(offset: Float = 0f): DocumentQuad {
        return DocumentQuad(
            tl = NormalizedPoint(0.2f + offset, 0.2f),
            tr = NormalizedPoint(0.8f + offset, 0.2f),
            br = NormalizedPoint(0.8f + offset, 0.8f),
            bl = NormalizedPoint(0.2f + offset, 0.8f),
            confidence = 0.9f
        )
    }
}
