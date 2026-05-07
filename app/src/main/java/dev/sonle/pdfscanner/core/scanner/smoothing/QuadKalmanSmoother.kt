package dev.sonle.pdfscanner.core.scanner.smoothing

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import kotlin.math.abs

class QuadKalmanSmoother(
    private val resetThreshold: Float = 0.25f,
    private val alpha: Float = 0.7f
) {
    private var lastQuad: DocumentQuad? = null

    fun smooth(input: DocumentQuad): DocumentQuad {
        val previous = lastQuad
        if (previous == null) {
            lastQuad = input
            return input
        }

        val points = input.points()
        val previousPoints = previous.points()
        val smoothed = points.mapIndexed { index, point ->
            val prevPoint = previousPoints[index]
            if (abs(prevPoint.x - point.x) > resetThreshold || abs(prevPoint.y - point.y) > resetThreshold) {
                point
            } else {
                NormalizedPoint(
                    x = (alpha * prevPoint.x + (1f - alpha) * point.x).coerceIn(0f, 1f),
                    y = (alpha * prevPoint.y + (1f - alpha) * point.y).coerceIn(0f, 1f)
                )
            }
        }

        val result = input.copy(
            tl = smoothed[0],
            tr = smoothed[1],
            br = smoothed[2],
            bl = smoothed[3]
        )
        lastQuad = result
        return result
    }
}
