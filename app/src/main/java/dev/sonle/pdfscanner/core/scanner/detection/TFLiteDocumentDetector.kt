package dev.sonle.pdfscanner.core.scanner.detection

import android.content.Context
import android.graphics.Bitmap
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.postprocess.QuadExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteDocumentDetector(
    private val context: Context,
    private val quadExtractor: QuadExtractor
) : DocumentDetector {

    private val modelFileName = "fairscan-segmentation-model.tflite"

    private val interpreter: Interpreter by lazy { createInterpreter() }

    override suspend fun detect(
        rgbaBuffer: ByteBuffer,
        width: Int,
        height: Int
    ): DocumentQuad? = withContext(Dispatchers.Default) {
        runCatching {
            val inputSpec = readInputSpec()
            if (inputSpec.channels != 3) {
                return@runCatching null
            }

            val bitmap = rgbaBufferToBitmap(rgbaBuffer, width, height)
            val resized = Bitmap.createScaledBitmap(bitmap, inputSpec.width, inputSpec.height, true)
            val inputBuffer = bitmapToInputBuffer(resized, inputSpec.type)

            val outSpec = readOutputSpec()
            val outputBuffer = ByteBuffer
                .allocateDirect(outSpec.width * outSpec.height * outSpec.type.byteSize())
                .order(ByteOrder.nativeOrder())

            interpreter.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            val mask = outputToMaskMat(outputBuffer, outSpec.width, outSpec.height, outSpec.type)
            val cleaned = postprocessMask(mask)
            val inverted = Mat()
            Core.bitwise_not(cleaned, inverted)
            val cleanedInverted = postprocessMask(inverted)

            val directQuad = quadExtractor.extractFromMask(cleaned)
            val invertedQuad = quadExtractor.extractFromMask(cleanedInverted)

            val selectedQuad = when {
                directQuad == null -> invertedQuad
                invertedQuad == null -> directQuad
                directQuad.confidence >= invertedQuad.confidence -> directQuad
                else -> invertedQuad
            }
            selectedQuad
        }.onFailure {
            Timber.w(it, "Document detector failed")
        }.getOrNull()
    }

    private fun createInterpreter(): Interpreter {
        val modelBuffer = FileUtil.loadMappedFile(context, modelFileName)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseXNNPACK(true)
            setUseNNAPI(false)
        }
        return Interpreter(modelBuffer, options)
    }

    private data class InputSpec(
        val width: Int,
        val height: Int,
        val channels: Int,
        val type: DataType
    )

    private data class OutputSpec(
        val width: Int,
        val height: Int,
        val channels: Int,
        val type: DataType
    )

    private fun readInputSpec(): InputSpec {
        val tensor = interpreter.getInputTensor(0)
        val shape = tensor.shape() // [1, H, W, C]
        return InputSpec(
            width = shape[2],
            height = shape[1],
            channels = shape[3],
            type = tensor.dataType()
        )
    }

    private fun readOutputSpec(): OutputSpec {
        val tensor = interpreter.getOutputTensor(0)
        val shape = tensor.shape()
        val h = shape.getOrNull(shape.size - 3) ?: error("Unexpected output shape: ${shape.contentToString()}")
        val w = shape.getOrNull(shape.size - 2) ?: error("Unexpected output shape: ${shape.contentToString()}")
        val channels = shape.lastOrNull() ?: 1
        return OutputSpec(
            width = w,
            height = h,
            channels = channels,
            type = tensor.dataType()
        )
    }

    private fun rgbaBufferToBitmap(rgbaBuffer: ByteBuffer, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val dup = rgbaBuffer.duplicate()
        dup.rewind()
        bitmap.copyPixelsFromBuffer(dup)
        return bitmap
    }

    private fun bitmapToInputBuffer(bitmap: Bitmap, type: DataType): ByteBuffer {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val buffer = ByteBuffer.allocateDirect(w * h * 3 * type.byteSize()).order(ByteOrder.nativeOrder())
        when (type) {
            DataType.UINT8 -> {
                pixels.forEach { pixel ->
                    buffer.put(((pixel shr 16) and 0xFF).toByte())
                    buffer.put(((pixel shr 8) and 0xFF).toByte())
                    buffer.put((pixel and 0xFF).toByte())
                }
            }

            DataType.FLOAT32 -> {
                pixels.forEach { pixel ->
                    buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
                    buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
                    buffer.putFloat((pixel and 0xFF) / 255f)
                }
            }

            else -> error("Unsupported input type: $type")
        }
        buffer.rewind()
        return buffer
    }

    private fun outputToMaskMat(
        output: ByteBuffer,
        width: Int,
        height: Int,
        type: DataType
    ): Mat {
        val mask = Mat.zeros(height, width, CvType.CV_8UC1)
        when (type) {
            DataType.UINT8 -> {
                val bytes = ByteArray(width * height)
                output.get(bytes)
                val out = ByteArray(width * height)
                for (i in bytes.indices) {
                    out[i] = if ((bytes[i].toInt() and 0xFF) > 127) 255.toByte() else 0.toByte()
                }
                mask.put(0, 0, out)
            }

            DataType.FLOAT32 -> {
                val floatBuffer = output.asFloatBuffer()
                val values = FloatArray(width * height)
                var min = Float.MAX_VALUE
                var max = -Float.MAX_VALUE
                for (i in values.indices) {
                    val value = floatBuffer.get(i)
                    values[i] = value
                    if (value < min) min = value
                    if (value > max) max = value
                }
                val out = ByteArray(width * height)
                var positives = 0
                val looksLikeProbability = min >= 0f && max <= 1f
                for (i in 0 until width * height) {
                    val score = if (looksLikeProbability) {
                        values[i]
                    } else {
                        // Most segmentation models export logits; convert to probability.
                        (1f / (1f + kotlin.math.exp(-values[i]))).toFloat()
                    }
                    val isPositive = score > 0.5f
                    if (isPositive) positives++
                    out[i] = if (isPositive) 255.toByte() else 0.toByte()
                }
                mask.put(0, 0, out)
            }

            else -> error("Unsupported output type: $type")
        }
        return mask
    }

    private fun postprocessMask(mask: Mat): Mat {
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(7.0, 7.0))
        val cleaned = Mat()
        Imgproc.morphologyEx(mask, cleaned, Imgproc.MORPH_CLOSE, kernel)
        Imgproc.morphologyEx(cleaned, cleaned, Imgproc.MORPH_OPEN, kernel)
        val nonZero = Core.countNonZero(cleaned)
        val total = cleaned.rows() * cleaned.cols()
        if (nonZero < total * 0.02 || nonZero > total * 0.95) {
            Imgproc.threshold(cleaned, cleaned, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)
        }
        return cleaned
    }
}

private fun DataType.byteSize(): Int = when (this) {
    DataType.FLOAT32 -> 4
    DataType.UINT8 -> 1
    DataType.INT8 -> 1
    else -> error("Unsupported DataType byte size: $this")
}
