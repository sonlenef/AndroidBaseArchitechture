package dev.sonle.pdfscanner.presentation.features.scanner.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.presentation.features.scanner.model.CaptureAnimationPhase
import dev.sonle.pdfscanner.presentation.features.scanner.model.MultiCaptureOverlayState
import dev.sonle.pdfscanner.presentation.features.scanner.util.ScannerPreviewMapper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object CaptureAnimationTimings {
    const val DETECTING_MIN_MS = 100
    const val DETECTING_SETTLE_MS = 60
    const val DETECTING_WITH_FRAME_MS = 16
    const val FLYING_MS = 300
    const val FLY_FADE_MS = 140
}

@Composable
fun MultiCaptureAnimationOverlay(
    overlayState: MultiCaptureOverlayState,
    stackAnchorCenter: Offset?,
    onAnimationStepFinished: (CaptureAnimationPhase) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = overlayState as? MultiCaptureOverlayState.Active ?: return

    val phaseLabel = when (active.phase) {
        CaptureAnimationPhase.Detecting -> stringResource(R.string.scanner_capture_detecting)
        CaptureAnimationPhase.Lifting -> stringResource(R.string.scanner_capture_detecting)
        CaptureAnimationPhase.Flattening -> stringResource(R.string.scanner_capture_flattening)
        CaptureAnimationPhase.FlyingToStack -> stringResource(R.string.scanner_capture_saving_page)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("multiCaptureOverlay")
            .semantics { contentDescription = phaseLabel }
    ) {
        val density = LocalDensity.current
        val containerPx = with(density) {
            Size(maxWidth.toPx(), maxHeight.toPx())
        }
        val thumbSidePx = with(density) { 52.dp.toPx() }

        DetectedRegionAnimator(
            sessionId = active.sessionId,
            phase = active.phase,
            frozenQuad = active.quad,
            sourceBitmap = active.sourceBitmap,
            processedBitmap = active.processedBitmap,
            containerSize = containerPx,
            stackAnchorCenter = stackAnchorCenter,
            stackThumbSidePx = thumbSidePx,
            onAnimationStepFinished = onAnimationStepFinished
        )
    }
}

