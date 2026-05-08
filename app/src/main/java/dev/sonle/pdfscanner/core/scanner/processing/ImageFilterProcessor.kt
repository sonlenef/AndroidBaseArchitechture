package dev.sonle.pdfscanner.core.scanner.processing

import android.graphics.Bitmap
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Applies image filters to scanned document bitmaps using OpenCV.
 */
object ImageFilterProcessor {

    /**
     * Apply the given [filter] to the [bitmap] and return the processed result.
     */
    fun applyFilter(bitmap: Bitmap, filter: ImageFilter): Bitmap {
        return when (filter) {
            ImageFilter.ORIGINAL -> bitmap
            ImageFilter.BLACK_WHITE -> applyBlackAndWhite(bitmap)
            ImageFilter.GRAYSCALE -> applyGrayscale(bitmap)
            ImageFilter.MAGIC_COLOR -> applyMagicColor(bitmap)
            ImageFilter.SHARPEN -> applySharpen(bitmap)
        }
    }

    /**
     * Adaptive threshold B&W — best for text documents.
     */
    fun applyBlackAndWhite(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        // Apply adaptive threshold for clean B&W text
        Imgproc.adaptiveThreshold(
            gray, gray, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 21, 10.0
        )

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        // Convert single-channel back to RGBA for display
        val rgba = Mat()
        Imgproc.cvtColor(gray, rgba, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(rgba, result)

        src.release()
        gray.release()
        rgba.release()
        return result
    }

    /**
     * Simple grayscale conversion.
     */
    fun applyGrayscale(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

        val rgba = Mat()
        Imgproc.cvtColor(gray, rgba, Imgproc.COLOR_GRAY2RGBA)

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgba, result)

        src.release()
        gray.release()
        rgba.release()
        return result
    }

    /**
     * Enhanced color using CLAHE (Contrast Limited Adaptive Histogram Equalization).
     * Produces vivid, well-balanced colors.
     */
    fun applyMagicColor(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        // Convert to LAB color space for perceptual enhancement
        val lab = Mat()
        Imgproc.cvtColor(src, lab, Imgproc.COLOR_BGR2Lab)

        // Split channels
        val channels = mutableListOf<Mat>()
        Core.split(lab, channels)

        // Apply CLAHE to the L (lightness) channel
        val clahe = Imgproc.createCLAHE(3.0, Size(8.0, 8.0))
        clahe.apply(channels[0], channels[0])

        // Merge and convert back
        Core.merge(channels, lab)
        val enhanced = Mat()
        Imgproc.cvtColor(lab, enhanced, Imgproc.COLOR_Lab2BGR)

        // Slight saturation boost
        val hsv = Mat()
        Imgproc.cvtColor(enhanced, hsv, Imgproc.COLOR_BGR2HSV)
        val hsvChannels = mutableListOf<Mat>()
        Core.split(hsv, hsvChannels)
        hsvChannels[1].convertTo(hsvChannels[1], -1, 1.2, 10.0) // boost saturation
        Core.merge(hsvChannels, hsv)
        Imgproc.cvtColor(hsv, enhanced, Imgproc.COLOR_HSV2BGR)

        val rgba = Mat()
        Imgproc.cvtColor(enhanced, rgba, Imgproc.COLOR_BGR2RGBA)

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgba, result)

        // Cleanup
        src.release()
        lab.release()
        enhanced.release()
        hsv.release()
        rgba.release()
        channels.forEach { it.release() }
        hsvChannels.forEach { it.release() }

        return result
    }

    /**
     * Unsharp mask sharpening — enhances edge clarity and detail.
     */
    fun applySharpen(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        // Gaussian blur for unsharp mask
        val blurred = Mat()
        Imgproc.GaussianBlur(src, blurred, Size(0.0, 0.0), 3.0)

        // sharpened = src * 1.5 - blurred * 0.5
        val sharpened = Mat()
        Core.addWeighted(src, 1.5, blurred, -0.5, 0.0, sharpened)

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(sharpened, result)

        src.release()
        blurred.release()
        sharpened.release()
        return result
    }

    /**
     * Generate a small thumbnail preview for a filter.
     */
    fun generateFilterThumbnail(bitmap: Bitmap, filter: ImageFilter, maxSize: Int = 120): Bitmap {
        // Scale down first for performance
        val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        val thumbWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val thumbHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val thumbnail = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, true)
        return applyFilter(thumbnail, filter)
    }
}
