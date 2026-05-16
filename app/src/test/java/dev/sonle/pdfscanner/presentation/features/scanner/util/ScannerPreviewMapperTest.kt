package dev.sonle.pdfscanner.presentation.features.scanner.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerPreviewMapperTest {

    @Test
    fun `previewContentRect letterboxes wide container to 4 by 3`() {
        val container = Size(400f, 300f)
        val rect = ScannerPreviewMapper.previewContentRect(container)
        assertEquals(300f, rect.height, 0.01f)
        assertEquals(300f * (3f / 4f), rect.width, 0.01f)
        assertTrue(rect.left > 0f)
        assertEquals(0f, rect.top, 0.01f)
    }

    @Test
    fun `previewContentRect letterboxes tall container`() {
        val rect = ScannerPreviewMapper.previewContentRect(Size(300f, 800f))
        assertEquals(300f, rect.width, 0.01f)
        assertEquals(300f / (3f / 4f), rect.height, 0.01f)
        assertEquals(0f, rect.left, 0.01f)
        assertTrue(rect.top > 0f)
    }

    @Test
    fun `mapQuadToOffsets places corners inside content rect`() {
        val quad = DocumentQuad(
            tl = NormalizedPoint(0f, 0f),
            tr = NormalizedPoint(1f, 0f),
            br = NormalizedPoint(1f, 1f),
            bl = NormalizedPoint(0f, 1f),
            confidence = 1f
        )
        val container = Size(400f, 300f)
        val content = ScannerPreviewMapper.previewContentRect(container)
        val offsets = ScannerPreviewMapper.mapQuadToOffsets(quad, container)
        val bounds = ScannerPreviewMapper.quadBoundingRect(quad, container)
        assertEquals(4, offsets.size)
        assertEquals(content.left, bounds.left, 0.01f)
        assertEquals(content.top, bounds.top, 0.01f)
        assertEquals(content.left + content.width, bounds.right, 0.01f)
        assertEquals(content.top + content.height, bounds.bottom, 0.01f)
    }

    @Test
    fun `lerpQuadToRect reaches axis aligned corners at progress 1`() {
        val quad = DocumentQuad(
            tl = NormalizedPoint(0.2f, 0.2f),
            tr = NormalizedPoint(0.8f, 0.25f),
            br = NormalizedPoint(0.75f, 0.85f),
            bl = NormalizedPoint(0.15f, 0.8f),
            confidence = 1f
        )
        val container = Size(400f, 300f)
        val bounds = ScannerPreviewMapper.quadBoundingRect(quad, container)
        val lerped = ScannerPreviewMapper.lerpQuadToRect(quad, 1f, container)
        assertEquals(bounds.left, lerped[0].x, 0.5f)
        assertEquals(bounds.top, lerped[0].y, 0.5f)
        assertEquals(bounds.right, lerped[2].x, 0.5f)
        assertEquals(bounds.bottom, lerped[2].y, 0.5f)
    }

    @Test
    fun `lerpOffsets interpolates each corner toward target rect`() {
        val start = listOf(
            Offset(0f, 0f),
            Offset(100f, 0f),
            Offset(100f, 200f),
            Offset(0f, 200f)
        )
        val end = ScannerPreviewMapper.rectCorners(Rect(50f, 60f, 150f, 160f))
        val mid = ScannerPreviewMapper.lerpOffsets(start, end, 0.5f)
        assertEquals(25f, mid[0].x, 0.01f)
        assertEquals(30f, mid[0].y, 0.01f)
        assertEquals(125f, mid[2].x, 0.01f)
        assertEquals(180f, mid[2].y, 0.01f)
    }

    @Test
    fun `lerpQuadCornersToRect reaches card corners at progress 1`() {
        val quad = DocumentQuad(
            tl = NormalizedPoint(0.2f, 0.2f),
            tr = NormalizedPoint(0.8f, 0.25f),
            br = NormalizedPoint(0.75f, 0.85f),
            bl = NormalizedPoint(0.15f, 0.8f),
            confidence = 1f
        )
        val container = Size(400f, 300f)
        val card = ScannerPreviewMapper.targetCardRect(container)
        val lerped = ScannerPreviewMapper.lerpQuadCornersToRect(quad, card, 1f, container)
        val cardCorners = ScannerPreviewMapper.rectCorners(card)
        assertEquals(cardCorners[0].x, lerped[0].x, 0.5f)
        assertEquals(cardCorners[2].x, lerped[2].x, 0.5f)
    }
}