@Composable
private fun DetectedRegionAnimator(
    sessionId: Long,
    phase: CaptureAnimationPhase,
    frozenQuad: DocumentQuad,
    sourceBitmap: Bitmap?,
    processedBitmap: Bitmap?,
    containerSize: Size,
    stackAnchorCenter: Offset?,
    stackThumbSidePx: Float,
    onAnimationStepFinished: (CaptureAnimationPhase) -> Unit
) {
    val sourceImage = remember(sourceBitmap) { sourceBitmap?.asImageBitmap() }
    val processedImage = remember(processedBitmap) { processedBitmap?.asImageBitmap() }

    val liftProgress = remember(sessionId) { Animatable(0f) }
    val flattenBlend = remember(sessionId) { Animatable(0f) }
    val flyProgress = remember(sessionId) { Animatable(0f) }
    val flyAlpha = remember(sessionId) { Animatable(1f) }
    val pulseAlpha = remember(sessionId) { Animatable(1f) }
    val imageReveal = remember(sessionId) { Animatable(0f) }

    val detectedCorners = remember(frozenQuad, containerSize) {
        ScannerPreviewMapper.mapQuadToOffsets(frozenQuad, containerSize)
    }
    val cardRect = remember(containerSize) { ScannerPreviewMapper.targetCardRect(containerSize) }
    val cardCorners = remember(cardRect) { ScannerPreviewMapper.rectCorners(cardRect) }
    // Fallback matches bottom-right stack slot before onGloballyPositioned fires
    val stackCenter = stackAnchorCenter
        ?: Offset(containerSize.width * 0.86f, containerSize.height * 0.88f)
    val stackCorners = remember(stackCenter, stackThumbSidePx) {
        ScannerPreviewMapper.rectCorners(
            ScannerPreviewMapper.stackThumbRect(stackCenter, stackThumbSidePx)
        )
    }

    LaunchedEffect(sessionId, sourceBitmap) {
        if (sourceBitmap == null) return@LaunchedEffect
        imageReveal.snapTo(1f)
        pulseAlpha.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(sessionId, phase, sourceBitmap) {
        if (phase != CaptureAnimationPhase.Detecting) return@LaunchedEffect
        if (sourceBitmap == null) return@LaunchedEffect
        if (sourceBitmap != null) {
            delay(CaptureAnimationTimings.DETECTING_WITH_FRAME_MS.toLong())
        } else {
            delay(CaptureAnimationTimings.DETECTING_MIN_MS.toLong())
            delay(CaptureAnimationTimings.DETECTING_SETTLE_MS.toLong())
        }
        onAnimationStepFinished(CaptureAnimationPhase.Detecting)
    }

    LaunchedEffect(sessionId, phase) {
        when (phase) {
            CaptureAnimationPhase.Lifting -> {
                liftProgress.snapTo(0f)
                liftProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                onAnimationStepFinished(CaptureAnimationPhase.Lifting)
            }
            CaptureAnimationPhase.FlyingToStack -> {
                flyProgress.snapTo(0f)
                flyAlpha.snapTo(1f)
                coroutineScope {
                    launch {
                        flyProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                CaptureAnimationTimings.FLYING_MS,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                    launch {
                        delay(
                            (CaptureAnimationTimings.FLYING_MS - CaptureAnimationTimings.FLY_FADE_MS)
                                .toLong()
                                .coerceAtLeast(0)
                        )
                        flyAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                CaptureAnimationTimings.FLY_FADE_MS,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
                onAnimationStepFinished(CaptureAnimationPhase.FlyingToStack)
            }
            CaptureAnimationPhase.Detecting,
            CaptureAnimationPhase.Flattening -> Unit
        }
    }

    LaunchedEffect(sessionId, phase, processedBitmap) {
        if (phase != CaptureAnimationPhase.Flattening) return@LaunchedEffect
        if (processedBitmap == null) return@LaunchedEffect
        flattenBlend.snapTo(0f)
        flattenBlend.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        onAnimationStepFinished(CaptureAnimationPhase.Flattening)
    }

    val lift = liftProgress.value.coerceIn(0f, 1f)
    val flatten = flattenBlend.value.coerceIn(0f, 1f)
    val fly = flyProgress.value.coerceIn(0f, 1f)
    val overlayAlpha = flyAlpha.value.coerceIn(0f, 1f)

    val cornerOffsets = when (phase) {
        CaptureAnimationPhase.Detecting -> detectedCorners
        CaptureAnimationPhase.Lifting -> ScannerPreviewMapper.lerpOffsets(
            detectedCorners,
            cardCorners,
            lift
        )
        CaptureAnimationPhase.Flattening -> cardCorners
        CaptureAnimationPhase.FlyingToStack -> ScannerPreviewMapper.lerpOffsets(
            cardCorners,
            stackCorners,
            fly
        )
    }

    val showBitmap = sourceImage != null
    val dimStrength = when (phase) {
        CaptureAnimationPhase.Detecting -> if (showBitmap) 0.2f else 0.12f
        CaptureAnimationPhase.Lifting -> 0.16f * (1f - lift * 0.65f)
        else -> 0.08f * (1f - fly)
    }

    if (cornerOffsets.size == 4) {
        QuadClippedCaptureCanvas(
            cornerOffsets = cornerOffsets,
            containerSize = containerSize,
            sourceImage = if (showBitmap) sourceImage else null,
            processedImage = processedImage,
            processedBlend = if (phase == CaptureAnimationPhase.Flattening ||
                phase == CaptureAnimationPhase.FlyingToStack
            ) {
                flatten
            } else {
                0f
            },
            imageReveal = if (showBitmap) 1f else imageReveal.value,
            strokeAlpha = if (phase == CaptureAnimationPhase.Detecting) {
                pulseAlpha.value
            } else {
                (1f - lift).coerceIn(0f, 1f) * 0.85f
            },
            dimStrength = dimStrength,
            contentAlpha = overlayAlpha
        )
    }
}

@Composable
private fun QuadClippedCaptureCanvas(
    cornerOffsets: List<Offset>,
    containerSize: Size,
    sourceImage: ImageBitmap?,
    processedImage: ImageBitmap?,
    processedBlend: Float,
    imageReveal: Float,
    strokeAlpha: Float,
    dimStrength: Float,
    contentAlpha: Float
) {
    val contentRect = remember(containerSize) {
        ScannerPreviewMapper.previewContentRect(containerSize)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (cornerOffsets.size != 4) return@Canvas

        val regionPath = Path().apply {
            moveTo(cornerOffsets[0].x, cornerOffsets[0].y)
            lineTo(cornerOffsets[1].x, cornerOffsets[1].y)
            lineTo(cornerOffsets[2].x, cornerOffsets[2].y)
            lineTo(cornerOffsets[3].x, cornerOffsets[3].y)
            close()
        }

        if (dimStrength > 0f) {
            clipPath(regionPath, clipOp = ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = dimStrength))
            }
        }

        if (sourceImage != null && imageReveal > 0f) {
            val dstLeft = contentRect.left.roundToInt()
            val dstTop = contentRect.top.roundToInt()
            val dstWidth = contentRect.width.roundToInt().coerceAtLeast(1)
            val dstHeight = contentRect.height.roundToInt().coerceAtLeast(1)
            val bitmapAlpha = (imageReveal * contentAlpha).coerceIn(0f, 1f)

            clipPath(regionPath) {
                drawImage(
                    image = sourceImage,
                    dstOffset = IntOffset(dstLeft, dstTop),
                    dstSize = IntSize(dstWidth, dstHeight),
                    alpha = bitmapAlpha * (1f - processedBlend)
                )
                if (processedImage != null && processedBlend > 0f) {
                    drawImage(
                        image = processedImage,
                        dstOffset = IntOffset(dstLeft, dstTop),
                        dstSize = IntSize(dstWidth, dstHeight),
                        alpha = bitmapAlpha * processedBlend
                    )
                }
            }
        }

        if (strokeAlpha > 0.05f) {
            drawPath(
                path = regionPath,
                color = Color(0xFF00E676).copy(alpha = 0.14f * strokeAlpha),
            )
            drawPath(
                path = regionPath,
                color = Color(0xFF00E676).copy(alpha = strokeAlpha),
                style = Stroke(width = 4f)
            )
        }
    }
}
