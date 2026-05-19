package dev.sonle.pdfscanner.domain.model

sealed class PdfExportError {
    data object EmptyPages : PdfExportError()
    data class IoFailure(val message: String?) : PdfExportError()
}
