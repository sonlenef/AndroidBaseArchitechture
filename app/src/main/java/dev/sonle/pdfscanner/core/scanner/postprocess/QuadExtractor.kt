package dev.sonle.pdfscanner.core.scanner.postprocess

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class QuadExtractor {

    fun extractFromMask(maskMat: Mat): DocumentQuad? {
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(maskMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        if (contours.isEmpty()) return null

        val largestContour = contours.maxByOrNull { Imgproc.contourArea(it) } ?: return null
        val contour2f = MatOfPoint2f(*largestContour.toArray())
        val arcLength = Imgproc.arcLength(contour2f, true)

        var bestQuad: MatOfPoint2f? = null
        var epsilon = 0.02
        repeat(6) {
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, approx, epsilon * arcLength, true)
            if (approx.total() == 4L) {
                bestQuad = approx
                return@repeat
            }
            epsilon += 0.005
        }

        val quadPoints = bestQuad?.toArray()?.toList() ?: run {
            val rect = Imgproc.minAreaRect(contour2f)
            val rectPoints = arrayOfNulls<Point>(4)
            rect.points(rectPoints)
            rectPoints.filterNotNull()
        }

        if (quadPoints.size != 4) return null
        val sorted = sortCorners(quadPoints)
        val normalized = sorted.map { it.toNormalized(maskMat.width(), maskMat.height()) }
        val minX = normalized.minOf { it.x }
        val maxX = normalized.maxOf { it.x }
        val minY = normalized.minOf { it.y }
        val maxY = normalized.maxOf { it.y }
        val bboxW = maxX - minX
        val bboxH = maxY - minY
        if (bboxW > 0.98f && bboxH > 0.98f) {
            return null
        }
        val confidence = estimateConfidence(maskMat, sorted)
        return DocumentQuad(
            tl = sorted[0].toNormalized(maskMat.width(), maskMat.height()),
            tr = sorted[1].toNormalized(maskMat.width(), maskMat.height()),
            br = sorted[2].toNormalized(maskMat.width(), maskMat.height()),
            bl = sorted[3].toNormalized(maskMat.width(), maskMat.height()),
            confidence = confidence
        )
    }

    private fun estimateConfidence(maskMat: Mat, points: List<Point>): Float {
        val polygonMat = Mat.zeros(maskMat.size(), CvType.CV_8UC1)
        val polygon = MatOfPoint(*points.toTypedArray())
        Imgproc.fillConvexPoly(polygonMat, polygon, org.opencv.core.Scalar(255.0))

        val intersection = Mat()
        org.opencv.core.Core.bitwise_and(maskMat, polygonMat, intersection)
        val union = Mat()
        org.opencv.core.Core.bitwise_or(maskMat, polygonMat, union)

        val intersectionPixels = org.opencv.core.Core.countNonZero(intersection).toFloat()
        val unionPixels = org.opencv.core.Core.countNonZero(union).toFloat().coerceAtLeast(1f)
        return (intersectionPixels / unionPixels).coerceIn(0f, 1f)
    }

    private fun Point.toNormalized(width: Int, height: Int): NormalizedPoint {
        return NormalizedPoint(
            x = ((x / width.toDouble()).coerceIn(0.0, 1.0)).toFloat(),
            y = ((y / height.toDouble()).coerceIn(0.0, 1.0)).toFloat()
        )
    }

    private fun sortCorners(points: List<Point>): List<Point> {
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()
        val remaining = points - tl - br
        val tr = remaining.minByOrNull { it.y - it.x } ?: remaining.first()
        val bl = remaining.maxByOrNull { it.y - it.x } ?: remaining.last()
        return listOf(tl, tr, br, bl)
    }
}
