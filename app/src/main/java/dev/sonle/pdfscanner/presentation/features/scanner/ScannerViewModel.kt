package dev.sonle.pdfscanner.presentation.features.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sonle.pdfscanner.core.util.OpenCVScanner
import dev.sonle.pdfscanner.domain.usecase.SavePdfUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Processing : ScannerUiState()
    data class Success(val bitmap: Bitmap) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
    data class SaveSuccess(val file: File) : ScannerUiState()
}

class ScannerViewModel(
    private val savePdfUseCase: SavePdfUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var currentBitmap: Bitmap? = null

    fun processCapturedImage(file: File) {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Processing
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap == null) {
                    _uiState.value = ScannerUiState.Error("Failed to decode image")
                    return@launch
                }

                // 1. Tìm góc tài liệu
                val corners = OpenCVScanner.findDocumentCorners(bitmap)
                
                if (corners != null) {
                    // 2. Thực hiện biến đổi phối cảnh và lọc B&W
                    val processedBitmap = OpenCVScanner.perspectiveTransform(bitmap, corners)
                    currentBitmap = processedBitmap
                    _uiState.value = ScannerUiState.Success(processedBitmap)
                } else {
                    _uiState.value = ScannerUiState.Error("No document found in image. Please try again.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing image")
                _uiState.value = ScannerUiState.Error(e.message ?: "Unknown error")
            } finally {
                // Xoá file tạm sau khi xử lý xong
                if (file.exists()) file.delete()
            }
        }
    }

    fun saveAsPdf() {
        val bitmap = currentBitmap ?: return
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Processing
            val result = savePdfUseCase(listOf(bitmap), "Scan_${System.currentTimeMillis()}.pdf")
            result.onSuccess { file ->
                _uiState.value = ScannerUiState.SaveSuccess(file)
            }.onFailure { e ->
                _uiState.value = ScannerUiState.Error("Failed to save PDF: ${e.message}")
            }
        }
    }

    fun reset() {
        currentBitmap = null
        _uiState.value = ScannerUiState.Idle
    }
}
