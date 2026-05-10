package dev.sonle.pdfscanner.presentation.features.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.core.scanner.model.DocumentQuad
import dev.sonle.pdfscanner.core.scanner.model.NormalizedPoint
import dev.sonle.pdfscanner.core.scanner.processing.CropProcessor
import dev.sonle.pdfscanner.core.scanner.processing.ImageFilterProcessor
import dev.sonle.pdfscanner.core.util.OpenCVScanner
import dev.sonle.pdfscanner.domain.model.RecentScan
import dev.sonle.pdfscanner.domain.usecase.AddRecentScanUseCase
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import dev.sonle.pdfscanner.presentation.features.scanner.model.ImageFilter
import dev.sonle.pdfscanner.presentation.features.scanner.model.PageMode
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannedPage
import dev.sonle.pdfscanner.presentation.features.scanner.model.ScannerMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

// ─── UI State ────────────────────────────────────────────────────────────────

sealed interface ScannerUiState {
    data class Camera(
        val scannerMode: ScannerMode = ScannerMode.AUTO,
        val pageMode: PageMode = PageMode.SINGLE,
        val scannedPages: List<ScannedPage> = emptyList()
    ) : ScannerUiState

    data class CropEditor(
        val originalBitmap: Bitmap,
        val detectedQuad: DocumentQuad?,
        val editableQuad: DocumentQuad
    ) : ScannerUiState

    data class FilterEditor(
        val originalBitmap: Bitmap,
        val croppedBitmap: Bitmap,
        val selectedFilter: ImageFilter = ImageFilter.ORIGINAL,
        val previewBitmap: Bitmap,
        val cropQuad: DocumentQuad,
        val rotation: Int = 0
    ) : ScannerUiState

    data class PageReview(
        val pages: List<ScannedPage>,
        val selectedPageIndex: Int = 0,
        val pageMode: PageMode
    ) : ScannerUiState

    object Processing : ScannerUiState

    data class SaveSuccess(val file: File) : ScannerUiState

    data class Error(val message: String) : ScannerUiState
}

// ─── Detection State (for camera overlay) ─────────────────────────────────────

data class DetectionUiState(
    val quad: DocumentQuad? = null,
    val stabilityProgress: Float = 0f,
    val isStable: Boolean = false
)

// ─── ViewModel ─────────────────────────────────────────────────────────────────

