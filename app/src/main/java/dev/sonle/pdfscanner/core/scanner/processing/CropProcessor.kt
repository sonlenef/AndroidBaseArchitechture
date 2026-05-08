package dev.sonle.pdfscanner.core.scanner.processing

import android.graphics.Bitmap
import android.graphics.Matrix
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Handles perspective crop/transform using OpenCV.
 */
object CropProcessor {

    /**
     * Performs perspective transform on [bitmap] using the normalized [quad].
     * Returns the cropped & corrected bitmap.
     */
    fun cropAndTransform(bitmap: Bitmap, quad: DocumentQuad): Bitmap {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)

        // Convert normalized quad to pixel coordinates
        val w = bitmap.width.toDouble()
        val h = bitmap.height.toDouble()
        val tl = Point(quad.tl.x * w, quad.tl.y * h)
        val tr = Point(quad.tr.x * w, quad.tr.y * h)
        val br = Point(quad.br.x * w, quad.br.y * h)
        val bl = Point(quad.bl.x * w, quad.bl.y * h)

        // Calculate output dimensions
        val widthA = sqrt((br.x - bl.x) * (br.x - bl.x) + (br.y - bl.y) * (br.y - bl.y))
        val widthB = sqrt((tr.x - tl.x) * (tr.x - tl.x) + (tr.y - tl.y) * (tr.y - tl.y))
        val maxWidth = max(widthA.toInt(), widthB.toInt())

        val heightA = sqrt((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y))
        val heightB = sqrt((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y))
        val maxHeight = max(heightA.toInt(), heightB.toInt())

        if (maxWidth <= 0 || maxHeight <= 0) {
            srcMat.release()
            return bitmap
        }

        val dstMat = Mat(maxHeight, maxWidth, srcMat.type())
        val srcPoints = MatOfPoint2f(tl, tr, br, bl)
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth.toDouble() - 1, 0.0),
            Point(maxWidth.toDouble() - 1, maxHeight.toDouble() - 1),
            Point(0.0, maxHeight.toDouble() - 1)
        )

        val transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
        Imgproc.warpPerspective(srcMat, dstMat, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))

        val resultBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, resultBitmap)

        // Release mats
        srcMat.release()
        dstMat.release()
        transform.release()
        srcPoints.release()
        dstPoints.release()

        return resultBitmap
    }

    /**
     * Rotates a bitmap by the given degrees (0, 90, 180, 270).
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Creates a default full-image quad when no document is detected.
     */
    fun defaultQuad(): DocumentQuad = DocumentQuad(
        tl = NormalizedPoint(0.05f, 0.05f),
        tr = NormalizedPoint(0.95f, 0.05f),
        br = NormalizedPoint(0.95f, 0.95f),
        bl = NormalizedPoint(0.05f, 0.95f),
        confidence = 0f
    )
}
