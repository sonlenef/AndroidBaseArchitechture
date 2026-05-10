package dev.sonle.pdfscanner.presentation.features.scanner.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.roundToInt
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class CropEditorGeometryTest {

    private val unitSquare = DocumentQuad(
        tl = NormalizedPoint(0f, 0f),
        tr = NormalizedPoint(1f, 0f),
        br = NormalizedPoint(1f, 1f),
        bl = NormalizedPoint(0f, 1f),
        confidence = 1f
    )

    @Test
    fun `cornerOffsetsForQuad maps normalized corners into draw rect`() {
        val rect = Rect(left = 10f, top = 20f, right = 110f, bottom = 220f)
        val corners = cornerOffsetsForQuad(unitSquare, rect)
        assertEquals(Offset(10f, 20f), corners[0])
        assertEquals(Offset(110f, 20f), corners[1])
        assertEquals(Offset(110f, 220f), corners[2])
        assertEquals(Offset(10f, 220f), corners[3])
    }

    @Test
    fun `indexOfDraggableCorner returns first matching corner within radius`() {
        val corners = listOf(
            Offset(50f, 50f),
            Offset(150f, 50f),
            Offset(150f, 150f),
            Offset(50f, 150f)
        )
        assertEquals(0, indexOfDraggableCorner(Offset(55f, 55f), corners, 60f))
        assertEquals(-1, indexOfDraggableCorner(Offset(400f, 400f), corners, 60f))
    }

    @Test
    fun `magnifierSourceRect centers sample on touch mapped to bitmap`() {
        // Same aspect as bitmap so sampling window is square in bitmap pixels
        val draw = Rect(0f, 0f, 100f, 100f)
        val radius = 80f
        val zoom = 2.5f
        val src = magnifierSourceRect(
            magPosCanvas = Offset(50f, 50f),
            imageDrawRect = draw,
            bitmapWidth = 100,
            bitmapHeight = 100,
            magnifierRadiusPx = radius,
            zoom = zoom
        )
        requireNotNull(src)
        val halfBitmap = (radius / zoom * (100f / draw.width)).roundToInt()
        assertEquals(50 - halfBitmap, src.left)
        assertEquals(50 - halfBitmap, src.top)
        assertEquals(halfBitmap * 2, src.width)
        assertEquals(halfBitmap * 2, src.height)
    }
}
