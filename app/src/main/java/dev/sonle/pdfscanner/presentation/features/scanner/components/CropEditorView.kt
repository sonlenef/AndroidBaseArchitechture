package dev.sonle.pdfscanner.presentation.features.scanner.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

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

    // Convert normalized points to pixel offsets for drawing
    val cornerOffsets = remember(editableQuad, imageDrawRect) {
        if (imageDrawRect == Rect.Zero) {
            listOf(Offset.Zero, Offset.Zero, Offset.Zero, Offset.Zero)
        } else {
            editableQuad.points().map { point ->
                Offset(
                    x = imageDrawRect.left + point.x * imageDrawRect.width,
                    y = imageDrawRect.top + point.y * imageDrawRect.height
                )
            }
        }
    }

    // Magnifier state
    val magnifierPosition = remember { mutableStateOf<Offset?>(null) }
    val magnifierRadius = 80f
    val magnifierZoom = 2.5f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ─── Top Bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable(onClick = onRetake),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.scanner_crop_retake),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = stringResource(R.string.scanner_crop_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.scanner_crop_confirm),
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ─── Hint ────────────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.scanner_crop_hint),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ─── Image + Crop Overlay ────────────────────────────────────────
        Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onSizeChanged { imageSize = it }
        ) {
            // Background image
            Image(
                bitmap = originalBitmap.asImageBitmap(),
                contentDescription = "Document",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Crop overlay with draggable corners
            if (imageSize.width > 0 && imageSize.height > 0) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(editableQuad) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Find nearest corner within touch radius
                                    val touchRadius = 60f
                                    draggingCornerIndex = cornerOffsets
                                        .indexOfFirst { corner ->
                                            sqrt(
                                                (corner.x - offset.x).pow(2) +
                                                (corner.y - offset.y).pow(2)
                                            ) < touchRadius
                                        }
                                    if (draggingCornerIndex >= 0) {
                                        magnifierPosition.value = offset
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (draggingCornerIndex >= 0) {
                                        val pos = change.position
                                        magnifierPosition.value = pos
                                        val normalizedX = if (imageDrawRect.width > 0f) {
                                            ((pos.x - imageDrawRect.left) / imageDrawRect.width)
                                                .coerceIn(0.01f, 0.99f)
                                        } else {
                                            0.5f
                                        }
                                        val normalizedY = if (imageDrawRect.height > 0f) {
                                            ((pos.y - imageDrawRect.top) / imageDrawRect.height)
                                                .coerceIn(0.01f, 0.99f)
                                        } else {
                                            0.5f
                                        }
                                        val newPoint = NormalizedPoint(normalizedX, normalizedY)

                                        val updatedQuad = when (draggingCornerIndex) {
                                            0 -> editableQuad.copy(tl = newPoint)
                                            1 -> editableQuad.copy(tr = newPoint)
                                            2 -> editableQuad.copy(br = newPoint)
                                            3 -> editableQuad.copy(bl = newPoint)
                                            else -> editableQuad
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
                    // Semi-transparent overlay outside crop area
                    val path = Path().apply {
                        moveTo(cornerOffsets[0].x, cornerOffsets[0].y)
                        lineTo(cornerOffsets[1].x, cornerOffsets[1].y)
                        lineTo(cornerOffsets[2].x, cornerOffsets[2].y)
                        lineTo(cornerOffsets[3].x, cornerOffsets[3].y)
                        close()
                    }

                    // Darken area outside quad
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        size = size
                    )
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.6f),
                        blendMode = BlendMode.DstOut
                    )

                    // Quad border (glowing effect)
                    drawPath(
                        path = path,
                        color = Color(0xFF00E676).copy(alpha = 0.3f),
                        style = Stroke(width = 8f)
                    )
                    drawPath(
                        path = path,
                        color = Color(0xFF00E676),
                        style = Stroke(width = 3f)
                    )

                    // Grid lines (rule of thirds within quad)
                    for (t in listOf(1f / 3f, 2f / 3f)) {
                        val topPoint = lerp(cornerOffsets[0], cornerOffsets[1], t)
                        val bottomPoint = lerp(cornerOffsets[3], cornerOffsets[2], t)
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = topPoint,
                            end = bottomPoint,
                            strokeWidth = 2f
                        )

                        val leftPoint = lerp(cornerOffsets[0], cornerOffsets[3], t)
                        val rightPoint = lerp(cornerOffsets[1], cornerOffsets[2], t)
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = leftPoint,
                            end = rightPoint,
                            strokeWidth = 2f
                        )
                    }

                    // Corner handles
                    cornerOffsets.forEachIndexed { index, corner ->
                        val isBeingDragged = index == draggingCornerIndex
                        val handleRadius = if (isBeingDragged) 22f else 16f

                        // Outer glow
                        drawCircle(
                            color = Color(0xFF00E676).copy(alpha = 0.4f),
                            radius = handleRadius + 10f,
                            center = corner
                        )
                        // White ring
                        drawCircle(
                            color = Color.White,
                            radius = handleRadius,
                            center = corner
                        )
                        // Inner green dot
                        drawCircle(
                            color = Color(0xFF00E676),
                            radius = handleRadius - 6f,
                            center = corner
                        )
                    }

                    // Edge midpoint handles (smaller)
                    val edges = listOf(
                        cornerOffsets[0] to cornerOffsets[1],
                        cornerOffsets[1] to cornerOffsets[2],
                        cornerOffsets[2] to cornerOffsets[3],
                        cornerOffsets[3] to cornerOffsets[0]
                    )
                    edges.forEach { (start, end) ->
                        val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = 6f,
                            center = mid
                        )
                    }

                    // Magnifier
                    magnifierPosition.value?.let { magPos ->
                        if (draggingCornerIndex >= 0) {
                            val magCenterY = if (magPos.y > size.height / 3f) 100f else size.height - 100f
                            val magCenter = Offset(size.width / 2f, magCenterY)

                            // Magnifier background
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.9f),
                                radius = magnifierRadius + 6f,
                                center = magCenter
                            )
                            drawCircle(
                                color = Color(0xFF00E676),
                                radius = magnifierRadius + 6f,
                                center = magCenter,
                                style = Stroke(width = 4f)
                            )
                            // Crosshair
                            drawLine(
                                color = Color(0xFF00E676).copy(alpha = 0.8f),
                                start = Offset(magCenter.x - 24f, magCenter.y),
                                end = Offset(magCenter.x + 24f, magCenter.y),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color(0xFF00E676).copy(alpha = 0.8f),
                                start = Offset(magCenter.x, magCenter.y - 24f),
                                end = Offset(magCenter.x, magCenter.y + 24f),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
        }

        // ─── Bottom Actions ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Retake
                OutlinedButton(
                    onClick = onRetake,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp).weight(1f)
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scanner_crop_retake),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                // Auto detect
                FilledTonalButton(
                    onClick = onAutoDetect,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp).weight(1f)
                ) {
                    Icon(
                        Icons.Rounded.Build,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scanner_crop_auto_detect),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Confirm
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp).weight(1f)
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.scanner_crop_confirm),
                        fontWeight = FontWeight.Bold
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
