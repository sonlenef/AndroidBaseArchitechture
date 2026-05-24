package dev.sonle.pdfscanner.presentation.features.viewer

import androidx.annotation.StringRes
import dev.sonle.pdfscanner.domain.model.ExportedPdf

sealed interface PdfViewerUiEffect {
    data class Share(val exported: ExportedPdf) : PdfViewerUiEffect
    data class OpenWith(val exported: ExportedPdf) : PdfViewerUiEffect
    data class SaveToDownloads(val exported: ExportedPdf) : PdfViewerUiEffect
    data class ShowMessage(@StringRes val messageRes: Int) : PdfViewerUiEffect
}
