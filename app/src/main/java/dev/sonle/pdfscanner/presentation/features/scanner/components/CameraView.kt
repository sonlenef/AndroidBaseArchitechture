package dev.sonle.pdfscanner.presentation.features.scanner.components

import android.graphics.Bitmap
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.analyzer.DocumentAnalyzer
import dev.sonle.pdfscanner.core.scanner.detection.DocumentDetector
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.postprocess.SubpixelCornerRefiner
import dev.sonle.pdfscanner.core.scanner.smoothing.QuadKalmanSmoother
import dev.sonle.pdfscanner.core.scanner.stability.StabilityTracker
import dev.sonle.pdfscanner.presentation.features.scanner.DetectionUiState
import dev.sonle.pdfscanner.presentation.features.scanner.model.CaptureAnimationPhase
import dev.sonle.pdfscanner.presentation.features.scanner.model.MultiCaptureOverlayState
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannerMode
import dev.sonle.pdfscanner.presentation.features.scanner.util.ScannerPreviewMapper
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import org.koin.compose.koinInject
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

@Composable
fun CameraView(
    scannerMode: ScannerMode,
    pageMode: PageMode,
    scannedPageCount: Int,
    isCapturePipelineRunning: Boolean,
    captureOverlay: MultiCaptureOverlayState,
    latestThumbnail: ImageBitmap?,
    detectionState: DetectionUiState,
    onDetectionUpdated: (DocumentQuad?, Float, Boolean) -> Unit,
    shouldAutoCapture: () -> Boolean,
    onImageCaptured: (File) -> Unit,
    onMultiCaptureShutter: () -> Unit,
    onCaptureFreezeFrameReady: (Bitmap?) -> Unit,
    onToggleScannerMode: () -> Unit,
    onTogglePageMode: () -> Unit,
    onReviewPages: () -> Unit,
    onCaptureAnimationStepFinished: (CaptureAnimationPhase) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val detector: DocumentDetector = koinInject()
    val subpixelCornerRefiner: SubpixelCornerRefiner = koinInject()
    val quadKalmanSmoother: QuadKalmanSmoother = koinInject()
    val stabilityTracker: StabilityTracker = koinInject()
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasTriggeredAutoCapture by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }
    var stackAnchorCenter by remember { mutableStateOf<Offset?>(null) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            tween(durationMillis = 300)
        } else {
            tween(durationMillis = 300)
        },
        label = "camera_content_alpha"
    )
    val topControlsOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-24).dp,
        animationSpec = if (isVisible) {
            tween(durationMillis = 320, delayMillis = 30)
        } else {
            tween(durationMillis = 260)
        },
        label = "camera_top_exit_offset"
    )
    val topControlsAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            tween(durationMillis = 320, delayMillis = 30)
        } else {
            tween(durationMillis = 260)
        },
        label = "camera_top_exit_alpha"
    )
    val bottomControlsOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = if (isVisible) {
            tween(durationMillis = 340, delayMillis = 90)
        } else {
            tween(durationMillis = 280, delayMillis = 60)
        },
        label = "camera_bottom_exit_offset"
    )
    val bottomControlsAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (isVisible) {
            tween(durationMillis = 340, delayMillis = 90)
        } else {
            tween(durationMillis = 280, delayMillis = 60)
        },
        label = "camera_bottom_exit_alpha"
    )
    val exitScrimAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0f else 0.35f,
        animationSpec = if (isVisible) {
            tween(durationMillis = 260)
        } else {
            tween(durationMillis = 300)
        },
        label = "camera_exit_scrim_alpha"
    )

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(
                        cameraExecutor,
                        DocumentAnalyzer(
                            detector = detector,
                            refiner = subpixelCornerRefiner,
                            smoother = quadKalmanSmoother,
                            tracker = stabilityTracker
                        ) { quad, stability ->
                            onDetectionUpdated(quad, stability.stabilityProgress, stability.isStable)
                        }
                    )
                }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Toggle flash
    LaunchedEffect(isFlashOn) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(isClosing) {
        if (isClosing) {
            isVisible = false
            delay(360)
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
        )

        val captureOverlayActive = captureOverlay as? MultiCaptureOverlayState.Active
        val hideLiveDetection = captureOverlayActive?.sourceBitmap != null

        DocumentDetectionOverlay(
            quad = detectionState.quad,
            isStable = detectionState.isStable,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .alpha(if (hideLiveDetection) 0f else contentAlpha)
        )

        if (pageMode == PageMode.MULTI && captureOverlayActive != null) {
            MultiCaptureAnimationOverlay(
                overlayState = captureOverlay,
                stackAnchorCenter = stackAnchorCenter,
                onAnimationStepFinished = onCaptureAnimationStepFinished,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (exitScrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = exitScrimAlpha))
            )
        }

        // ─── Top Bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(topControlsAlpha)
                .offset(y = topControlsOffsetY)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            GlassIconButton(
                icon = Iconsax.Linear.ArrowLeft,
                contentDescription = stringResource(R.string.scanner_close),
                onClick = {
                    if (!isClosing) {
                        isClosing = true
                    }
                }
            )

            // Auto or manual text toggle
            GlassTextButton(
                text = if (scannerMode == ScannerMode.AUTO) "AUTO" else "MANUAL",
                isActive = scannerMode == ScannerMode.AUTO,
                enabled = !isCapturePipelineRunning,
                onClick = onToggleScannerMode
            )

            // Flash toggle
            GlassIconButton(
                icon = if (isFlashOn) Iconsax.Bold.Flash else Iconsax.Linear.Flash,
                contentDescription = "Flash",
                tint = if (isFlashOn) Color(0xFFFFD700) else Color.White,
                enabled = !isCapturePipelineRunning,
                onClick = { isFlashOn = !isFlashOn }
            )
        }

        // ─── Hint Text ──────────────────────────────────────────────────
        val hintText = when (scannerMode) {
            ScannerMode.AUTO -> stringResource(R.string.scanner_frame_auto_hint)
            ScannerMode.MANUAL -> stringResource(R.string.scanner_frame_manual_hint)
        }
        Text(
            text = hintText,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .alpha(topControlsAlpha)
                .offset(y = topControlsOffsetY)
                .statusBarsPadding()
                .padding(top = 80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp)
        )

        // ─── Page Counter (multi mode) ──────────────────────────────────
        if (pageMode == PageMode.MULTI && scannedPageCount > 0) {
            Text(
                text = stringResource(R.string.scanner_page_count, scannedPageCount),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .alpha(topControlsAlpha)
                    .offset(y = topControlsOffsetY)
                    .statusBarsPadding()
                    .padding(top = 130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF00E676), Color(0xFF00BFA5))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), clip = false)
            )
        }

        // ─── Stability Indicator ────────────────────────────────────────
        if (detectionState.stabilityProgress > 0f && scannerMode == ScannerMode.AUTO) {
            CircularProgressIndicator(
                progress = { detectionState.stabilityProgress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .alpha(bottomControlsAlpha)
                    .offset(y = bottomControlsOffsetY)
                    .padding(bottom = 180.dp)
                    .size(48.dp),
                strokeWidth = 4.dp,
                color = if (detectionState.isStable) Color(0xFF00E676) else Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        // ─── Bottom Controls ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(bottomControlsAlpha)
                .offset(y = bottomControlsOffsetY)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .navigationBarsPadding()
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Left: page mode
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GlassIconButton(
                        icon = Iconsax.Bold.DocumentCopy,
                        contentDescription = "Page Mode",
                        isActive = pageMode == PageMode.MULTI,
                        enabled = !isCapturePipelineRunning,
                        onClick = onTogglePageMode
                    )
                }

                // Center: shutter
                ShutterButton(
                    enabled = !isCapturePipelineRunning,
                    onClick = {
                        triggerCapture(
                            pageMode = pageMode,
                            previewView = previewView,
                            context = context,
                            imageCapture = imageCapture,
                            cameraExecutor = cameraExecutor,
                            onMultiCaptureShutter = onMultiCaptureShutter,
                            onCaptureFreezeFrameReady = onCaptureFreezeFrameReady,
                            onImageCaptured = onImageCaptured
                        )
                    }
                )

                // Right: thumbnail stack (UI unchanged); anchor slot for fly animation in multi
                if (pageMode == PageMode.MULTI) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("thumbnailStackAnchor")
                            .onGloballyPositioned { coordinates ->
                                val pos = coordinates.positionInRoot()
                                stackAnchorCenter = Offset(
                                    pos.x + coordinates.size.width / 2f,
                                    pos.y + coordinates.size.height / 2f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (scannedPageCount > 0) {
                            ThumbnailStackButton(
                                count = scannedPageCount,
                                thumbnail = latestThumbnail,
                                enabled = !isCapturePipelineRunning,
                                onClick = onReviewPages
                            )
                        }
                    }
                } else if (scannedPageCount > 0) {
                    ThumbnailStackButton(
                        count = scannedPageCount,
                        thumbnail = latestThumbnail,
                        enabled = !isCapturePipelineRunning,
                        onClick = onReviewPages
                    )
                } else {
                    Spacer(modifier = Modifier.size(56.dp))
                }
            }
        }
    }

    // Auto-capture logic
    LaunchedEffect(detectionState.isStable, isCapturePipelineRunning) {
        if (isCapturePipelineRunning) return@LaunchedEffect
        if (detectionState.isStable && shouldAutoCapture() && !hasTriggeredAutoCapture) {
            hasTriggeredAutoCapture = true
            triggerCapture(
                pageMode = pageMode,
                previewView = previewView,
                context = context,
                imageCapture = imageCapture,
                cameraExecutor = cameraExecutor,
                onMultiCaptureShutter = onMultiCaptureShutter,
                onCaptureFreezeFrameReady = onCaptureFreezeFrameReady,
                onImageCaptured = onImageCaptured
            )
        }
        if (!detectionState.isStable) {
            hasTriggeredAutoCapture = false
        }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = Color.White,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_btn_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "icon_btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "icon_btn_border"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676) else tint,
        animationSpec = tween(300),
        label = "icon_tint"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun GlassTextButton(
    text: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "btn_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676) else Color.White,
        animationSpec = tween(300),
        label = "btn_text"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(36.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ThumbnailStackButton(
    count: Int,
    thumbnail: ImageBitmap? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbnail_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(56.dp)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null, // Custom scale animation instead of ripple
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Stack effect
        if (count > 1) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            )
        }
        
        // Front thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                androidx.compose.foundation.Image(
                    bitmap = thumbnail,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Iconsax.Bold.Gallery,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Badge
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ShutterButton(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "shutter_scale"
    )
    val coreScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "shutter_core_scale"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 0.78f,
        animationSpec = tween(durationMillis = 180),
        label = "shutter_ring_alpha"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 140),
        label = "shutter_icon_scale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.2f))
            .border(2.dp, Color.White.copy(alpha = ringAlpha), CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.18f))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(coreScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF4F4F4),
                            Color(0xFFDCDCDC)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Iconsax.Linear.Scan,
                contentDescription = stringResource(R.string.scanner_action_scan),
                tint = Color.Black.copy(alpha = 0.78f),
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
        }
    }
}

