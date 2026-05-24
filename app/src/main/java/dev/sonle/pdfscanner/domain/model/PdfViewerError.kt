package dev.sonle.pdfscanner.domain.model

/**
 * Errors that can occur while opening or rendering a PDF in the in-app viewer.
 */
sealed class PdfViewerError {
    data object FileNotFound : PdfViewerError()
    data object OpenFailed : PdfViewerError()
    data class RenderFailed(val pageIndex: Int) : PdfViewerError()
}
