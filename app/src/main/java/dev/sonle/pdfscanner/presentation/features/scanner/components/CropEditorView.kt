package dev.sonle.pdfscanner.presentation.features.scanner.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import kotlin.math.roundToInt

@Composable
fun CropEditorView(
    originalBitmap: Bitmap,
    editableQuad: DocumentQuad,
    onQuadUpdated: (DocumentQuad) -> Unit,
    onConfirm: () -> Unit,
    onRetake: () -> Unit,
    onAutoDetect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var draggingCornerIndex by remember { mutableIntStateOf(-1) }
    val imageDrawRect = remember(imageSize, originalBitmap.width, originalBitmap.height) {
        if (imageSize.width == 0 || imageSize.height == 0) {
            Rect.Zero
        } else {
            val containerW = imageSize.width.toFloat()
            val containerH = imageSize.height.toFloat()
            val imageAspect = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val containerAspect = containerW / containerH
            if (imageAspect > containerAspect) {
                val drawW = containerW
                val drawH = drawW / imageAspect
                val top = (containerH - drawH) / 2f
                Rect(0f, top, drawW, top + drawH)
            } else {
                val drawH = containerH
                val drawW = drawH * imageAspect
                val left = (containerW - drawW) / 2f
                Rect(left, 0f, left + drawW, drawH)
            }
        }
    }

    val currentQuad by rememberUpdatedState(editableQuad)
    val currentDrawRect by rememberUpdatedState(imageDrawRect)

    val cornerOffsets = remember(editableQuad, imageDrawRect) {
        cornerOffsetsForQuad(editableQuad, imageDrawRect)
    }

    val magnifierPosition = remember { mutableStateOf<Offset?>(null) }
    val magnifierRadius = 80f
    val magnifierZoom = 2.5f
    val imageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }

    val accentColor = Color(0xFF00E676)
    val surfaceDark = Color(0xFF1E1E1E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // ─── Image + Crop Overlay ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, top = 100.dp) // Provide space for floating elements
                .onSizeChanged { imageSize = it }
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Document",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            if (imageSize.width > 0 && imageSize.height > 0) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(imageDrawRect) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val touchRadius = 80f
                                    val corners = cornerOffsetsForQuad(currentQuad, currentDrawRect)
                                    draggingCornerIndex = indexOfDraggableCorner(offset, corners, touchRadius)
                                    if (draggingCornerIndex >= 0) {
                                        magnifierPosition.value = offset
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (draggingCornerIndex >= 0) {
                                        val pos = change.position
                                        val rect = currentDrawRect
                                        magnifierPosition.value = pos
                                        val normalizedX = if (rect.width > 0f) {
                                            ((pos.x - rect.left) / rect.width).coerceIn(0.01f, 0.99f)
                                        } else 0.5f
                                        val normalizedY = if (rect.height > 0f) {
                                            ((pos.y - rect.top) / rect.height).coerceIn(0.01f, 0.99f)
                                        } else 0.5f
                                        val newPoint = NormalizedPoint(normalizedX, normalizedY)
                                        val quad = currentQuad
                                        val updatedQuad = when (draggingCornerIndex) {
                                            0 -> quad.copy(tl = newPoint)
                                            1 -> quad.copy(tr = newPoint)
                                            2 -> quad.copy(br = newPoint)
                                            3 -> quad.copy(bl = newPoint)
                                            else -> quad
                                        }
                                        onQuadUpdated(updatedQuad)
                                    }
                                },
                                onDragEnd = {
                                    draggingCornerIndex = -1
                                    magnifierPosition.value = null
                                },
                                onDragCancel = {
                                    draggingCornerIndex = -1
                                    magnifierPosition.value = null
                                }
                            )
                        }
                ) {
                    val path = Path().apply {
                        moveTo(cornerOffsets[0].x, cornerOffsets[0].y)
                        lineTo(cornerOffsets[1].x, cornerOffsets[1].y)
                        lineTo(cornerOffsets[2].x, cornerOffsets[2].y)
                        lineTo(cornerOffsets[3].x, cornerOffsets[3].y)
                        close()
                    }

                    // Darken outside
                    drawRect(color = Color.Black.copy(alpha = 0.7f), size = size)
                    drawPath(path = path, color = Color.Black.copy(alpha = 0.7f), blendMode = BlendMode.DstOut)

                    // Glow border
                    drawPath(path = path, color = accentColor.copy(alpha = 0.4f), style = Stroke(width = 8f))
                    drawPath(path = path, color = accentColor, style = Stroke(width = 3f))

                    // Grid lines
                    for (t in listOf(1f / 3f, 2f / 3f)) {
                        val topPoint = lerp(cornerOffsets[0], cornerOffsets[1], t)
                        val bottomPoint = lerp(cornerOffsets[3], cornerOffsets[2], t)
                        drawLine(color = Color.White.copy(alpha = 0.3f), start = topPoint, end = bottomPoint, strokeWidth = 2f)

                        val leftPoint = lerp(cornerOffsets[0], cornerOffsets[3], t)
                        val rightPoint = lerp(cornerOffsets[1], cornerOffsets[2], t)
                        drawLine(color = Color.White.copy(alpha = 0.3f), start = leftPoint, end = rightPoint, strokeWidth = 2f)
                    }

                    // Corner handles
                    cornerOffsets.forEachIndexed { index, corner ->
                        val isBeingDragged = index == draggingCornerIndex
                        val handleRadius = if (isBeingDragged) 24f else 18f

                        drawCircle(color = accentColor.copy(alpha = if (isBeingDragged) 0.5f else 0.2f), radius = handleRadius + 8f, center = corner)
                        drawCircle(color = Color.White, radius = handleRadius, center = corner, style = Stroke(width = 4f))
                        drawCircle(color = if (isBeingDragged) accentColor else Color.White.copy(alpha = 0.5f), radius = handleRadius - 4f, center = corner)
                    }

                    // Edge midpoints
                    val edges = listOf(
                        cornerOffsets[0] to cornerOffsets[1],
                        cornerOffsets[1] to cornerOffsets[2],
                        cornerOffsets[2] to cornerOffsets[3],
                        cornerOffsets[3] to cornerOffsets[0]
                    )
                    edges.forEach { (start, end) ->
                        val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                        drawCircle(color = Color.White, radius = 8f, center = mid)
                        drawCircle(color = accentColor, radius = 4f, center = mid)
                    }

                    // Magnifier
                    magnifierPosition.value?.let { magPos ->
                        if (draggingCornerIndex >= 0) {
                            val magCenterY = if (magPos.y > size.height / 3f) 120f else size.height - 120f
                            val magCenter = Offset(size.width / 2f, magCenterY)
                            val lensRect = Rect(
                                magCenter.x - magnifierRadius,
                                magCenter.y - magnifierRadius,
                                magCenter.x + magnifierRadius,
                                magCenter.y + magnifierRadius
                            )

                            val src = magnifierSourceRect(
                                magPosCanvas = magPos,
                                imageDrawRect = imageDrawRect,
                                bitmapWidth = originalBitmap.width,
                                bitmapHeight = originalBitmap.height,
                                magnifierRadiusPx = magnifierRadius,
                                zoom = magnifierZoom
                            )

                            clipPath(Path().apply { addOval(lensRect) }) {
                                if (src != null) {
                                    drawImage(
                                        image = imageBitmap,
                                        srcOffset = IntOffset(src.left, src.top),
                                        srcSize = IntSize(src.width, src.height),
                                        dstOffset = IntOffset(lensRect.left.roundToInt(), lensRect.top.roundToInt()),
                                        dstSize = IntSize((magnifierRadius * 2f).roundToInt(), (magnifierRadius * 2f).roundToInt()),
                                        filterQuality = FilterQuality.High
                                    )
                                } else {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.88f),
                                        topLeft = Offset(lensRect.left, lensRect.top),
                                        size = Size(magnifierRadius * 2f, magnifierRadius * 2f)
                                    )
                                }
                            }

                            drawCircle(color = Color.White, radius = magnifierRadius, center = magCenter, style = Stroke(width = 6f))
                            drawLine(color = accentColor, start = Offset(magCenter.x - 24f, magCenter.y), end = Offset(magCenter.x + 24f, magCenter.y), strokeWidth = 3f)
                            drawLine(color = accentColor, start = Offset(magCenter.x, magCenter.y - 24f), end = Offset(magCenter.x, magCenter.y + 24f), strokeWidth = 3f)
                        }
                    }
                }
            }
        }

        // ─── Top Header ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.scanner_crop_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.scanner_crop_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // ─── Bottom Actions (Floating Pill) ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(surfaceDark.copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel / Retake
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onRetake() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.scanner_crop_retake),
                        tint = Color.White
                    )
                }

                // Auto Detect
                TextButton(
                    onClick = onAutoDetect,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                ) {
                    Icon(
                        Icons.Rounded.Build,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scanner_crop_auto_detect),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // Confirm
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.scanner_crop_confirm),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

// ─── Utility ─────────────────────────────────────────────────────────────────

private fun lerp(start: Offset, end: Offset, t: Float): Offset {
    return Offset(
        x = start.x + (end.x - start.x) * t,
        y = start.y + (end.y - start.y) * t
    )
}