@Composable
internal fun DocumentDetectionOverlay(
    quad: DocumentQuad?,
    isStable: Boolean,
    modifier: Modifier = Modifier
) {
    val corners = quad?.points().orEmpty()
    val lineColor = if (isStable) Color(0xFF00E676) else Color.White.copy(alpha = 0.9f)

    Canvas(modifier = modifier.testTag("pdfDetectionOverlay")) {
        val containerSize = Size(size.width, size.height)
        val content = ScannerPreviewMapper.previewContentRect(containerSize)

        if (corners.size == 4 && quad != null) {
            val mapped = ScannerPreviewMapper.mapQuadToOffsets(quad, containerSize)
            val path = Path().apply {
                moveTo(mapped[0].x, mapped[0].y)
                lineTo(mapped[1].x, mapped[1].y)
                lineTo(mapped[2].x, mapped[2].y)
                lineTo(mapped[3].x, mapped[3].y)
                close()
            }
            // Inner fill
            drawPath(path = path, color = lineColor.copy(alpha = 0.15f))

            // Glowing outer stroke
            drawPath(path = path, color = lineColor.copy(alpha = 0.3f), style = Stroke(width = 12f))

            // Core stroke
            drawPath(path = path, color = lineColor.copy(alpha = 0.9f), style = Stroke(width = 4f))

            // Corner dots with glow
            mapped.forEach { point ->
                drawCircle(color = lineColor.copy(alpha = 0.2f), radius = 20f, center = point)
                drawCircle(color = Color.White, radius = 8f, center = point)
            }
        } else {
            // Scanning indicator when no document is found
            val cornerLength = 60f
            val strokeWidth = 6f
            val color = Color.White.copy(alpha = 0.5f)

            // Padding inside the camera view for the viewfinder
            val padding = 48.dp.toPx()
            val vfLeft = content.left + padding
            val vfTop = content.top + padding
            val vfRight = content.left + content.width - padding
            val vfBottom = content.top + content.height - padding

            // Draw border
            drawRect(
                color = color.copy(alpha = 0.2f),
                topLeft = Offset(vfLeft, vfTop),
                size = androidx.compose.ui.geometry.Size(vfRight - vfLeft, vfBottom - vfTop),
                style = Stroke(width = 2f)
            )

            // Top Left
            drawLine(color, Offset(vfLeft, vfTop), Offset(vfLeft + cornerLength, vfTop), strokeWidth)
            drawLine(color, Offset(vfLeft, vfTop), Offset(vfLeft, vfTop + cornerLength), strokeWidth)

            // Top Right
            drawLine(color, Offset(vfRight, vfTop), Offset(vfRight - cornerLength, vfTop), strokeWidth)
            drawLine(color, Offset(vfRight, vfTop), Offset(vfRight, vfTop + cornerLength), strokeWidth)

            // Bottom Left
            drawLine(color, Offset(vfLeft, vfBottom), Offset(vfLeft + cornerLength, vfBottom), strokeWidth)
            drawLine(color, Offset(vfLeft, vfBottom), Offset(vfLeft, vfBottom - cornerLength), strokeWidth)

            // Bottom Right
            drawLine(color, Offset(vfRight, vfBottom), Offset(vfRight - cornerLength, vfBottom), strokeWidth)
            drawLine(color, Offset(vfRight, vfBottom), Offset(vfRight, vfBottom - cornerLength), strokeWidth)
        }
    }
}

private fun triggerCapture(
    pageMode: PageMode,
    previewView: PreviewView,
    context: android.content.Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onMultiCaptureShutter: () -> Unit,
    onCaptureFreezeFrameReady: (Bitmap?) -> Unit,
    onImageCaptured: (File) -> Unit
) {
    if (pageMode == PageMode.MULTI) {
        onMultiCaptureShutter()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        cameraExecutor.execute {
            val freezeFrame = capturePreviewSnapshot(previewView)
            mainExecutor.execute {
                onCaptureFreezeFrameReady(freezeFrame)
            }
        }
    }
    takePicture(context, imageCapture, cameraExecutor, onImageCaptured)
}

private fun capturePreviewSnapshot(previewView: PreviewView): Bitmap? =
    runCatching { previewView.bitmap }.getOrNull()

private fun takePicture(
    context: android.content.Context,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService,
    onImageCaptured: (File) -> Unit
) {
    val file = File(context.cacheDir, "temp_scan_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(file)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}

