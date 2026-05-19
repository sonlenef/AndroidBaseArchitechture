package dev.sonle.pdfscanner.presentation.features.scanner

import dev.sonle.pdfscanner.domain.model.ExportedPdf

/**
 * One-shot UI events for the scanner screen (snackbars, navigation, etc.).
 */
sealed interface ScannerUiEffect {
    data class SaveCompleted(val exported: ExportedPdf) : ScannerUiEffect
    data class ShowMessage(val messageResId: Int) : ScannerUiEffect
}
