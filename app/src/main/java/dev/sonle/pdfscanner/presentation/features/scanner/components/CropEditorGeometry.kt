package dev.sonle.pdfscanner.presentation.features.scanner.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Bitmap pixel rectangle sampled for the magnifier lens (may be edge-clamped). */
internal data class MagnifierSourceRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

/**
 * Maps finger position on canvas to a centered bitmap crop for circular zoom ([zoom] > 1 zooms in).
 */
internal fun magnifierSourceRect(
    magPosCanvas: Offset,
    imageDrawRect: Rect,
    bitmapWidth: Int,
    bitmapHeight: Int,
    magnifierRadiusPx: Float,
    zoom: Float
): MagnifierSourceRect? {
    if (imageDrawRect.width <= 0f || imageDrawRect.height <= 0f || zoom <= 0f) return null

    val bmpW = bitmapWidth.toFloat()
    val bmpH = bitmapHeight.toFloat()

    val nx = ((magPosCanvas.x - imageDrawRect.left) / imageDrawRect.width).coerceIn(0f, 1f)
    val ny = ((magPosCanvas.y - imageDrawRect.top) / imageDrawRect.height).coerceIn(0f, 1f)
    val cx = nx * bmpW
    val cy = ny * bmpH

    val sx = bmpW / imageDrawRect.width
    val sy = bmpH / imageDrawRect.height

    val halfW = magnifierRadiusPx / zoom * sx
    val halfH = magnifierRadiusPx / zoom * sy

    val leftRaw = (cx - halfW).roundToInt()
    val topRaw = (cy - halfH).roundToInt()
    val rightRaw = (cx + halfW).roundToInt()
    val bottomRaw = (cy + halfH).roundToInt()

    val left = leftRaw.coerceIn(0, bitmapWidth)
    val top = topRaw.coerceIn(0, bitmapHeight)
    val right = rightRaw.coerceIn(0, bitmapWidth)
    val bottom = bottomRaw.coerceIn(0, bitmapHeight)

    val w = right - left
    val h = bottom - top
    if (w < 1 || h < 1) return null

    return MagnifierSourceRect(left, top, w, h)
}

/**
 * Pixel positions of quad corners inside [rect] (same mapping as [CropEditorView]).
 */
internal fun cornerOffsetsForQuad(quad: DocumentQuad, rect: Rect): List<Offset> {
    if (rect == Rect.Zero) {
        return listOf(Offset.Zero, Offset.Zero, Offset.Zero, Offset.Zero)
    }
    return quad.points().map { point ->
        Offset(
            x = rect.left + point.x * rect.width,
            y = rect.top + point.y * rect.height
        )
    }
}

/**
 * Index of the first corner within [touchRadius] px of [touch], or -1.
 */
internal fun indexOfDraggableCorner(touch: Offset, corners: List<Offset>, touchRadius: Float): Int =
    corners.indexOfFirst { corner ->
        sqrt(
            (corner.x - touch.x) * (corner.x - touch.x) +
                (corner.y - touch.y) * (corner.y - touch.y)
        ) < touchRadius
    }
