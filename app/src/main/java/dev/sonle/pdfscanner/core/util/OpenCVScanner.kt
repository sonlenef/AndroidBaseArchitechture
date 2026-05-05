package dev.sonle.pdfscanner.core.util

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.sqrt

object OpenCVScanner {

    /**
     * Tìm 4 điểm góc của tài liệu trong ảnh.
     */
    fun findDocumentCorners(bitmap: Bitmap): List<Point>? {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // 1. Chuyển sang Grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // 2. Làm mờ để giảm nhiễu
        Imgproc.GaussianBlur(grayMat, grayMat, Size(5.0, 5.0), 0.0)

        // 3. Tìm cạnh (Canny)
        val edgeMat = Mat()
        Imgproc.Canny(grayMat, edgeMat, 75.0, 200.0)

        // 4. Tìm Contours
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edgeMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        // Sắp xếp contours theo diện tích giảm dần
        contours.sortByDescending { Imgproc.contourArea(it) }

        for (contour in contours) {
            val peri = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * peri, true)

            // Nếu contour có 4 cạnh, khả năng cao là tài liệu
            if (approx.total() == 4L) {
                return approx.toList()
            }
        }
        return null
    }

    /**
     * Cắt và xoay ảnh dựa trên 4 điểm góc.
     */
    fun perspectiveTransform(bitmap: Bitmap, corners: List<Point>): Bitmap {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)

        // Sắp xếp các điểm: top-left, top-right, bottom-right, bottom-left
        val sortedCorners = sortCorners(corners)
        
        val tl = sortedCorners[0]
        val tr = sortedCorners[1]
        val br = sortedCorners[2]
        val bl = sortedCorners[3]

        // Tính toán chiều rộng và chiều cao mới
        val widthA = sqrt(((br.x - bl.x) * (br.x - bl.x)) + ((br.y - bl.y) * (br.y - bl.y)))
        val widthB = sqrt(((tr.x - tl.x) * (tr.x - tl.x)) + ((tr.y - tl.y) * (tr.y - tl.y)))
        val maxWidth = max(widthA.toInt(), widthB.toInt())

        val heightA = sqrt(((tr.x - br.x) * (tr.x - br.x)) + ((tr.y - br.y) * (tr.y - br.y)))
        val heightB = sqrt(((tl.x - bl.x) * (tl.x - bl.x)) + ((tl.y - bl.y) * (tl.y - bl.y)))
        val maxHeight = max(heightA.toInt(), heightB.toInt())

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

        // Chuyển sang ảnh quét (B&W Adaptive Thresholding)
        Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.adaptiveThreshold(dstMat, dstMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)

        val resultBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, resultBitmap)
        
        return resultBitmap
    }

    private fun sortCorners(points: List<Point>): List<Point> {
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()

        val sortedByDiff = points.sortedBy { it.y - it.x }
        val tr = sortedByDiff.first()
        val bl = sortedByDiff.last()

        return listOf(tl, tr, br, bl)
    }
}
