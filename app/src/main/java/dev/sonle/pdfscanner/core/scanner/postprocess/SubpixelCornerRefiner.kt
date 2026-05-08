package dev.sonle.pdfscanner.core.scanner.postprocess

import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.core.TermCriteria

class SubpixelCornerRefiner {

    fun refine(
        quad: DocumentQuad,
        yBuffer: ByteArray,
        width: Int,
        height: Int
    ): DocumentQuad {
        if (quad.confidence <= 0f || width <= 0 || height <= 0) return quad
        if (yBuffer.size < width * height) return quad

        val gray = Mat(height, width, CvType.CV_8UC1)
        gray.put(0, 0, yBuffer)

        val points = MatOfPoint2f(
            toPoint(quad.tl, width, height),
            toPoint(quad.tr, width, height),
            toPoint(quad.br, width, height),
            toPoint(quad.bl, width, height)
        )

        runCatching {
            Imgproc.cornerSubPix(
                gray,
                points,
                Size(5.0, 5.0),
                Size(-1.0, -1.0),
                TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 30, 0.01)
            )
        }.onFailure { error ->
            return quad
        }

        val refined = points.toArray().toList()
        return quad.copy(
            tl = refined[0].toNormalized(width, height),
            tr = refined[1].toNormalized(width, height),
            br = refined[2].toNormalized(width, height),
            bl = refined[3].toNormalized(width, height)
        )
    }

    private fun toPoint(point: NormalizedPoint, width: Int, height: Int): Point {
        val maxX = (width - 1).coerceAtLeast(0).toDouble()
        val maxY = (height - 1).coerceAtLeast(0).toDouble()
        return Point(
            (point.x.toDouble() * width.toDouble()).coerceIn(0.0, maxX),
            (point.y.toDouble() * height.toDouble()).coerceIn(0.0, maxY)
        )
    }

    private fun Point.toNormalized(width: Int, height: Int): NormalizedPoint {
        return NormalizedPoint(
            x = (x / width.toDouble()).toFloat().coerceIn(0f, 1f),
            y = (y / height.toDouble()).toFloat().coerceIn(0f, 1f)
        )
    }
}
