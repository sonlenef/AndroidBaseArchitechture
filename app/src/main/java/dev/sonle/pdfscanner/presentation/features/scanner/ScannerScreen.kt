package dev.sonle.pdfscanner.presentation.features.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import dev.sonle.pdfscanner.R
import dev.sonle.pdfscanner.presentation.features.scanner.components.CameraView
import dev.sonle.pdfscanner.presentation.features.scanner.components.CropEditorView
import dev.sonle.pdfscanner.presentation.features.scanner.components.FilterEditorView
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val detectionState by viewModel.detection.collectAsState()
    val captureOverlay by viewModel.captureOverlay.collectAsState()

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
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        // Start a fresh scanner session each time this screen is opened.
        viewModel.reset()
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Handle back press based on current state
    BackHandler {
        if (!viewModel.onBackPressed()) {
            onNavigateBack()
        }
    }

    if (!hasCameraPermission) {
        PermissionRequiredView()
        return
    }

    when (val state = uiState) {
        is ScannerUiState.Camera -> {
            CameraView(
                scannerMode = state.scannerMode,
                pageMode = state.pageMode,
                scannedPageCount = state.scannedPages.size,
                isCapturePipelineRunning = state.isPipelineRunning,
                captureOverlay = captureOverlay,
                latestThumbnail = state.scannedPages.lastOrNull()
                    ?.processedBitmap
                    ?.asImageBitmap(),
                detectionState = detectionState,
                onDetectionUpdated = viewModel::onDetectionUpdated,
                shouldAutoCapture = viewModel::shouldAutoCapture,
                onImageCaptured = { file -> viewModel.processCapturedImage(file) },
                onMultiCaptureShutter = { viewModel.beginMultiCaptureOverlay() },
                onCaptureFreezeFrameReady = { frame -> viewModel.applyCaptureFreezeFrame(frame) },
                onToggleScannerMode = viewModel::toggleScannerMode,
                onTogglePageMode = viewModel::togglePageMode,
                onReviewPages = viewModel::onReviewPages,
                onCaptureAnimationStepFinished = viewModel::onCaptureAnimationStepFinished,
                onClose = onNavigateBack
            )
        }

        is ScannerUiState.CropEditor -> {
            CropEditorView(
                originalBitmap = state.originalBitmap,
                editableQuad = state.editableQuad,
                onQuadUpdated = viewModel::onCropQuadUpdated,
                onConfirm = viewModel::onCropConfirmed,
                onRetake = viewModel::onCropRetake,
                onAutoDetect = viewModel::onAutoDetectInCropEditor
            )
        }

        is ScannerUiState.FilterEditor -> {
            FilterEditorView(
                croppedBitmap = state.croppedBitmap,
                previewBitmap = state.previewBitmap,
                selectedFilter = state.selectedFilter,
                rotation = state.rotation,
                onFilterSelected = viewModel::onFilterSelected,
                onRotateLeft = viewModel::onRotateLeft,
                onRotateRight = viewModel::onRotateRight,
                onConfirm = viewModel::onFilterConfirmed,
                onBack = viewModel::onFilterBack
            )
        }

        is ScannerUiState.PageReview -> {
            ScannerReviewScreen(
                pages = state.pages,
                selectedPageIndex = state.selectedPageIndex,
                pageMode = state.pageMode,
                onSelectPage = viewModel::onSelectPage,
                onEditPage = viewModel::onEditPage,
                onDeletePage = viewModel::onDeletePage,
                onAddMorePages = viewModel::onAddMorePages,
                onSavePdf = viewModel::savePdf,
                onBack = viewModel::onReviewBack
            )
        }

        is ScannerUiState.Processing -> {
            ProcessingView()
        }

        is ScannerUiState.SaveSuccess -> {
            SaveSuccessView(
                filePath = state.file.absolutePath,
                onContinue = viewModel::reset,
                onBackHome = {
                    viewModel.reset()
                    onNavigateBack()
                }
            )
        }

        is ScannerUiState.Error -> {
            ErrorView(
                message = state.message,
                onRetry = viewModel::reset
            )
        }
    }
}

// ─── Sub-screens ─────────────────────────────────────────────────────────────

@Composable
private fun PermissionRequiredView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Icon(
                Icons.Rounded.Check, // You'd ideally use a camera icon here, but Check is what's available without adding imports
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.scanner_camera_permission_required),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProcessingView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)), // semi-transparent background if overlaid, but full black is fine
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(40.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF00E676),
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.scanner_processing),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SaveSuccessView(
    filePath: String,
    onContinue: () -> Unit,
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .background(Color.DarkGray.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .padding(32.dp)
        ) {
            // Success icon with glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF00E676).copy(alpha = 0.2f), CircleShape)
                    .padding(12.dp)
                    .background(Color(0xFF00E676), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.scanner_saved_file_label),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = filePath,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(R.string.scanner_continue_scan),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    stringResource(R.string.scanner_back_home),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(32.dp)
        ) {
            Text(
                text = "Oops!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.scanner_error_prefix, message),
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(
                    stringResource(R.string.retry),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
