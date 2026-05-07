package dev.sonle.pdfscanner.core.scanner.postprocess

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class QuadExtractorTest {

    @Test
    fun `documentQuad should expose corners in deterministic order`() {
        val quad = DocumentQuad(
            tl = NormalizedPoint(0.1f, 0.1f),
            tr = NormalizedPoint(0.9f, 0.1f),
            br = NormalizedPoint(0.9f, 0.9f),
            bl = NormalizedPoint(0.1f, 0.9f),
            confidence = 1f
        )

        val points = quad.points()
        assertEquals(4, points.size)
        assertEquals(0.1f, points.first().x)
        assertEquals(0.9f, points.last().y)
    }
}
