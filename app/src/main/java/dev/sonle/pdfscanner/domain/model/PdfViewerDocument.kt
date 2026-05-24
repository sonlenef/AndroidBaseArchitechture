package dev.sonle.pdfscanner.domain.model

/**
 * Metadata for an opened PDF document in the viewer.
 */
data class PdfViewerDocument(
    val fileName: String,
    val absolutePath: String,
    val pageCount: Int,
    val fileSizeBytes: Long
) {
    fun toExportedPdf(): ExportedPdf = ExportedPdf(
        fileName = fileName,
        absolutePath = absolutePath,
        fileSizeBytes = fileSizeBytes
    )
}
