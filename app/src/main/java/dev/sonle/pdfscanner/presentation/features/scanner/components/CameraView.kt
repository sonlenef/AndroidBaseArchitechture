package dev.sonle.pdfscanner.presentation.features.scanner.components

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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannerMode
import org.koin.compose.koinInject
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraView(
    scannerMode: ScannerMode,
    pageMode: PageMode,
    scannedPageCount: Int,
    detectionState: DetectionUiState,
    onDetectionUpdated: (DocumentQuad?, Float, Boolean) -> Unit,
    shouldAutoCapture: () -> Boolean,
    onImageCaptured: (File) -> Unit,
    onToggleScannerMode: () -> Unit,
    onTogglePageMode: () -> Unit,
    onReviewPages: () -> Unit,
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

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
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

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Camera preview
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Detection overlay
        DocumentDetectionOverlay(
            quad = detectionState.quad,
            isStable = detectionState.isStable,
            modifier = Modifier.align(Alignment.Center).fillMaxSize()
        )

        // ─── Top Bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            GlassIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.scanner_close),
                onClick = onClose
            )

            // Flash toggle
            GlassIconButton(
                icon = Icons.Rounded.Star,
                contentDescription = "Flash",
                tint = if (isFlashOn) Color(0xFFFFD700) else Color.White,
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
                // Left controls: Auto/Manual toggle & Single/Multi toggle
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassIconButton(
                        icon = if (scannerMode == ScannerMode.AUTO) Icons.Rounded.Star else Icons.Rounded.Edit,
                        contentDescription = "Scanner Mode",
                        isActive = scannerMode == ScannerMode.AUTO,
                        onClick = onToggleScannerMode
                    )
                    
                    GlassIconButton(
                        icon = if (pageMode == PageMode.MULTI) Icons.Rounded.List else Icons.Rounded.Done,
                        contentDescription = "Page Mode",
                        isActive = pageMode == PageMode.MULTI,
                        onClick = onTogglePageMode
                    )
                }

                // Shutter button (Center)
                ShutterButton(
                    onClick = {
                        takePicture(context, imageCapture, cameraExecutor, onImageCaptured)
                    }
                )

                // Right control: Thumbnail stack or placeholder
                if (scannedPageCount > 0) {
                    ThumbnailStackButton(
                        count = scannedPageCount,
                        onClick = onReviewPages
                    )
                } else {
                    Spacer(modifier = Modifier.size(56.dp))
                }
            }
        }
    }

    // Auto-capture logic
    LaunchedEffect(detectionState.isStable) {
        if (detectionState.isStable && shouldAutoCapture() && !hasTriggeredAutoCapture) {
            hasTriggeredAutoCapture = true
            takePicture(context, imageCapture, cameraExecutor, onImageCaptured)
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
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "icon_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "icon_border"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E676) else tint,
        animationSpec = tween(300),
        label = "icon_tint"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
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
private fun ThumbnailStackButton(
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clickable(onClick = onClick),
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
        
        // Front image placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
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
private fun ShutterButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(150),
        label = "shutter_scale"
    )

    val innerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(150),
        label = "shutter_inner_scale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .border(4.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle for shutter feel
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(innerScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.8f))
                    )
                )
                .shadow(elevation = if (isPressed) 2.dp else 6.dp, shape = CircleShape)
        )
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

    if (corners.size == 4) {
        Canvas(modifier = modifier.testTag("pdfDetectionOverlay")) {
            val previewAspectRatio = 3f / 4f
            val viewAspectRatio = size.width / size.height
            val contentWidth: Float
            val contentHeight: Float
            if (viewAspectRatio > previewAspectRatio) {
                contentHeight = size.height
                contentWidth = contentHeight * previewAspectRatio
            } else {
                contentWidth = size.width
                contentHeight = contentWidth / previewAspectRatio
            }
            val contentLeft = (size.width - contentWidth) / 2f
            val contentTop = (size.height - contentHeight) / 2f

            val mapped = corners.map {
                Offset(
                    x = contentLeft + (it.x * contentWidth),
                    y = contentTop + (it.y * contentHeight)
                )
            }
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
        }
    } else {
        // Scanning indicator when no document is found
        Box(
            modifier = modifier
                .testTag("pdfDetectionOverlay")
                .padding(horizontal = 32.dp, vertical = 80.dp)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Corner highlights
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerLength = 40f
                val strokeWidth = 4f
                val color = Color.White.copy(alpha = 0.6f)

                // Top Left
                drawLine(color, Offset.Zero, Offset(cornerLength, 0f), strokeWidth)
                drawLine(color, Offset.Zero, Offset(0f, cornerLength), strokeWidth)

                // Top Right
                drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)

                // Bottom Left
                drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)

                // Bottom Right
                drawLine(
                    color,
                    Offset(size.width, size.height),
                    Offset(size.width - cornerLength, size.height),
                    strokeWidth
                )
                drawLine(
                    color,
                    Offset(size.width, size.height),
                    Offset(size.width, size.height - cornerLength),
                    strokeWidth
                )
            }
        }
    }
}

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

