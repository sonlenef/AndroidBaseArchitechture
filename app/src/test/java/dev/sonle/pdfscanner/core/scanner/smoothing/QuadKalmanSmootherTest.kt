package dev.sonle.pdfscanner.core.scanner.smoothing

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class QuadKalmanSmootherTest {

    @Test
    fun `smooth should reduce jitter for repeated noisy points`() {
        val smoother = QuadKalmanSmoother()
        val noisyInputs = listOf(
            createQuad(0.2f),
            createQuad(0.205f),
            createQuad(0.198f),
            createQuad(0.203f)
        )

        val outputs = noisyInputs.map { smoother.smooth(it) }
        val xs = outputs.map { it.tl.x }
        val outputVariance = (xs.maxOrNull() ?: 0f) - (xs.minOrNull() ?: 0f)
        assertTrue(outputVariance < 0.02f)
    }

    private fun createQuad(x: Float): DocumentQuad {
        return DocumentQuad(
            tl = NormalizedPoint(x, 0.2f),
            tr = NormalizedPoint(0.8f, 0.2f),
            br = NormalizedPoint(0.8f, 0.8f),
            bl = NormalizedPoint(x, 0.8f),
            confidence = 0.9f
        )
    }
}
