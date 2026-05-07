package dev.sonle.pdfscanner.core.scanner.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.graphics.ImageFormat
import dev.sonle.pdfscanner.core.scanner.detection.DocumentDetector
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import dev.sonle.pdfscanner.core.scanner.postprocess.SubpixelCornerRefiner
import dev.sonle.pdfscanner.core.scanner.smoothing.QuadKalmanSmoother
import dev.sonle.pdfscanner.core.scanner.stability.StabilityState
import dev.sonle.pdfscanner.core.scanner.stability.StabilityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class DocumentAnalyzer(
    private val detector: DocumentDetector,
    private val refiner: SubpixelCornerRefiner,
    private val smoother: QuadKalmanSmoother,
    private val tracker: StabilityTracker,
    private val onResult: (DocumentQuad?, StabilityState) -> Unit
) : ImageAnalysis.Analyzer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isProcessing = false

    override fun analyze(image: ImageProxy) {
        if (isProcessing) {
            image.close()
            return
        }
        isProcessing = true

        val rgbaBuffer = image.planes.firstOrNull()?.buffer?.duplicate()

        if (rgbaBuffer == null) {
            onResult(null, tracker.update(null))
            image.close()
            isProcessing = false
            return
        }

        scope.launch {
            val detected = detector.detect(rgbaBuffer, image.width, image.height)
            val rotationCorrected = detected?.let { applyRotationCorrection(it, image.imageInfo.rotationDegrees) }
            val grayscaleForRefine = extractGrayscaleBuffer(image, rgbaBuffer)
            val refined = rotationCorrected?.let { refiner.refine(it, grayscaleForRefine, image.width, image.height) }
            val smoothed = refined?.let { smoother.smooth(it) }
            val stability = tracker.update(smoothed)
            onResult(smoothed, stability)
            image.close()
            isProcessing = false
        }
    }

    private fun extractGrayscaleBuffer(image: ImageProxy, rgbaBuffer: ByteBuffer): ByteArray {
        if (image.format == ImageFormat.YUV_420_888) {
            val yPlane = image.planes.firstOrNull()?.buffer?.duplicate() ?: return ByteArray(0)
            return ByteArray(yPlane.remaining()).also { yPlane.get(it) }
        }

        val rgba = rgbaBuffer.duplicate().apply { rewind() }
        val pixelCount = image.width * image.height
        val gray = ByteArray(pixelCount)
        var i = 0
        while (i < pixelCount && rgba.remaining() >= 4) {
            val r = rgba.get().toInt() and 0xFF
            val g = rgba.get().toInt() and 0xFF
            val b = rgba.get().toInt() and 0xFF
            rgba.get() // alpha
            val luma = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
            gray[i] = luma.toByte()
            i++
        }
        return gray
    }

    private fun applyRotationCorrection(quad: DocumentQuad, rotationDegrees: Int): DocumentQuad {
        if (rotationDegrees % 360 == 0) return quad
        val rotated = quad.points().map { point ->
            when ((rotationDegrees % 360 + 360) % 360) {
                90 -> NormalizedPoint(x = 1f - point.y, y = point.x)
                180 -> NormalizedPoint(x = 1f - point.x, y = 1f - point.y)
                270 -> NormalizedPoint(x = point.y, y = 1f - point.x)
                else -> point
            }
        }
        return quad.copy(
            tl = rotated.minBy { it.x + it.y },
            tr = rotated.minBy { it.y - it.x },
            br = rotated.maxBy { it.x + it.y },
            bl = rotated.maxBy { it.y - it.x }
        )
    }
}