class ScannerViewModel(
    private val savePdfUseCase: SavePdfUseCase,
    private val addRecentScanUseCase: AddRecentScanUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScannerUiState>(
        ScannerUiState.Camera()
    )
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _detection = MutableStateFlow(DetectionUiState())
    val detection: StateFlow<DetectionUiState> = _detection.asStateFlow()

    // Accumulated pages for multi-page mode
    private val scannedPages = mutableListOf<ScannedPage>()

    // Persisted mode across captures
    private var currentScannerMode = ScannerMode.AUTO
    private var currentPageMode = PageMode.SINGLE
    private var autoCaptureArmed = true

    // Index of page being edited (null = new page)
    private var editingPageIndex: Int? = null
    // Insert position when adding a page from review (null = append)
    private var pendingInsertIndex: Int? = null

    // ─── Camera Actions ──────────────────────────────────────────────────────

    fun toggleScannerMode() {
        currentScannerMode = when (currentScannerMode) {
            ScannerMode.AUTO -> ScannerMode.MANUAL
            ScannerMode.MANUAL -> ScannerMode.AUTO
        }
        val currentState = _uiState.value
        if (currentState is ScannerUiState.Camera) {
            _uiState.value = currentState.copy(scannerMode = currentScannerMode)
        }
    }

    fun togglePageMode() {
        currentPageMode = when (currentPageMode) {
            PageMode.SINGLE -> PageMode.MULTI
            PageMode.MULTI -> PageMode.SINGLE
        }
        val currentState = _uiState.value
        if (currentState is ScannerUiState.Camera) {
            _uiState.value = currentState.copy(pageMode = currentPageMode)
        }
    }

    fun onDetectionUpdated(
        quad: DocumentQuad?,
        stabilityProgress: Float,
        isStable: Boolean
    ) {
        _detection.update {
            it.copy(
                quad = quad,
                stabilityProgress = stabilityProgress,
                isStable = isStable
            )
        }
    }

    fun shouldAutoCapture(): Boolean {
        if (_uiState.value !is ScannerUiState.Camera) return false
        if (currentScannerMode != ScannerMode.AUTO) return false
        if (!_detection.value.isStable || !autoCaptureArmed) return false
        autoCaptureArmed = false
        return true
    }

    // ─── Image Capture → Crop Editor ─────────────────────────────────────────

    fun processCapturedImage(file: File) {
        editingPageIndex = null
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Processing
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(file.absolutePath)
                }
                if (bitmap == null) {
                    _uiState.value = ScannerUiState.Error("Failed to decode image")
                    return@launch
                }
                val exifOrientation = withContext(Dispatchers.IO) {
                    runCatching {
                        ExifInterface(file.absolutePath).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                        )
                    }.getOrDefault(ExifInterface.ORIENTATION_UNDEFINED)
                }
                val normalizedBitmap = applyExifOrientation(bitmap, exifOrientation)

                // Detect document corners
                val corners = withContext(Dispatchers.Default) {
                    OpenCVScanner.findDocumentCorners(normalizedBitmap)
                }

                val detectedQuadRaw = corners?.let { cornerList ->
                    val sorted = sortCorners(cornerList)
                    DocumentQuad(
                        tl = NormalizedPoint(
                            (sorted[0].x / normalizedBitmap.width).toFloat(),
                            (sorted[0].y / normalizedBitmap.height).toFloat()
                        ),
                        tr = NormalizedPoint(
                            (sorted[1].x / normalizedBitmap.width).toFloat(),
                            (sorted[1].y / normalizedBitmap.height).toFloat()
                        ),
                        br = NormalizedPoint(
                            (sorted[2].x / normalizedBitmap.width).toFloat(),
                            (sorted[2].y / normalizedBitmap.height).toFloat()
                        ),
                        bl = NormalizedPoint(
                            (sorted[3].x / normalizedBitmap.width).toFloat(),
                            (sorted[3].y / normalizedBitmap.height).toFloat()
                        ),
                        confidence = 1f
                    )
                }
                val detectedQuad = detectedQuadRaw?.takeIf { isQuadSane(it) }
                val cameraQuad = _detection.value.quad?.takeIf { isQuadSane(it) }
                val editableQuad = detectedQuad ?: cameraQuad ?: CropProcessor.defaultQuad()

                // For both SINGLE and MULTI, show Review after capture with a cropped preview.
                // User enters edit flow explicitly from Review via "Edit".
                val cropped = withContext(Dispatchers.Default) {
                    CropProcessor.cropAndTransform(normalizedBitmap, editableQuad)
                }
                val defaultFiltered = withContext(Dispatchers.Default) {
                    ImageFilterProcessor.applySharpen(cropped)
                }
                val page = ScannedPage(
                    originalBitmap = normalizedBitmap,
                    processedBitmap = defaultFiltered,
                    cropQuad = editableQuad,
                    filter = ImageFilter.SHARPEN,
                    rotation = 0
                )
                val selectedIndex = pendingInsertIndex
                    ?.coerceIn(0, scannedPages.size)
                    ?.also { insertIndex ->
                        scannedPages.add(insertIndex, page)
                    }
                    ?: run {
                        scannedPages.add(page)
                        scannedPages.lastIndex
                    }
                pendingInsertIndex = null
                _uiState.value = ScannerUiState.PageReview(
                    pages = scannedPages.toList(),
                    selectedPageIndex = selectedIndex,
                    pageMode = currentPageMode
                )
            } catch (e: Exception) {
                Timber.e(e, "Error processing captured image")
                _uiState.value = ScannerUiState.Error(e.message ?: "Unknown error")
            } finally {
                if (file.exists()) file.delete()
            }
        }
    }

    // ─── Crop Editor Actions ─────────────────────────────────────────────────

    fun onCropQuadUpdated(quad: DocumentQuad) {
        val state = _uiState.value
        if (state is ScannerUiState.CropEditor) {
            _uiState.value = state.copy(editableQuad = quad)
        }
    }

    fun onCropConfirmed() {
        val state = _uiState.value
        if (state !is ScannerUiState.CropEditor) return

        viewModelScope.launch {
            _uiState.value = ScannerUiState.Processing
            try {
                val cropped = withContext(Dispatchers.Default) {
                    CropProcessor.cropAndTransform(state.originalBitmap, state.editableQuad)
                }
                val defaultFiltered = withContext(Dispatchers.Default) {
                    ImageFilterProcessor.applySharpen(cropped)
                }
                _uiState.value = ScannerUiState.FilterEditor(
                    originalBitmap = state.originalBitmap,
                    croppedBitmap = cropped,
                    selectedFilter = ImageFilter.SHARPEN,
                    previewBitmap = defaultFiltered,
                    cropQuad = state.editableQuad
                )
            } catch (e: Exception) {
                Timber.e(e, "Error cropping image")
                _uiState.value = ScannerUiState.Error("Failed to crop: ${e.message}")
            }
        }
    }

    fun onAutoDetectInCropEditor() {
        val state = _uiState.value
        if (state !is ScannerUiState.CropEditor) return

        viewModelScope.launch {
            val corners = withContext(Dispatchers.Default) {
                OpenCVScanner.findDocumentCorners(state.originalBitmap)
            }
            val detectedQuadRaw = corners?.let { cornerList ->
                val sorted = sortCorners(cornerList)
                DocumentQuad(
                    tl = NormalizedPoint(
                        (sorted[0].x / state.originalBitmap.width).toFloat(),
                        (sorted[0].y / state.originalBitmap.height).toFloat()
                    ),
                    tr = NormalizedPoint(
                        (sorted[1].x / state.originalBitmap.width).toFloat(),
                        (sorted[1].y / state.originalBitmap.height).toFloat()
                    ),
                    br = NormalizedPoint(
                        (sorted[2].x / state.originalBitmap.width).toFloat(),
                        (sorted[2].y / state.originalBitmap.height).toFloat()
                    ),
                    bl = NormalizedPoint(
                        (sorted[3].x / state.originalBitmap.width).toFloat(),
                        (sorted[3].y / state.originalBitmap.height).toFloat()
                    ),
                    confidence = 1f
                )
            }
            val detectedQuad = detectedQuadRaw?.takeIf { isQuadSane(it) } ?: state.editableQuad

            _uiState.value = state.copy(
                detectedQuad = detectedQuad,
                editableQuad = detectedQuad
            )
        }
    }

    fun onCropRetake() {
        goBackToCamera()
    }

    // ─── Filter Editor Actions ───────────────────────────────────────────────

    fun onFilterSelected(filter: ImageFilter) {
        val state = _uiState.value
        if (state !is ScannerUiState.FilterEditor) return

        viewModelScope.launch {
            val bitmapToFilter = if (state.rotation != 0) {
                CropProcessor.rotateBitmap(state.croppedBitmap, state.rotation)
            } else {
                state.croppedBitmap
            }
            val filtered = withContext(Dispatchers.Default) {
                ImageFilterProcessor.applyFilter(bitmapToFilter, filter)
            }
            _uiState.value = state.copy(
                selectedFilter = filter,
                previewBitmap = filtered
            )
        }
    }

    fun onRotateLeft() {
        val state = _uiState.value
        if (state !is ScannerUiState.FilterEditor) return
        val newRotation = (state.rotation - 90 + 360) % 360
        updateRotation(state, newRotation)
    }

    fun onRotateRight() {
        val state = _uiState.value
        if (state !is ScannerUiState.FilterEditor) return
        val newRotation = (state.rotation + 90) % 360
        updateRotation(state, newRotation)
    }

    private fun updateRotation(state: ScannerUiState.FilterEditor, newRotation: Int) {
        viewModelScope.launch {
            val rotated = withContext(Dispatchers.Default) {
                val rotatedCrop = CropProcessor.rotateBitmap(state.croppedBitmap, newRotation)
                ImageFilterProcessor.applyFilter(rotatedCrop, state.selectedFilter)
            }
            _uiState.value = state.copy(
                rotation = newRotation,
                previewBitmap = rotated
            )
        }
    }

    fun onFilterConfirmed() {
        val state = _uiState.value
        if (state !is ScannerUiState.FilterEditor) return

        val page = ScannedPage(
            originalBitmap = state.originalBitmap,
            processedBitmap = state.previewBitmap,
            cropQuad = state.cropQuad,
            filter = state.selectedFilter,
            rotation = state.rotation
        )

        val editIndex = editingPageIndex
        if (editIndex != null && editIndex in scannedPages.indices) {
            scannedPages[editIndex] = page
        } else {
            scannedPages.add(page)
        }
        editingPageIndex = null

        when (currentPageMode) {
            PageMode.SINGLE -> {
                _uiState.value = ScannerUiState.PageReview(
                    pages = scannedPages.toList(),
                    selectedPageIndex = scannedPages.lastIndex,
                    pageMode = currentPageMode
                )
            }
            PageMode.MULTI -> {
                _uiState.value = ScannerUiState.PageReview(
                    pages = scannedPages.toList(),
                    selectedPageIndex = scannedPages.lastIndex,
                    pageMode = currentPageMode
                )
            }
        }
    }

    fun onFilterBack() {
        val state = _uiState.value
        if (state !is ScannerUiState.FilterEditor) return
        _uiState.value = ScannerUiState.CropEditor(
            originalBitmap = state.originalBitmap,
            detectedQuad = null,
            editableQuad = state.cropQuad
        )
    }

    // ─── Page Review Actions ─────────────────────────────────────────────────

    fun onAddMorePages() {
        val reviewState = _uiState.value as? ScannerUiState.PageReview
        pendingInsertIndex = reviewState
            ?.selectedPageIndex
            ?.plus(1)
            ?.coerceAtMost(scannedPages.size)
        goBackToCamera(keepPendingInsert = true)
    }

    fun onDeletePage(index: Int) {
        if (index in scannedPages.indices) {
            scannedPages.removeAt(index)
        }
        if (scannedPages.isEmpty()) {
            goBackToCamera()
        } else {
            _uiState.value = ScannerUiState.PageReview(
                pages = scannedPages.toList(),
                selectedPageIndex = (index - 1).coerceAtLeast(0),
                pageMode = currentPageMode
            )
        }
    }

    fun onEditPage(index: Int) {
        if (index !in scannedPages.indices) return
        editingPageIndex = index
        val page = scannedPages[index]
        _uiState.value = ScannerUiState.CropEditor(
            originalBitmap = page.originalBitmap,
            detectedQuad = page.cropQuad,
            editableQuad = page.cropQuad ?: CropProcessor.defaultQuad()
        )
    }

    fun onReorderPages(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in scannedPages.indices || toIndex !in scannedPages.indices) return
        val page = scannedPages.removeAt(fromIndex)
        scannedPages.add(toIndex, page)
        _uiState.value = ScannerUiState.PageReview(
            pages = scannedPages.toList(),
            selectedPageIndex = toIndex,
            pageMode = currentPageMode
        )
    }

    fun onSelectPage(index: Int) {
        val state = _uiState.value
        if (state is ScannerUiState.PageReview) {
            _uiState.value = state.copy(selectedPageIndex = index)
        }
    }

    fun onReviewPages() {
        if (scannedPages.isNotEmpty()) {
            _uiState.value = ScannerUiState.PageReview(
                pages = scannedPages.toList(),
                selectedPageIndex = scannedPages.lastIndex,
                pageMode = currentPageMode
            )
        }
    }

    /**
     * Toolbar / system back from Page Review.
     * In **single** mode: clears the session so the camera is fresh (no thumbnail stack).
     * In **multi** mode: returns to camera keeping scanned pages.
     */
    fun onReviewBack() {
        if (clearsScanSessionOnReviewBack(currentPageMode)) {
            scannedPages.clear()
            editingPageIndex = null
            pendingInsertIndex = null
            autoCaptureArmed = true
            _detection.value = DetectionUiState()
            _uiState.value = ScannerUiState.Camera(
                scannerMode = currentScannerMode,
                pageMode = currentPageMode,
                scannedPages = emptyList()
            )
        } else {
            goBackToCamera()
        }
    }

    // ─── Save PDF ────────────────────────────────────────────────────────────

    fun savePdf() {
        if (scannedPages.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Processing
            val pageCount = scannedPages.size
            val bitmaps = scannedPages.map { it.processedBitmap }
            val result = savePdfUseCase(bitmaps, "Scan_${System.currentTimeMillis()}.pdf")
            result.onSuccess { file ->
                runCatching {
                    addRecentScanUseCase(
                        RecentScan(
                            id = 0,
                            fileName = file.name,
                            filePath = file.absolutePath,
                            pageCount = pageCount,
                            fileSizeBytes = file.length(),
                            savedAt = System.currentTimeMillis()
                        )
                    )
                }.onFailure { error ->
                    Timber.e(error, "Failed to record recent scan metadata")
                }
                _uiState.value = ScannerUiState.SaveSuccess(file)
            }.onFailure { e ->
                _uiState.value = ScannerUiState.Error("Failed to save PDF: ${e.message}")
            }
        }
    }

    // ─── Navigation Helpers ──────────────────────────────────────────────────

    private fun goBackToCamera(keepPendingInsert: Boolean = false) {
        if (!keepPendingInsert) {
            pendingInsertIndex = null
        }
        _detection.value = DetectionUiState()
        autoCaptureArmed = true
        _uiState.value = ScannerUiState.Camera(
            scannerMode = currentScannerMode,
            pageMode = currentPageMode,
            scannedPages = scannedPages.toList()
        )
    }

    fun reset() {
        scannedPages.clear()
        editingPageIndex = null
        pendingInsertIndex = null
        autoCaptureArmed = true
        _detection.value = DetectionUiState()
        _uiState.value = ScannerUiState.Camera(
            scannerMode = currentScannerMode,
            pageMode = currentPageMode
        )
    }

    fun onBackPressed(): Boolean {
        return when (_uiState.value) {
            is ScannerUiState.CropEditor -> {
                goBackToCamera()
                true
            }
            is ScannerUiState.FilterEditor -> {
                onFilterBack()
                true
            }
            is ScannerUiState.PageReview -> {
                onReviewBack()
                true
            }
            is ScannerUiState.Error -> {
                goBackToCamera()
                true
            }
            is ScannerUiState.SaveSuccess -> {
                reset()
                true
            }
            else -> false // Camera state → let system handle
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private fun sortCorners(points: List<org.opencv.core.Point>): List<org.opencv.core.Point> {
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()
        val sortedByDiff = points.sortedBy { it.y - it.x }
        val tr = sortedByDiff.first()
        val bl = sortedByDiff.last()
        return listOf(tl, tr, br, bl)
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return if (rotation == 0) bitmap else CropProcessor.rotateBitmap(bitmap, rotation)
    }

    private fun quadArea(quad: DocumentQuad): Float {
        val points = quad.points()
        var sum = 0f
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return kotlin.math.abs(sum) / 2f
    }

    private fun isQuadSane(quad: DocumentQuad): Boolean {
        val area = quadArea(quad)
        return area in 0.02f..0.98f
    }
}
