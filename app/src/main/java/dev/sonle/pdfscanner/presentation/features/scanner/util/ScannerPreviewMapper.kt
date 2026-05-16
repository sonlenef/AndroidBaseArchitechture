package dev.sonle.pdfscanner.presentation.features.scanner.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint

/**
 * Maps normalized document quads to screen coordinates for the camera preview (4:3 letterbox).
 */
object ScannerPreviewMapper {

  private const val PREVIEW_ASPECT_RATIO = 3f / 4f

  data class PreviewContentRect(
      val left: Float,
      val top: Float,
      val width: Float,
      val height: Float
  ) {
      fun mapNormalized(point: NormalizedPoint): Offset =
          Offset(left + point.x * width, top + point.y * height)
  }

  fun previewContentRect(containerSize: Size): PreviewContentRect {
      val viewAspectRatio = containerSize.width / containerSize.height
      return if (viewAspectRatio > PREVIEW_ASPECT_RATIO) {
          val contentHeight = containerSize.height
          val contentWidth = contentHeight * PREVIEW_ASPECT_RATIO
          PreviewContentRect(
              left = (containerSize.width - contentWidth) / 2f,
              top = 0f,
              width = contentWidth,
              height = contentHeight
          )
      } else {
          val contentWidth = containerSize.width
          val contentHeight = contentWidth / PREVIEW_ASPECT_RATIO
          PreviewContentRect(
              left = 0f,
              top = (containerSize.height - contentHeight) / 2f,
              width = contentWidth,
              height = contentHeight
          )
      }
  }

  fun mapQuadToOffsets(quad: DocumentQuad, containerSize: Size): List<Offset> {
      val content = previewContentRect(containerSize)
      return quad.points().map { content.mapNormalized(it) }
  }

  fun quadBoundingRect(quad: DocumentQuad, containerSize: Size): Rect {
      val offsets = mapQuadToOffsets(quad, containerSize)
      if (offsets.isEmpty()) return Rect.Zero
      val minX = offsets.minOf { it.x }
      val maxX = offsets.maxOf { it.x }
      val minY = offsets.minOf { it.y }
      val maxY = offsets.maxOf { it.y }
      return Rect(minX, minY, maxX, maxY)
  }

  fun lerpQuadToRect(quad: DocumentQuad, progress: Float, containerSize: Size): List<Offset> {
      val bounds = quadBoundingRect(quad, containerSize)
      val corners = mapQuadToOffsets(quad, containerSize)
      val targets = rectCorners(bounds)
      return lerpOffsets(corners, targets, progress)
  }

  fun rectCorners(rect: Rect): List<Offset> = listOf(
      Offset(rect.left, rect.top),
      Offset(rect.right, rect.top),
      Offset(rect.right, rect.bottom),
      Offset(rect.left, rect.bottom)
  )

  fun targetCardRect(containerSize: Size): Rect {
      val width = containerSize.width * 0.72f
      val height = width * 1.3f
      val centerX = containerSize.width / 2f
      val centerY = containerSize.height * 0.42f
      return Rect(
          left = centerX - width / 2f,
          top = centerY - height / 2f,
          right = centerX + width / 2f,
          bottom = centerY + height / 2f
      )
  }

  fun stackThumbRect(center: Offset, sideLength: Float): Rect = Rect(
      left = center.x - sideLength / 2f,
      top = center.y - sideLength / 2f,
      right = center.x + sideLength / 2f,
      bottom = center.y + sideLength / 2f
  )

  fun lerpOffsets(
      start: List<Offset>,
      end: List<Offset>,
      progress: Float
  ): List<Offset> {
      if (start.isEmpty()) return end
      val t = progress.coerceIn(0f, 1f)
      return start.mapIndexed { index, corner ->
          val target = end.getOrElse(index) { corner }
          Offset(
              x = lerp(corner.x, target.x, t),
              y = lerp(corner.y, target.y, t)
          )
      }
  }

  fun lerpQuadCornersToRect(
      quad: DocumentQuad,
      targetRect: Rect,
      progress: Float,
      containerSize: Size
  ): List<Offset> {
      val start = mapQuadToOffsets(quad, containerSize)
      val end = rectCorners(targetRect)
      return lerpOffsets(start, end, progress)
  }

  private fun lerp(start: Float, end: Float, fraction: Float): Float =
      start + (end - start) * fraction.coerceIn(0f, 1f)
}
