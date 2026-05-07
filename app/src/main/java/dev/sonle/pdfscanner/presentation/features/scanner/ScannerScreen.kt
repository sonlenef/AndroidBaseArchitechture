package dev.sonle.pdfscanner.presentation.features.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.core.scanner.analyzer.DocumentAnalyzer
import dev.sonle.pdfscanner.core.scanner.detection.DocumentDetector
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.postprocess.SubpixelCornerRefiner
import dev.sonle.pdfscanner.core.scanner.smoothing.QuadKalmanSmoother
import dev.sonle.pdfscanner.core.scanner.stability.StabilityTracker
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val detectionState by viewModel.detection.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (hasCameraPermission) {
                when (val state = uiState) {
                    is ScannerUiState.Idle -> {
                        CameraView(
                            detectionState = detectionState,
                            onDetectionUpdated = viewModel::onDetectionUpdated,
                            shouldAutoCapture = viewModel::shouldAutoCapture,
                            onImageCaptured = { file ->
                                viewModel.processCapturedImage(file)
                            }
                        )
                    }
                    is ScannerUiState.Processing -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.scanner_processing),
                                modifier = Modifier.padding(top = 80.dp)
                            )
                        }
                    }
                    is ScannerUiState.Success -> {
                        ResultView(
                            bitmap = state.bitmap,
                            onBack = { viewModel.reset() },
                            onSave = { viewModel.saveAsPdf() }
                        )
                    }
                    is ScannerUiState.SaveSuccess -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.scanner_saved_file_label),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(state.file.absolutePath, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
                            Button(onClick = { viewModel.reset() }) {
                                Text(stringResource(R.string.scanner_continue_scan))
                            }
                        }
                    }
                    is ScannerUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.scanner_error_prefix, state.message),
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.reset() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.scanner_camera_permission_required))
                }
            }
        }
    }
}

@Composable
fun CameraView(
    detectionState: DetectionUiState,
    onDetectionUpdated: (DocumentQuad?, Float, Boolean) -> Unit,
    shouldAutoCapture: () -> Boolean,
    onImageCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val detector: DocumentDetector = koinInject()
    val subpixelCornerRefiner: SubpixelCornerRefiner = koinInject()
    val quadKalmanSmoother: QuadKalmanSmoother = koinInject()
    val stabilityTracker: StabilityTracker = koinInject()
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    var hasTriggeredAutoCapture by remember { mutableStateOf(false) }

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
                cameraProvider.bindToLifecycle(
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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        DocumentDetectionOverlay(
            quad = detectionState.quad,
            isStable = detectionState.isStable,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        )

        Text(
            text = stringResource(R.string.scanner_frame_auto_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        if (detectionState.stabilityProgress > 0f) {
            CircularProgressIndicator(
                progress = { detectionState.stabilityProgress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 126.dp)
                    .size(36.dp),
                strokeWidth = 3.dp,
                color = if (detectionState.isStable) Color(0xFF00E676) else Color.White
            )
        }

        Button(
            onClick = {
                takePicture(context, imageCapture, cameraExecutor, onImageCaptured)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(80.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(stringResource(R.string.scanner_action_scan))
        }
    }

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
            // PreviewView uses FIT_CENTER, so the camera stream occupies only a centered content rect.
            // Map normalized points into that rect to keep overlay aligned with visible preview pixels.
            val previewAspectRatio = 3f / 4f // 4:3 sensor stream displayed in portrait
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
            drawPath(path = path, color = lineColor.copy(alpha = 0.18f))
            drawPath(path = path, color = lineColor.copy(alpha = 0.95f), style = Stroke(width = 6f))
            drawPath(path = path, color = Color.Black.copy(alpha = 0.3f), style = Stroke(width = 2f))

            mapped.forEach { point ->
                drawCircle(color = Color.White, radius = 16f, center = point)
                drawCircle(color = Color(0xFF00E676), radius = 8f, center = point)
            }
        }
    } else {
        Box(
            modifier = modifier
                .testTag("pdfDetectionOverlay")
                .padding(horizontal = 24.dp, vertical = 64.dp)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

@Composable
fun ResultView(bitmap: Bitmap, onBack: () -> Unit, onSave: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Scanned Document",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.scanner_capture_again))
            }
            Button(onClick = onSave) {
                Text(stringResource(R.string.scanner_save_pdf))
            }
        }
    }
}
