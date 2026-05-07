package dev.sonle.pdfscanner.core.scanner.stability

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import java.util.ArrayDeque
import kotlin.math.pow

data class StabilityState(
    val isStable: Boolean,
    val stabilityProgress: Float
)

class StabilityTracker(
    private val windowSize: Int = 10,
    private val varianceThreshold: Float = 0.00002f
) {
    private val history = ArrayDeque<DocumentQuad>(windowSize)

    fun update(quad: DocumentQuad?): StabilityState {
        if (quad == null) {
            history.clear()
            return StabilityState(isStable = false, stabilityProgress = 0f)
        }

        if (history.size == windowSize) {
            history.removeFirst()
        }
        history.addLast(quad)

        if (history.size < 3) {
            return StabilityState(isStable = false, stabilityProgress = history.size / windowSize.toFloat())
        }

        val pointsByCorner = history.map { it.points() }.let { list ->
            List(4) { cornerIndex -> list.map { points -> points[cornerIndex] } }
        }

        val totalVariance = pointsByCorner.fold(0f) { acc, cornerPoints ->
            val meanX = cornerPoints.map { it.x }.average().toFloat()
            val meanY = cornerPoints.map { it.y }.average().toFloat()
            val variance = cornerPoints.fold(0f) { varianceAcc, point ->
                ((point.x - meanX).pow(2) + (point.y - meanY).pow(2)).toDouble()
                    .toFloat() + varianceAcc
            } / cornerPoints.size.toFloat()
            acc + variance
        }

        val progress = (1f - (totalVariance / (varianceThreshold * 6f))).coerceIn(0f, 1f)
        return StabilityState(
            isStable = history.size >= windowSize - 2 && totalVariance <= varianceThreshold,
            stabilityProgress = progress
        )
    }
}
